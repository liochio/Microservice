package com.liochio.auth.service;

import com.liochio.auth.dto.ApprovalDto;
import com.liochio.auth.entity.ApprovalHistoryEntity;
import com.liochio.auth.entity.ApprovalRequestEntity;
import com.liochio.auth.entity.UserEntity;
import com.liochio.auth.repository.ApprovalHistoryRepository;
import com.liochio.auth.repository.ApprovalRequestRepository;
import com.liochio.auth.repository.UserRepository;
import com.liochio.common.exception.AppException;
import com.liochio.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MakerCheckerService {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final ApprovalHistoryRepository approvalHistoryRepository;
    private final UserRepository userRepository;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Transactional
    public ApprovalDto submitRequest(String tenantId, Long makerUserId, ApprovalDto.SubmitRequest request, String ipAddress) {
        String requestCode = "REQ_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        ApprovalRequestEntity entity = ApprovalRequestEntity.builder()
                .tenantId(tenantId != null ? tenantId : "default")
                .requestCode(requestCode)
                .requestType(request.getRequestType())
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .title(request.getTitle())
                .makerUserId(makerUserId)
                .makerNote(request.getMakerNote())
                .payloadBefore(request.getPayloadBefore())
                .payloadAfter(request.getPayloadAfter())
                .status("PENDING")
                .build();

        ApprovalRequestEntity saved = approvalRequestRepository.save(entity);

        // Audit Trail
        ApprovalHistoryEntity history = ApprovalHistoryEntity.builder()
                .approvalRequestId(saved.getId())
                .actorUserId(makerUserId)
                .action("SUBMITTED")
                .actionNote(request.getMakerNote())
                .ipAddress(ipAddress)
                .build();
        approvalHistoryRepository.save(history);

        log.info("Maker {} created approval request {} type {}", makerUserId, requestCode, request.getRequestType());
        return mapToDto(saved);
    }

    @Transactional
    public ApprovalDto actionRequest(Long requestId, Long checkerUserId, ApprovalDto.ActionRequest actionReq, String ipAddress) {
        ApprovalRequestEntity entity = approvalRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy yêu cầu phê duyệt có ID: " + requestId));

        if (!"PENDING".equalsIgnoreCase(entity.getStatus())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Yêu cầu đã được xử lý (trạng thái hiện tại: " + entity.getStatus() + ")");
        }

        // Ràng buộc Separation of Duties (SoD)
        if (Objects.equals(entity.getMakerUserId(), checkerUserId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Vi phạm quy tắc Phân tách Trách nhiệm (SoD): Người tạo lệnh (Maker) không được phép tự phê duyệt lệnh của chính mình!");
        }

        String action = actionReq.getAction().toUpperCase();
        if (!action.equals("APPROVED") && !action.equals("REJECTED")) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Hành động không hợp lệ: " + actionReq.getAction() + " (Chỉ chấp nhận APPROVED hoặc REJECTED)");
        }

        entity.setCheckerUserId(checkerUserId);
        entity.setCheckerNote(actionReq.getCheckerNote());
        entity.setReviewedAt(Instant.now());
        entity.setStatus(action);

        if (action.equals("REJECTED")) {
            entity.setRejectionReason(actionReq.getRejectionReason());
        }

        // Thực thi Payload nếu được APPROVED
        if (action.equals("APPROVED")) {
            executeApprovedPayload(entity);
        }

        // Gửi email thông báo kết quả phê duyệt tới Maker
        sendMakerNotificationEmail(entity, action, checkerUserId, action.equals("APPROVED") ? actionReq.getCheckerNote() : actionReq.getRejectionReason());

        ApprovalRequestEntity saved = approvalRequestRepository.save(entity);

        // Audit Trail
        ApprovalHistoryEntity history = ApprovalHistoryEntity.builder()
                .approvalRequestId(saved.getId())
                .actorUserId(checkerUserId)
                .action(action)
                .actionNote(action.equals("APPROVED") ? actionReq.getCheckerNote() : actionReq.getRejectionReason())
                .ipAddress(ipAddress)
                .build();
        approvalHistoryRepository.save(history);

        log.info("Checker {} actioned {} on request {}", checkerUserId, action, entity.getRequestCode());
        return mapToDto(saved);
    }

    private void executeApprovedPayload(ApprovalRequestEntity entity) {
        try {
            log.info("[MakerChecker] Thực thi payloadAfter cho entityType={}, entityId={}", entity.getEntityType(), entity.getEntityId());
            if ("USER".equalsIgnoreCase(entity.getEntityType()) || "LIMIT_OVERRIDE".equalsIgnoreCase(entity.getRequestType())) {
                if (entity.getEntityId() != null && entity.getPayloadAfter() != null) {
                    if (entity.getPayloadAfter().contains("ACTIVE")) {
                        jdbcTemplate.update("UPDATE liochio_core_db.users SET status = 'ACTIVE', updated_at = NOW() WHERE id = ?", entity.getEntityId());
                    }
                }
            } else if ("SYSTEM_CONFIG".equalsIgnoreCase(entity.getEntityType()) || "CONFIG".equalsIgnoreCase(entity.getEntityType())) {
                if (entity.getPayloadAfter() != null && entity.getEntityId() != null) {
                    jdbcTemplate.update("UPDATE liochio_core_db.system_configs SET config_value = ?, updated_at = NOW() WHERE config_key = ?",
                            entity.getPayloadAfter(), entity.getEntityId());
                }
            }
        } catch (Exception e) {
            log.warn("[MakerChecker] Không thể tự động áp dụng payloadAfter: {}", e.getMessage());
        }
    }

    private void sendMakerNotificationEmail(ApprovalRequestEntity entity, String action, Long checkerUserId, String note) {
        try {
            String makerEmail = "voduylebt99@gmail.com";
            if (entity.getMakerUserId() != null) {
                var userOpt = userRepository.findById(entity.getMakerUserId());
                if (userOpt.isPresent() && userOpt.get().getEmail() != null) {
                    makerEmail = userOpt.get().getEmail();
                }
            }
            String statusText = action.equals("APPROVED") ? "ĐÃ ĐƯỢC PHÊ DUYỆT" : "ĐÃ BỊ TỪ CHỐI";
            String subject = "[Liochio Maker-Checker] Yêu cầu " + entity.getRequestCode() + " " + statusText;
            String body = "<div style=\"font-family: Arial, sans-serif; padding: 15px; color: #1F2937;\">"
                    + "<h3>Thông báo kết quả duyệt yêu cầu</h3>"
                    + "<p>Yêu cầu: <b>" + entity.getTitle() + "</b> (Mã: <code>" + entity.getRequestCode() + "</code>)</p>"
                    + "<p>Trạng thái: <b style=\"color:" + (action.equals("APPROVED") ? "green" : "red") + ";\">" + statusText + "</b></p>"
                    + "<p>Người duyệt: ID <b>" + checkerUserId + "</b></p>"
                    + "<p>Ghi chú: " + (note != null ? note : "Không có") + "</p>"
                    + "</div>";

            jdbcTemplate.update(
                    "INSERT INTO liochio_app_db.mail_logs (trace_id, recipient, channel, template_code, language_code, subject, content, status, execution_time_ms, retry_count, created_at) "
                            + "VALUES (?, ?, 'EMAIL', 'MAKER_CHECKER_ALERT', 'vi', ?, ?, 'PENDING', 0, 0, NOW())",
                    entity.getRequestCode(), makerEmail, subject, body
            );
            log.info("[MakerChecker] Đã ghi nhận mail_log thông báo cho Maker '{}'", makerEmail);
        } catch (Exception e) {
            log.warn("[MakerChecker] Không thể tạo mail_log cho Maker: {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Page<ApprovalDto> listRequests(String tenantId, String status, Pageable pageable) {
        String effectiveTenant = (tenantId != null && !tenantId.isBlank()) ? tenantId : "default";
        Page<ApprovalRequestEntity> page;
        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) {
            page = approvalRequestRepository.findByTenantIdAndStatusOrderByCreatedAtDesc(effectiveTenant, status.toUpperCase(), pageable);
        } else {
            page = approvalRequestRepository.findByTenantIdOrderByCreatedAtDesc(effectiveTenant, pageable);
        }
        return page.map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public ApprovalDto getRequestById(Long id) {
        ApprovalRequestEntity entity = approvalRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy yêu cầu phê duyệt có ID: " + id));
        return mapToDto(entity);
    }

    private ApprovalDto mapToDto(ApprovalRequestEntity entity) {
        String makerUsername = userRepository.findById(entity.getMakerUserId())
                .map(UserEntity::getUsername).orElse("User#" + entity.getMakerUserId());

        String checkerUsername = null;
        if (entity.getCheckerUserId() != null) {
            checkerUsername = userRepository.findById(entity.getCheckerUserId())
                    .map(UserEntity::getUsername).orElse("User#" + entity.getCheckerUserId());
        }

        List<ApprovalHistoryEntity> histories = approvalHistoryRepository.findByApprovalRequestIdOrderByCreatedAtAsc(entity.getId());
        List<ApprovalDto.ApprovalHistoryDto> historyDtos = histories.stream().map(h -> {
            String actorName = userRepository.findById(h.getActorUserId())
                    .map(UserEntity::getUsername).orElse("User#" + h.getActorUserId());
            return ApprovalDto.ApprovalHistoryDto.builder()
                    .id(h.getId())
                    .actorUserId(h.getActorUserId())
                    .actorUsername(actorName)
                    .action(h.getAction())
                    .actionNote(h.getActionNote())
                    .ipAddress(h.getIpAddress())
                    .createdAt(h.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());

        return ApprovalDto.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .requestCode(entity.getRequestCode())
                .requestType(entity.getRequestType())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .title(entity.getTitle())
                .makerUserId(entity.getMakerUserId())
                .makerUsername(makerUsername)
                .makerNote(entity.getMakerNote())
                .payloadBefore(entity.getPayloadBefore())
                .payloadAfter(entity.getPayloadAfter())
                .checkerUserId(entity.getCheckerUserId())
                .checkerUsername(checkerUsername)
                .checkerNote(entity.getCheckerNote())
                .status(entity.getStatus())
                .rejectionReason(entity.getRejectionReason())
                .createdAt(entity.getCreatedAt())
                .reviewedAt(entity.getReviewedAt())
                .history(historyDtos)
                .build();
    }
}

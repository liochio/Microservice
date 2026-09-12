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

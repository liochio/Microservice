package com.liochio.notification.service;

import com.liochio.common.context.TenantContext;
import com.liochio.common.enums.NotificationChannel;
import com.liochio.common.exception.AppException;
import com.liochio.common.exception.ErrorCode;
import com.liochio.common.pattern.factory.NotificationFactory;
import com.liochio.common.pattern.strategy.NotificationStrategy;
import com.liochio.notification.dto.NotificationResponse;
import com.liochio.notification.dto.NotificationSendRequest;
import com.liochio.notification.entity.NotificationLogEntity;
import com.liochio.notification.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ==============================================================================
 * Dịch Vụ Điều Phối Thông Báo Đa Kênh Chuẩn Enterprise (Notification Hub Service)
 * ==============================================================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationLogRepository notificationLogRepository;
    private final NotificationFactory notificationFactory;
    private final NotificationTemplateEngine templateEngine;

    @Transactional
    public NotificationResponse send(NotificationSendRequest request) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            tenantId = "default";
        }

        // 1. Kiểm tra tính Bất Biến / Chống Gửi Lặp (Idempotency Engine)
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
            Optional<NotificationLogEntity> existing = notificationLogRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existing.isPresent()) {
                log.info("[NotificationService] Trả về kết quả thông báo đã lưu trước đó cho IdempotencyKey='{}'", request.getIdempotencyKey());
                return mapToResponse(existing.get());
            }
        }

        // 2. Phân giải nội dung mẫu động (Dynamic Template Engine)
        String subject = request.getSubject();
        String content = request.getContent();

        if (request.getTemplateCode() != null && !request.getTemplateCode().isBlank()) {
            NotificationTemplateEngine.CompiledTemplate compiled = templateEngine.compile(
                    tenantId,
                    request.getTemplateCode(),
                    request.getChannel() != null ? request.getChannel().name() : "EMAIL",
                    request.getLocale(),
                    request.getTemplateParams()
            );
            if (compiled != null) {
                if (compiled.getSubject() != null && !compiled.getSubject().isBlank()) {
                    subject = compiled.getSubject();
                }
                if (compiled.getContent() != null && !compiled.getContent().isBlank()) {
                    content = compiled.getContent();
                }
            }
        }

        if (content == null || content.isBlank()) {
            content = "Thông báo hệ thống Liochio";
        }

        // 3. Điều phối qua Adapter kênh truyền chỉ định
        boolean success = false;
        String errorMessage = null;
        try {
            NotificationStrategy strategy = notificationFactory.getStrategy(request.getChannel());
            success = strategy.send(request.getRecipient(), subject, content, request.getMetadata());
        } catch (Exception ex) {
            log.error("[NotificationService] Gửi qua kênh '{}' gặp lỗi: {}", request.getChannel(), ex.getMessage());
            errorMessage = ex.getMessage();

            // Kênh dự phòng (Fallback): Nếu SMS/Push thất bại -> Thử gửi qua Email nếu recipient là email
            if (request.getChannel() != NotificationChannel.EMAIL && request.getRecipient().contains("@")) {
                log.info("[NotificationService] Kích hoạt Fallback sang kênh EMAIL cho '{}'", request.getRecipient());
                try {
                    NotificationStrategy emailStrategy = notificationFactory.getStrategy(NotificationChannel.EMAIL);
                    success = emailStrategy.send(request.getRecipient(), subject, content, request.getMetadata());
                    if (success) {
                        errorMessage = "Đã gửi qua kênh dự phòng EMAIL (Kênh gốc " + request.getChannel() + " lỗi)";
                    }
                } catch (Exception emailEx) {
                    log.error("[NotificationService] Fallback sang EMAIL cũng thất bại: {}", emailEx.getMessage());
                }
            }
        }

        // 4. Lưu vết nhật ký thông báo
        NotificationLogEntity entity = NotificationLogEntity.builder()
                .recipient(request.getRecipient())
                .channel(request.getChannel())
                .subject(subject)
                .content(content)
                .status(success ? "SUCCESS" : "FAILED")
                .idempotencyKey(request.getIdempotencyKey())
                .templateCode(request.getTemplateCode())
                .isRead(false)
                .errorMessage(errorMessage != null ? errorMessage : (success ? null : "Gửi thông báo thất bại qua adapter"))
                .build();
        entity.setTenantId(tenantId);

        NotificationLogEntity saved = notificationLogRepository.save(entity);
        log.info("[NotificationService] Đã lưu log gửi thông báo ID: {}, Status: {}, IdempotencyKey: {}",
                saved.getId(), saved.getStatus(), saved.getIdempotencyKey());

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getHistory() {
        String tenantId = TenantContext.getTenantId();
        return notificationLogRepository.findByTenantId(tenantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyInbox(String recipient) {
        String tenantId = TenantContext.getTenantId();
        List<NotificationLogEntity> list;
        if (tenantId != null && !tenantId.isBlank() && !tenantId.equals("default")) {
            list = notificationLogRepository.findByTenantIdAndRecipientOrderByCreatedAtDesc(tenantId, recipient);
        } else {
            list = notificationLogRepository.findByRecipientOrderByCreatedAtDesc(recipient);
        }
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public NotificationResponse markAsRead(Long id, String recipient) {
        NotificationLogEntity entity = notificationLogRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy thông báo ID: " + id));

        if (recipient != null && !recipient.isBlank() && !recipient.equalsIgnoreCase(entity.getRecipient())) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Bạn không có quyền thao tác trên thông báo của người khác");
        }

        entity.setIsRead(true);
        entity.setReadAt(Instant.now());
        NotificationLogEntity updated = notificationLogRepository.save(entity);
        return mapToResponse(updated);
    }

    @Transactional
    public int markAllAsRead(String recipient) {
        if (recipient == null || recipient.isBlank()) {
            return 0;
        }
        return notificationLogRepository.markAllAsRead(recipient, Instant.now());
    }

    private NotificationResponse mapToResponse(NotificationLogEntity entity) {
        return NotificationResponse.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .recipient(entity.getRecipient())
                .channel(entity.getChannel() != null ? entity.getChannel().name() : null)
                .subject(entity.getSubject())
                .content(entity.getContent())
                .status(entity.getStatus())
                .idempotencyKey(entity.getIdempotencyKey())
                .templateCode(entity.getTemplateCode())
                .isRead(entity.getIsRead())
                .readAt(entity.getReadAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
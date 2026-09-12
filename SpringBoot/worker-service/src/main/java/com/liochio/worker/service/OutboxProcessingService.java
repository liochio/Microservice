package com.liochio.worker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liochio.worker.entity.DlqMessageEntity;
import com.liochio.worker.entity.MailLogEntity;
import com.liochio.worker.repository.DlqMessageRepository;
import com.liochio.worker.repository.MailLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ==============================================================================
 * Dịch Vụ Xử Lý Sự Kiện Outbox Đa CSDL & Quản Trị DLQ Chuẩn Enterprise
 * ==============================================================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxProcessingService {

    private final JdbcTemplate jdbcTemplate;
    private final MailLogRepository mailLogRepository;
    private final DlqMessageRepository dlqRepository;
    private final TelegramAlertService telegramAlertService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static final int MAX_EVENT_RETRIES = 5;

    private static final List<String> OUTBOX_TABLES = List.of(
            "liochio_core_db.outbox_events",
            "liochio_app_db.outbox_events"
    );

    @Transactional
    public int processPendingOutboxEvents() {
        int totalProcessed = 0;
        for (String table : OUTBOX_TABLES) {
            totalProcessed += processTableOutboxEvents(table);
        }
        return totalProcessed;
    }

    private int processTableOutboxEvents(String tableName) {
        try {
            String selectSql = "SELECT id, aggregate_type, aggregate_id, event_type, payload, tenant_id FROM " + tableName + " WHERE status = 'PENDING' ORDER BY id ASC LIMIT 50";
            List<Map<String, Object>> pendingEvents = jdbcTemplate.queryForList(selectSql);

            if (pendingEvents.isEmpty()) {
                return 0;
            }

            log.info("[OutboxProcessingService] Đang xử lý {} sự kiện Outbox từ {}...", pendingEvents.size(), tableName);
            int count = 0;

            for (Map<String, Object> ev : pendingEvents) {
                Long id = ((Number) ev.get("id")).longValue();
                String eventType = (String) ev.get("event_type");
                String aggregateId = (String) ev.get("aggregate_id");
                String payload = (String) ev.get("payload");
                String tenantId = (String) ev.get("tenant_id");

                try {
                    log.info("[OutboxProcessingService] -> Phát tán sự kiện EventType='{}', AggregateId='{}'", eventType, aggregateId);

                    // 1. Chuyển đổi sự kiện thành thông báo (Notification / Mail)
                    handleEventNotification(eventType, aggregateId, payload, tenantId);

                    // 2. Cập nhật trạng thái sang PROCESSED
                    jdbcTemplate.update("UPDATE " + tableName + " SET status = 'PROCESSED' WHERE id = ?", id);
                    count++;
                } catch (Exception ex) {
                    log.error("[OutboxProcessingService] Lỗi xử lý sự kiện ID={} từ {}: {}", id, tableName, ex.getMessage());

                    // Chuyển sang Dead Letter Queue (DLQ)
                    DlqMessageEntity dlq = DlqMessageEntity.builder()
                            .eventId(tableName + "_" + id)
                            .topic(eventType)
                            .traceId("trace_dlq_" + id)
                            .payload(payload != null ? payload : "{}")
                            .retryCount(MAX_EVENT_RETRIES)
                            .errorReason("Xử lý Outbox Event thất bại: " + ex.getMessage())
                            .status("PENDING_RETRY")
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build();
                    dlqRepository.save(dlq);

                    // Bắn Telegram Alert khẩn cấp
                    telegramAlertService.sendCriticalAlert(
                            "🚨 [OUTBOX DLQ ROUTING] Sự kiện xử lý thất bại",
                            "Sự kiện EventType: " + eventType + "\nTable: " + tableName + "\nID: " + id + "\nLỗi: " + ex.getMessage(),
                            "trace_dlq_" + id,
                            "OUTBOX_RELAY"
                    );

                    jdbcTemplate.update("UPDATE " + tableName + " SET status = 'FAILED_DLQ' WHERE id = ?", id);
                }
            }

            return count;
        } catch (Exception e) {
            log.warn("[OutboxProcessingService] Quét {} gặp cảnh báo: {}", tableName, e.getMessage());
            return 0;
        }
    }

    private void handleEventNotification(String eventType, String aggregateId, String payload, String tenantId) {
        if (payload == null || payload.isBlank()) return;

        try {
            JsonNode node = objectMapper.readTree(payload);
            String email = node.has("email") ? node.get("email").asText() : null;
            String username = node.has("username") ? node.get("username").asText() : aggregateId;
            String traceId = "evt_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
            String currentTenant = (tenantId != null && !tenantId.isBlank()) ? tenantId : "default";

            if (email == null || email.isBlank()) {
                try {
                    email = jdbcTemplate.queryForObject(
                            "SELECT email FROM liochio_core_db.users WHERE id = ?",
                            String.class,
                            aggregateId
                    );
                } catch (Exception ignored) {}
            }

            if (email == null || email.isBlank()) {
                return;
            }

            if ("USER_REGISTERED".equalsIgnoreCase(eventType)) {
                String subject = "Đăng ký tài khoản Liochio thành công: Vui lòng kiểm tra mã OTP kích hoạt";
                String content = "Xin chào " + username + ",\n\nBạn vừa đăng ký tài khoản tại Liochio FinTech Platform. Vui lòng kiểm tra mã OTP trong hộp thư để kích hoạt tài khoản.\n\nTrân trọng,\nLiochio Team";
                try {
                    jdbcTemplate.update(
                            "INSERT INTO liochio_app_db.notifications (tenant_id, recipient, channel, subject, content, status, version, created_at, updated_at) VALUES (?, ?, ?, ?, ?, 'SUCCESS', 1, NOW(), NOW())",
                            currentTenant, email, "IN_APP", subject, content
                    );
                } catch (Exception ignored) {}
                return;
            }

            String subject = null;
            String content = null;
            String templateCode = null;

            if ("USER_ACTIVATED".equalsIgnoreCase(eventType) || "USER_VERIFIED".equalsIgnoreCase(eventType)) {
                subject = "Chào mừng thành viên mới: " + username + " đã kích hoạt thành công!";
                content = "Xin chào " + username + ",\n\nTài khoản của bạn đã được kích hoạt thành công trên hệ sinh thái Liochio Platform.\nBạn đã được cấp 3 tài khoản sổ cái kế toán và ví tiền tệ VND mặc định.\n\nTrân trọng,\nLiochio FinTech Team";
                templateCode = "WELCOME_ACTIVATED_MAIL";
            } else if ("LOGIN_ALERT".equalsIgnoreCase(eventType) || "DEVICE_CHALLENGE".equalsIgnoreCase(eventType)) {
                subject = "[CẢNH BÁO BẢO MẬT] Phát hiện đăng nhập thiết bị mới";
                content = "Xin chào " + username + ",\n\nHệ thống phát hiện tài khoản của bạn vừa đăng nhập từ một thiết bị hoặc IP mới.\nNếu đây không phải là bạn, vui lòng đổi mật khẩu ngay lập tức.\n\nTrân trọng,\nLiochio Security Center";
                templateCode = "LOGIN_SECURITY_ALERT";
            } else if ("PASSWORD_CHANGED".equalsIgnoreCase(eventType)) {
                subject = "[BẢO MẬT] Mật khẩu tài khoản đã được thay đổi thành công";
                content = "Xin chào " + username + ",\n\nMật khẩu tài khoản Liochio của bạn vừa được thay đổi thành công.\nNếu bạn không thực hiện yêu cầu này, vui lòng liên hệ bộ phận hỗ trợ an ninh ngay lập tức.\n\nTrân trọng,\nLiochio Security Team";
                templateCode = "PASSWORD_CHANGED_ALERT";
            } else if ("TRANSFER_SUCCESS".equalsIgnoreCase(eventType) || "PAYMENT_SUCCESS".equalsIgnoreCase(eventType)) {
                subject = "[BIÊN LAI] Giao dịch thanh toán / chuyển tiền thành công";
                content = "Xin chào " + username + ",\n\nGiao dịch của bạn đã được xử lý thành công trên hệ thống Sổ cái kép.\n\nTrân trọng,\nLiochio Core Banking";
                templateCode = "TRANSFER_SUCCESS_MAIL";
            } else if ("PAYMENT_FAILED".equalsIgnoreCase(eventType)) {
                subject = "[THÔNG BÁO] Giao dịch thanh toán không thành công";
                content = "Xin chào " + username + ",\n\nGiao dịch thanh toán của bạn không thành công hoặc đã bị từ chối. Vui lòng kiểm tra lại số dư hoặc phương thức thanh toán.\n\nTrân trọng,\nLiochio Payment Support";
                templateCode = "PAYMENT_FAILED_ALERT";
            } else if ("REFUND_PROCESSED".equalsIgnoreCase(eventType)) {
                subject = "[HOÀN TIỀN] Giao dịch hoàn tiền đã được xử lý thành công";
                content = "Xin chào " + username + ",\n\nYêu cầu hoàn tiền của bạn đã được xử lý và cộng trực tiếp vào ví tài khoản.\n\nTrân trọng,\nLiochio Payment Team";
                templateCode = "REFUND_SUCCESS_MAIL";
            } else if ("EKYC_APPROVED".equalsIgnoreCase(eventType) || "EKYC_VERIFIED".equalsIgnoreCase(eventType)) {
                subject = "Chúc mừng: Hồ sơ định danh eKYC đã được phê duyệt TIER_3";
                content = "Xin chào " + username + ",\n\nHồ sơ định danh cá nhân (eKYC) của bạn đã được đối soát và phê duyệt nâng hạn mức giao dịch lên TIER_3 (Hạn mức 500,000,000 VND/ngày).\n\nTrân trọng,\nLiochio Identity Team";
                templateCode = "EKYC_APPROVED_MAIL";
            }

            if (subject != null && content != null) {
                // 1. Đẩy vào hàng đợi Mail Logs
                MailLogEntity mailLog = MailLogEntity.builder()
                        .traceId(traceId)
                        .recipient(email)
                        .channel("EMAIL")
                        .templateCode(templateCode)
                        .languageCode("vi")
                        .subject(subject)
                        .content(content)
                        .status("PENDING")
                        .retryCount(0)
                        .executionTimeMs(0L)
                        .createdAt(Instant.now())
                        .build();

                mailLogRepository.save(mailLog);

                // 2. Đồng thời đẩy vào bảng Notifications trung tâm
                try {
                    jdbcTemplate.update(
                            "INSERT INTO liochio_app_db.notifications (tenant_id, recipient, channel, subject, content, status, version, created_at, updated_at) VALUES (?, ?, ?, ?, ?, 'SUCCESS', 1, NOW(), NOW())",
                            currentTenant, email, "IN_APP", subject, content
                    );
                } catch (Exception notiErr) {
                    log.debug("[OutboxProcessingService] Ghi liochio_app_db.notifications: {}", notiErr.getMessage());
                }

                log.info("[OutboxProcessingService] 📨 Đã đồng bộ Thông báo & Mail cho sự kiện '{}' tới '{}'", eventType, email);
            }
        } catch (Exception e) {
            log.warn("[OutboxProcessingService] Không thể tạo thông báo từ sự kiện '{}': {}", eventType, e.getMessage());
            throw new RuntimeException("Lỗi xử lý sự kiện: " + e.getMessage(), e);
        }
    }
}
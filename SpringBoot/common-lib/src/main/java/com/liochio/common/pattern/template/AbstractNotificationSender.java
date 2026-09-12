package com.liochio.common.pattern.template;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * ==============================================================================
 * Khung Gửi Thông Báo Chuẩn (Abstract Notification Sender - Template Method)
 * ==============================================================================
 * 
 * Mục đích:
 * - Định nghĩa các bước chuẩn để gửi thông báo:
 *   1. Kiểm tra tính hợp lệ người nhận (validateRecipient)
 *   2. Làm giàu nội dung qua Template Engine (enrichContent)
 *   3. Thực hiện gửi thực tế qua adapter mạng (executeSend)
 *   4. Ghi vết kết quả gửi (logResult)
 */
@Slf4j
public abstract class AbstractNotificationSender {

    public final boolean sendNotification(String recipient, String subject, String rawContent, Map<String, Object> metadata) {
        log.info("[NotificationSender] Bắt đầu gửi thông báo tới: '{}', Tiêu đề: '{}'", recipient, subject);

        validateRecipient(recipient);
        String finalContent = enrichContent(rawContent, metadata);
        boolean success = executeSend(recipient, subject, finalContent, metadata);
        logResult(recipient, success);

        return success;
    }

    protected void validateRecipient(String recipient) {
        if (recipient == null || recipient.isBlank()) {
            throw new IllegalArgumentException("Người nhận thông báo không được để trống");
        }
    }

    protected String enrichContent(String rawContent, Map<String, Object> metadata) {
        return rawContent != null ? rawContent : "";
    }

    protected abstract boolean executeSend(String recipient, String subject, String content, Map<String, Object> metadata);

    protected void logResult(String recipient, boolean success) {
        if (success) {
            log.info("[NotificationSender] Gửi thông báo thành công tới: '{}'", recipient);
        } else {
            log.error("[NotificationSender] Gửi thông báo thất bại tới: '{}'", recipient);
        }
    }
}

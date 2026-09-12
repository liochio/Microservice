package com.liochio.common.enums;

import lombok.Getter;

/**
 * ==============================================================================
 * Enum Kênh Gửi Thông Báo (Notification Channels)
 * ==============================================================================
 * 
 * Mục đích:
 * - Định nghĩa các kênh thông báo hỗ trợ trong Notification Hub (Strategy Pattern):
 *   + EMAIL: Gửi qua SMTP / Amazon SES / Mailgun
 *   + SMS: Gửi mã OTP / SMS Brandname qua Twilio / eSMS
 *   + TELEGRAM: Gửi qua Telegram Bot Webhook
 *   + WEBSOCKET: Đẩy tin tức thời gian thực tới trình duyệt client qua STOMP
 *   + IN_APP: Lưu thông báo vào hộp thư cá nhân trong hệ thống
 * 
 * Khi nào gọi:
 * - Được NotificationStrategyFactory và NotificationService điều phối.
 */
@Getter
public enum NotificationChannel {
    EMAIL("Thư điện tử (Email SMTP/SES)"),
    SMS("Tin nhắn SMS / OTP Brandname"),
    TELEGRAM("Telegram Bot Webhook"),
    WEBSOCKET("Thông báo đẩy thời gian thực STOMP"),
    IN_APP("Thông báo nội bộ trong ứng dụng");

    private final String description;

    NotificationChannel(String description) {
        this.description = description;
    }
}

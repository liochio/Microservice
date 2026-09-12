package com.liochio.common.pattern.strategy;

import com.liochio.common.enums.NotificationChannel;

import java.util.Map;

/**
 * ==============================================================================
 * Chiến Lược Gửi Thông Báo Đa Kênh (Notification Strategy Interface - GoF Strategy)
 * ==============================================================================
 * 
 * Mục đích:
 * - Định nghĩa giao diện chuẩn cho việc phát tán thông báo qua nhiều kênh
 *   (Email, SMS/OTP, Telegram Webhook, WebSocket STOMP Push).
 */
public interface NotificationStrategy {

    /**
     * Trả về kênh thông báo mà Strategy này đảm nhiệm
     */
    NotificationChannel getChannel();

    /**
     * Gửi thông báo tới người nhận
     *
     * @param recipient Người nhận (Email, Số điện thoại, Chat ID, Topic WebSocket)
     * @param subject   Tiêu đề thông báo
     * @param content   Nội dung thông báo
     * @param metadata  Tham số bổ sung
     * @return true nếu gửi thành công
     */
    boolean send(String recipient, String subject, String content, Map<String, Object> metadata);
}

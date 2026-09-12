package com.liochio.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * ==============================================================================
 * DTO Trả Về Kết Quả Gửi Thông Báo (Notification Response DTO)
 * ==============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private String tenantId;
    private String recipient;
    private String channel;
    private String subject;
    private String content;
    private String status;
    private String idempotencyKey;
    private String templateCode;
    private Boolean isRead;
    private Instant readAt;
    private Instant createdAt;
}

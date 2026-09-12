package com.liochio.notification.dto;

import com.liochio.common.enums.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * ==============================================================================
 * DTO Yêu Cầu Gửi Thông Báo (Notification Send Request DTO)
 * ==============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSendRequest {

    @NotBlank(message = "Người nhận không được để trống")
    private String recipient;

    @NotNull(message = "Kênh thông báo không được null")
    private NotificationChannel channel;

    private String subject;

    private String content;

    private String idempotencyKey;

    private String templateCode;

    private String locale;

    private Map<String, Object> templateParams;

    private Map<String, Object> metadata;
}

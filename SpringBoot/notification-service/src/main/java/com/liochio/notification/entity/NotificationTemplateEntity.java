package com.liochio.notification.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

/**
 * ==============================================================================
 * Thực Thể Mẫu Thông Báo Đa Ngôn Ngữ (Notification Template - Bảng 25)
 * ==============================================================================
 */
@Entity
@Table(name = "notification_templates", indexes = {
        @Index(name = "uk_template_lookup", columnList = "tenant_id, template_code, channel, locale", unique = true)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplateEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", length = 50, nullable = false)
    @Builder.Default
    private String tenantId = "SYSTEM";

    @Column(name = "template_code", length = 100, nullable = false)
    private String templateCode;

    @Column(name = "channel", length = 30, nullable = false)
    private String channel; // EMAIL, SMS, TELEGRAM, FIREBASE_PUSH

    @Column(name = "locale", length = 10, nullable = false)
    @Builder.Default
    private String locale = "vi";

    @Column(name = "subject", length = 255)
    private String subject;

    @Column(name = "body_template", columnDefinition = "TEXT", nullable = false)
    private String bodyTemplate;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    @Builder.Default
    private Instant updatedAt = Instant.now();
}

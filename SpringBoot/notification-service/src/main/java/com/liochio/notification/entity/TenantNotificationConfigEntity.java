package com.liochio.notification.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

/**
 * ==============================================================================
 * Thực Thể Cấu Hình Kênh Gửi Tin Của Tenant (Notification Config - Bảng 24)
 * ==============================================================================
 */
@Entity
@Table(name = "tenant_notification_configs", indexes = {
        @Index(name = "uk_tenant_channel", columnList = "tenant_id, channel", unique = true)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantNotificationConfigEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", length = 50, nullable = false)
    private String tenantId;

    @Column(name = "channel", length = 30, nullable = false)
    private String channel; // EMAIL, SMS, TELEGRAM, DISCORD, FIREBASE_PUSH

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "config_data", columnDefinition = "JSON", nullable = false)
    private String configData;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    @Builder.Default
    private Instant updatedAt = Instant.now();
}

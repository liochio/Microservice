package com.liochio.notification.entity;

import com.liochio.common.entity.BaseEntity;
import com.liochio.common.enums.NotificationChannel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;

/**
 * ==============================================================================
 * Thực Thể Nhật Ký Thông Báo (Notification Log Entity)
 * ==============================================================================
 */
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "uk_noti_idempotency", columnList = "idempotency_key", unique = true),
        @Index(name = "idx_noti_recipient_read", columnList = "recipient, is_read")
})
@SQLDelete(sql = "UPDATE notifications SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLogEntity extends BaseEntity {

    @Column(name = "recipient", length = 255, nullable = false)
    private String recipient;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", length = 50, nullable = false)
    private NotificationChannel channel;

    @Column(name = "subject", length = 255)
    private String subject;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private String status = "SUCCESS";

    @Column(name = "idempotency_key", length = 100, unique = true)
    private String idempotencyKey;

    @Column(name = "template_code", length = 100)
    private String templateCode;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}

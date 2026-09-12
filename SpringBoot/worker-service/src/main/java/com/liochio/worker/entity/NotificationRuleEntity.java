package com.liochio.worker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "notification_rules", indexes = {
        @Index(name = "idx_rule_trigger", columnList = "action_trigger")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "action_trigger", length = 100, nullable = false)
    private String actionTrigger;

    @Column(name = "user_role_target", length = 50, nullable = false)
    @Builder.Default
    private String userRoleTarget = "ALL";

    @Column(name = "channel", length = 30, nullable = false)
    @Builder.Default
    private String channel = "EMAIL";

    @Column(name = "template_code", length = 100, nullable = false)
    private String templateCode;

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private Boolean isEnabled = true;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();
}

package com.liochio.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

/**
 * ==============================================================================
 * Thực Thể Cấu Hình Môi Trường Động (Dynamic System Configuration Entity)
 * ==============================================================================
 */
@Entity
@Table(name = "system_configs", uniqueConstraints = {
        @UniqueConstraint(name = "uk_system_config_env_service_key", columnNames = {"env_profile", "service_name", "config_key"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "env_profile", length = 20, nullable = false)
    @Builder.Default
    private String envProfile = "dev";

    @Column(name = "service_name", length = 50, nullable = false)
    @Builder.Default
    private String serviceName = "global";

    @Column(name = "config_key", length = 100, nullable = false)
    private String configKey;

    @Column(name = "config_value", columnDefinition = "TEXT", nullable = false)
    private String configValue;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "is_sensitive", nullable = false)
    @Builder.Default
    private Boolean isSensitive = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    @Builder.Default
    private Instant updatedAt = Instant.now();
}

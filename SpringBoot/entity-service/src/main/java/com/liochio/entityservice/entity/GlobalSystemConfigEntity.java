package com.liochio.entityservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "global_system_configs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalSystemConfigEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "config_key", length = 80, nullable = false)
    private String configKey;

    @Column(name = "category", length = 50, nullable = false)
    private String category;

    @Column(name = "config_name", length = 120, nullable = false)
    private String configName;

    @Column(name = "value", columnDefinition = "TEXT", nullable = false)
    private String value;

    @Column(name = "data_type", length = 30, nullable = false)
    @Builder.Default
    private String dataType = "STRING";

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "is_tenant_overridable", nullable = false)
    @Builder.Default
    private Boolean isTenantOverridable = false;

    @Column(name = "min_value", length = 50)
    private String minValue;

    @Column(name = "max_value", length = 50)
    private String maxValue;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    @Builder.Default
    private Instant updatedAt = Instant.now();
}

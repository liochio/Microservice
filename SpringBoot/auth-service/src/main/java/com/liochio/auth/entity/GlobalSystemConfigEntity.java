package com.liochio.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
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
    @Column(name = "config_key", length = 100, nullable = false)
    private String configKey;

    @Column(name = "category", length = 50, nullable = false)
    private String category; // FINANCIAL, SECURITY, INTEGRATION, HARDWARE, COMPLIANCE

    @Column(name = "config_name", length = 150, nullable = false)
    private String configName;

    @Column(name = "data_type", length = 20, nullable = false)
    @Builder.Default
    private String dataType = "STRING"; // STRING, NUMBER, BOOLEAN, JSON

    @Column(name = "value", columnDefinition = "TEXT", nullable = false)
    private String value;

    @Column(name = "min_value", precision = 18, scale = 4)
    private BigDecimal minValue;

    @Column(name = "max_value", precision = 18, scale = 4)
    private BigDecimal maxValue;

    @Column(name = "is_tenant_overridable", nullable = false)
    @Builder.Default
    private Boolean isTenantOverridable = false;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at")
    @Builder.Default
    private Instant updatedAt = Instant.now();
}

package com.liochio.otp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "otp_service_configs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpConfigEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", length = 50, nullable = false, unique = true)
    private String tenantId;

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private Boolean isEnabled = true;

    @Column(name = "bypass_in_dev", nullable = false)
    @Builder.Default
    private Boolean bypassInDev = true;

    @Column(name = "dev_bypass_code", length = 20, nullable = false)
    @Builder.Default
    private String devBypassCode = "123456";

    @Column(name = "environment", length = 20, nullable = false)
    @Builder.Default
    private String environment = "DEV";

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
}

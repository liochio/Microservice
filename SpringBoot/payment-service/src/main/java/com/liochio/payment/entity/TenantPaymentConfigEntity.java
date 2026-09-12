package com.liochio.payment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

/**
 * ==============================================================================
 * Thực Thể Cấu Hình Cổng Thanh Toán Tenant (Payment Config - Bảng 19)
 * ==============================================================================
 */
@Entity
@Table(name = "tenant_payment_configs", indexes = {
        @Index(name = "uk_tenant_gateway", columnList = "tenant_id, gateway_name", unique = true)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantPaymentConfigEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", length = 50, nullable = false)
    private String tenantId;

    @Column(name = "gateway_name", length = 50, nullable = false)
    private String gatewayName; // VNPAY, MOMO, ZALOPAY, STRIPE, PAYPAL

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_sandbox", nullable = false)
    @Builder.Default
    private Boolean isSandbox = true;

    @Column(name = "credentials", columnDefinition = "JSON", nullable = false)
    private String credentials;

    @Column(name = "callback_url", length = 500)
    private String callbackUrl;

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

package com.liochio.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "customer_onboarding_gates")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerOnboardingGateEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", length = 50, nullable = false)
    private String tenantId;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    // Gate 1: eKYC Thẩm định hồ sơ
    @Column(name = "gate1_ekyc_status", length = 30, nullable = false)
    @Builder.Default
    private String gate1EkycStatus = "PENDING"; // NOT_STARTED, PENDING, APPROVED, REJECTED

    @Column(name = "gate1_reviewer_id")
    private Long gate1ReviewerId;

    @Column(name = "gate1_reviewed_at")
    private Instant gate1ReviewedAt;

    @Column(name = "gate1_notes", columnDefinition = "TEXT")
    private String gate1Notes;

    // Gate 2: Cấp phát Vai trò & Xếp hạng Tier
    @Column(name = "gate2_role_tier_status", length = 30, nullable = false)
    @Builder.Default
    private String gate2RoleTierStatus = "NOT_STARTED"; // NOT_STARTED, PENDING, APPROVED, REJECTED

    @Column(name = "assigned_tier", length = 30, nullable = false)
    @Builder.Default
    private String assignedTier = "TIER_1";

    @Column(name = "assigned_daily_limit", precision = 18, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal assignedDailyLimit = new BigDecimal("5000000.00");

    @Column(name = "gate2_reviewer_id")
    private Long gate2ReviewerId;

    @Column(name = "gate2_reviewed_at")
    private Instant gate2ReviewedAt;

    // Gate 3: Kích hoạt Cặp Tài khoản Sổ cái Core-Banking
    @Column(name = "gate3_wallet_provision_status", length = 30, nullable = false)
    @Builder.Default
    private String gate3WalletProvisionStatus = "NOT_STARTED"; // NOT_STARTED, PENDING, PROVISIONED, FAILED

    @Column(name = "available_account_no", length = 50)
    private String availableAccountNo;

    @Column(name = "savings_account_no", length = 50)
    private String savingsAccountNo;

    @Column(name = "gate3_provisioned_at")
    private Instant gate3ProvisionedAt;

    // Gate 4: Ghép đôi Thiết bị Heo Đất IoT & Trusted Device
    @Column(name = "gate4_device_binding_status", length = 30, nullable = false)
    @Builder.Default
    private String gate4DeviceBindingStatus = "NOT_STARTED"; // NOT_STARTED, PENDING, BOUND, REJECTED

    @Column(name = "bound_device_serial", length = 100)
    private String boundDeviceSerial;

    @Column(name = "bound_device_model", length = 50)
    private String boundDeviceModel;

    @Column(name = "bound_at")
    private Instant boundAt;

    // Overall Status
    @Column(name = "overall_status", length = 30, nullable = false)
    @Builder.Default
    private String overallStatus = "IN_PROGRESS"; // IN_PROGRESS, COMPLETED, BLOCKED

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    @Builder.Default
    private Instant updatedAt = Instant.now();
}

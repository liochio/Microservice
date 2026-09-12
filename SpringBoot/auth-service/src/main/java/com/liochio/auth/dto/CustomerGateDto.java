package com.liochio.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerGateDto {
    private Long id;
    private String tenantId;
    private Long userId;
    private String username;
    private String fullName;
    private String phone;
    private String email;

    // Gate 1
    private String gate1EkycStatus;
    private Long gate1ReviewerId;
    private Instant gate1ReviewedAt;
    private String gate1Notes;

    // Gate 2
    private String gate2RoleTierStatus;
    private String assignedTier;
    private BigDecimal assignedDailyLimit;
    private Long gate2ReviewerId;
    private Instant gate2ReviewedAt;

    // Gate 3
    private String gate3WalletProvisionStatus;
    private String availableAccountNo;
    private String savingsAccountNo;
    private Instant gate3ProvisionedAt;

    // Gate 4
    private String gate4DeviceBindingStatus;
    private String boundDeviceSerial;
    private String boundDeviceModel;
    private Instant boundAt;

    // Overall
    private String overallStatus;
    private Instant completedAt;
    private Instant createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Gate1ReviewRequest {
        private String action; // APPROVED, REJECTED
        private String notes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Gate2TierAssignRequest {
        private String tier; // TIER_1, TIER_2, TIER_3
        private BigDecimal dailyLimit;
        private String notes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Gate4PairingRequest {
        private String deviceSerial;
        private String deviceModel;
        private String hmacSecret;
    }
}

package com.liochio.auth.dto;

import com.liochio.common.annotation.MaskPII;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

public class EkycSubmitRequest {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubmitDto {
        @NotBlank(message = "Số CCCD/Hộ chiếu không được để trống")
        private String idCardNumber;

        private String idCardType; // CCCD, PASSPORT, CMND

        @NotBlank(message = "Họ và tên không được để trống")
        private String fullName;

        private String frontCardImageUrl;
        private String backCardImageUrl;
        private String selfieImageUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApproveDto {
        private String targetEkycLevel; // TIER_2, TIER_3
        private BigDecimal customDailyLimit;
        private String reviewerNote;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EkycResponseDto {
        private Long userId;
        private String username;
        private String fullName;

        @MaskPII(type = MaskPII.MaskType.ID_CARD)
        private String idCardNumber;

        private String idCardType;
        private String ekycLevel;
        private String ekycStatus;
        private BigDecimal dailyTransferLimit;
        private Instant ekycVerifiedAt;
    }
}

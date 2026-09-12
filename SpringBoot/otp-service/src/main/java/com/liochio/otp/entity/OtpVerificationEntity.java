package com.liochio.otp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "user_otp_verifications", indexes = {
        @Index(name = "idx_otp_lookup", columnList = "tenant_id, user_id, otp_purpose, status"),
        @Index(name = "idx_otp_expiry", columnList = "expires_at"),
        @Index(name = "idx_otp_dest_created", columnList = "target_destination, created_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerificationEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", length = 50, nullable = false)
    private String tenantId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "otp_type", length = 30, nullable = false)
    private String otpType; // SMS, EMAIL, SMART_OTP, VOICE_CALL

    @Column(name = "otp_purpose", length = 50, nullable = false)
    private String otpPurpose; // REGISTRATION, DEVICE_TRUST, RESET_PASSWORD, SMART_OTP_ENROLL, TRANSACTION_SIGN

    @Column(name = "otp_code_hash", length = 255)
    private String otpCodeHash;

    @Column(name = "smart_otp_secret", length = 255)
    private String smartOtpSecret;

    @Column(name = "smart_otp_pin_hash", length = 255)
    private String smartOtpPinHash;

    @Column(name = "target_destination", length = 150)
    private String targetDestination;

    @Column(name = "tx_context_hash", length = 128)
    private String txContextHash;

    @Column(name = "client_ip", length = 50)
    private String clientIp;

    @Column(name = "attempt_count", nullable = false)
    @Builder.Default
    private Integer attemptCount = 0;

    @Column(name = "max_attempts", nullable = false)
    @Builder.Default
    private Integer maxAttempts = 3;

    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private String status = "PENDING"; // PENDING, VERIFIED, EXPIRED, BLOCKED, BYPASSED

    @Column(name = "reference_id", length = 64)
    private String referenceId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
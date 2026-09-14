package com.liochio.otp.service;

import com.liochio.common.dto.otp.OtpVerifyResponse;
import com.liochio.common.dto.otp.SmartOtpSetupRequest;
import com.liochio.common.dto.otp.SmartOtpSetupResponse;
import com.liochio.common.dto.otp.SmartOtpVerifyRequest;
import com.liochio.common.exception.AppException;
import com.liochio.common.exception.ErrorCode;
import com.liochio.common.security.TotpProvider;
import com.liochio.common.utils.SecurityUtils;
import com.liochio.otp.entity.OtpVerificationEntity;
import com.liochio.otp.repository.OtpVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DedicatedSmartOtpService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DedicatedSmartOtpService.class);

    private final TotpProvider totpProvider;
    private final OtpVerificationRepository otpRepository;
    private final OtpConfigService otpConfigService;

    private static final String ISSUER = "LiochioFintech";

    @Transactional
    public SmartOtpSetupResponse setupSmartOtp(SmartOtpSetupRequest request) {
        String tenantId = (request.getTenantId() != null && !request.getTenantId().isBlank()) ? request.getTenantId() : "SYSTEM";
        String base32Secret = totpProvider.generateBase32Secret();
        String qrBarcodeUri = totpProvider.generateQrBarcodeUri(base32Secret, request.getUsername(), ISSUER);

        OtpVerificationEntity entity = OtpVerificationEntity.builder()
                .tenantId(tenantId)
                .userId(request.getUserId())
                .otpType("SMART_OTP")
                .otpPurpose("SMART_OTP_ENROLL")
                .smartOtpSecret(base32Secret)
                .status("PENDING")
                .attemptCount(0)
                .maxAttempts(5)
                .expiresAt(Instant.now().plus(600, ChronoUnit.SECONDS)) // 10 phút để quét và kích hoạt
                .createdAt(Instant.now())
                .build();

        otpRepository.save(entity);
        log.info("[DedicatedSmartOtpService] Khởi tạo thiết lập SmartOTP cho User ID: {}", request.getUserId());

        return SmartOtpSetupResponse.builder()
                .secret(base32Secret)
                .qrBarcodeUri(qrBarcodeUri)
                .issuer(ISSUER)
                .accountName(request.getUsername())
                .build();
    }

    @Transactional
    public OtpVerifyResponse verifyAndEnrollSmartOtp(SmartOtpVerifyRequest request) {
        String tenantId = (request.getTenantId() != null && !request.getTenantId().isBlank()) ? request.getTenantId() : "SYSTEM";
        boolean isBypassed = otpConfigService.isVerificationBypassed(tenantId);
        Instant now = Instant.now();

        // 1. Kiểm tra nếu chế độ Bypass đang BẬT
        if (isBypassed) {
            log.info("[DedicatedSmartOtpService] [DEV BYPASS ACTIVE] Chấp thuận kích hoạt SmartOTP tự động cho User ID: {}", request.getUserId());

            otpRepository.findTopByTenantIdAndUserIdAndOtpPurposeAndStatusOrderByCreatedAtDesc(
                    tenantId, request.getUserId(), "SMART_OTP_ENROLL", "PENDING"
            ).ifPresent(entity -> {
                entity.setStatus("VERIFIED");
                entity.setVerifiedAt(now);
                if (request.getPin() != null && !request.getPin().isBlank()) {
                    entity.setSmartOtpPinHash(SecurityUtils.encodePassword(request.getPin()));
                }
                otpRepository.save(entity);
            });

            return OtpVerifyResponse.builder()
                    .success(true)
                    .isBypassed(true)
                    .message("SmartOTP activation bypassed for dev environment")
                    .verifiedAt(now)
                    .build();
        }

        // 2. Chế độ xác thực đầy đủ (Strict Mode)
        Optional<OtpVerificationEntity> pendingOpt = otpRepository.findTopByTenantIdAndUserIdAndOtpPurposeAndStatusOrderByCreatedAtDesc(
                tenantId, request.getUserId(), "SMART_OTP_ENROLL", "PENDING"
        );

        if (pendingOpt.isEmpty() || pendingOpt.get().getExpiresAt().isBefore(now)) {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        OtpVerificationEntity entity = pendingOpt.get();
        boolean isValid = totpProvider.verifyTotpCode(entity.getSmartOtpSecret(), request.getOtpCode());

        if (!isValid) {
            entity.setAttemptCount(entity.getAttemptCount() + 1);
            otpRepository.save(entity);
            throw new AppException(ErrorCode.SMART_OTP_INVALID_CODE);
        }

        entity.setStatus("VERIFIED");
        entity.setVerifiedAt(now);
        if (request.getPin() != null && !request.getPin().isBlank()) {
            entity.setSmartOtpPinHash(SecurityUtils.encodePassword(request.getPin()));
        }
        otpRepository.save(entity);

        log.info("[DedicatedSmartOtpService] Kích hoạt SmartOTP thành công cho User ID: {}", request.getUserId());

        return OtpVerifyResponse.builder()
                .success(true)
                .isBypassed(false)
                .message("SmartOTP activated successfully")
                .verifiedAt(now)
                .build();
    }
}

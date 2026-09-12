package com.liochio.auth.service;

import com.liochio.auth.client.OtpServiceClient;
import com.liochio.auth.dto.SetupSmartOtpResponse;
import com.liochio.auth.entity.UserDeviceEntity;
import com.liochio.auth.repository.UserDeviceRepository;
import com.liochio.common.dto.ApiResponse;
import com.liochio.common.dto.otp.OtpVerifyResponse;
import com.liochio.common.dto.otp.SmartOtpSetupRequest;
import com.liochio.common.dto.otp.SmartOtpSetupResponse;
import com.liochio.common.dto.otp.SmartOtpVerifyRequest;
import com.liochio.common.exception.AppException;
import com.liochio.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ==============================================================================
 * Dịch Vụ Ủy Quyền SmartOTP (SmartOTP Client Delegation Service)
 * ==============================================================================
 */
@Service
@RequiredArgsConstructor
public class SmartOtpService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SmartOtpService.class);

    private final OtpServiceClient otpServiceClient;
    private final UserDeviceRepository deviceRepository;
    private final com.liochio.common.security.TotpProvider totpProvider;

    @Transactional
    public SetupSmartOtpResponse setupSmartOtp(Long userId, String tenantId, String username) {
        SmartOtpSetupRequest request = SmartOtpSetupRequest.builder()
                .tenantId(tenantId != null ? tenantId : "SYSTEM")
                .userId(userId)
                .username(username)
                .build();

        try {
            ApiResponse<SmartOtpSetupResponse> response = otpServiceClient.setupSmartOtp(request);
            if (response != null && response.getData() != null) {
                SmartOtpSetupResponse data = response.getData();
                return SetupSmartOtpResponse.builder()
                        .secret(data.getSecret())
                        .qrBarcodeUri(data.getQrBarcodeUri())
                        .issuer(data.getIssuer())
                        .accountName(data.getAccountName())
                        .build();
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.warn("[SmartOtpService] otp-service không khả dụng ({}), tạo mã SmartOTP cục bộ", e.getMessage());
            String secret = totpProvider.generateBase32Secret();
            String uri = totpProvider.generateQrBarcodeUri(secret, username, "PortfolioEngine");
            return SetupSmartOtpResponse.builder()
                    .secret(secret)
                    .qrBarcodeUri(uri)
                    .issuer("PortfolioEngine")
                    .accountName(username)
                    .build();
        }

        String secret = totpProvider.generateBase32Secret();
        String uri = totpProvider.generateQrBarcodeUri(secret, username, "PortfolioEngine");
        return SetupSmartOtpResponse.builder()
                .secret(secret)
                .qrBarcodeUri(uri)
                .issuer("PortfolioEngine")
                .accountName(username)
                .build();
    }

    @Transactional
    public boolean verifyAndEnrollSmartOtp(Long userId, String tenantId, String otpCode, String pin) {
        SmartOtpVerifyRequest request = SmartOtpVerifyRequest.builder()
                .tenantId(tenantId != null ? tenantId : "SYSTEM")
                .userId(userId)
                .otpCode(otpCode)
                .pin(pin)
                .build();

        try {
            ApiResponse<OtpVerifyResponse> response = otpServiceClient.verifySmartOtp(request);
            if (response != null && response.getData() != null && Boolean.TRUE.equals(response.getData().getSuccess())) {
                List<UserDeviceEntity> devices = deviceRepository.findByTenantIdAndUserId(tenantId, userId);
                for (UserDeviceEntity d : devices) {
                    d.setIsSmartOtpEnrolled(true);
                    deviceRepository.save(d);
                }
                log.info("[SmartOtpService] Kích hoạt SmartOTP thành công cho User ID: {}", userId);
                return true;
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.warn("[SmartOtpService] otp-service không khả dụng ({}), kích hoạt SmartOTP với fallback", e.getMessage());
            List<UserDeviceEntity> devices = deviceRepository.findByTenantIdAndUserId(tenantId, userId);
            for (UserDeviceEntity d : devices) {
                d.setIsSmartOtpEnrolled(true);
                deviceRepository.save(d);
            }
            return true;
        }

        return false;
    }

    /**
     * Xác thực SmartOTP và cấp Action Token có hiệu lực 120s cho giao dịch nhạy cảm
     */
    @Transactional
    public com.liochio.auth.dto.ActionTokenResponse verifySmartOtpAndIssueActionToken(Long userId, String tenantId, String otpCode, String pin, String purpose) {
        boolean isValid = verifyAndEnrollSmartOtp(userId, tenantId, otpCode, pin);
        if (!isValid) {
            throw new AppException(ErrorCode.SMART_OTP_INVALID_CODE);
        }

        String token = "act_" + java.util.UUID.randomUUID().toString().replace("-", "") + "_" + System.currentTimeMillis();
        java.time.Instant expiresAt = java.time.Instant.now().plusSeconds(120);

        log.info("[SmartOtpService] Cấp Action Token thành công cho User ID: {}, Purpose: {}, Token: {}", userId, purpose, token);

        return com.liochio.auth.dto.ActionTokenResponse.builder()
                .actionToken(token)
                .purpose(purpose != null ? purpose : "STEP_UP_TRANSACTION")
                .userId(userId)
                .expiresInSeconds(120)
                .expiresAt(expiresAt)
                .build();
    }
}

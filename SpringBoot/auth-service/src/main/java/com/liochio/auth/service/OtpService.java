package com.liochio.auth.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.liochio.auth.client.OtpServiceClient;
import com.liochio.common.dto.ApiResponse;
import com.liochio.common.dto.otp.OtpGenerateRequest;
import com.liochio.common.dto.otp.OtpGenerateResponse;
import com.liochio.common.dto.otp.OtpVerifyRequest;
import com.liochio.common.dto.otp.OtpVerifyResponse;
import com.liochio.common.exception.AppException;
import com.liochio.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * ==============================================================================
 * Dịch Vụ Ủy Quyền OTP Tập Trung (Centralized OTP Client Delegation Service)
 * ==============================================================================
 * 
 * Toàn bộ vòng đời sinh mã, lưu trữ CSDL (liochio_otp_db), TTL 5 phút,
 * đếm số lần thử và xác thực OTP/SmartOTP được quản lý tập trung 100% tại 'otp-service'.
 * Kèm theo cơ chế Fallback L1/L2 tự động nếu 'otp-service' chưa khởi chạy.
 */
@Slf4j
@Service
public class OtpService {

    private final OtpServiceClient otpServiceClient;
    private final Optional<StringRedisTemplate> redisTemplate;

    // Fallback cache TTL 5 phút khi otp-service tạm thời gián đoạn
    private final Cache<String, String> fallbackOtpCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    private static final SecureRandom RANDOM = new SecureRandom();

    public OtpService(
            OtpServiceClient otpServiceClient,
            @Autowired(required = false) StringRedisTemplate redisTemplate
    ) {
        this.otpServiceClient = otpServiceClient;
        this.redisTemplate = Optional.ofNullable(redisTemplate);
    }

    /**
     * Ủy quyền cho otp-service sinh mã OTP ngẫu nhiên và lưu vào liochio_otp_db.
     * Nếu otp-service chưa khởi động hoặc gặp sự cố, tự động fallback an toàn để không làm gián đoạn luồng người dùng.
     */
    public String generateAndSaveOtp(String tenantId, Long userId, String purpose, String destination, String otpType) {
        String safeTenant = (tenantId != null && !tenantId.isBlank()) ? tenantId : "SYSTEM";
        String channel = (otpType != null && !otpType.isBlank()) ? otpType : "EMAIL";

        try {
            OtpGenerateRequest request = OtpGenerateRequest.builder()
                    .tenantId(safeTenant)
                    .userId(userId)
                    .purpose(purpose)
                    .destination(destination)
                    .otpType(channel)
                    .build();

            ApiResponse<OtpGenerateResponse> response = otpServiceClient.generateOtp(request);
            if (response != null && response.getData() != null) {
                OtpGenerateResponse data = response.getData();
                return data.getDevBypassCode() != null && !data.getDevBypassCode().isBlank()
                        ? data.getDevBypassCode()
                        : data.getReferenceId();
            }
        } catch (Exception e) {
            log.warn("[OtpService] otp-service (8094) không phản hồi ({}), kích hoạt Fallback OTP Mode", e.getMessage());

            // Sinh mã OTP 6 số fallback
            String fallbackCode = String.format("%06d", RANDOM.nextInt(1_000_000));
            String cacheKey = "fallback:otp:" + safeTenant + ":" + userId + ":" + purpose;

            fallbackOtpCache.put(cacheKey, fallbackCode);
            redisTemplate.ifPresent(rt -> {
                try {
                    rt.opsForValue().set(cacheKey, fallbackCode, 300, TimeUnit.SECONDS);
                } catch (Exception ignored) {}
            });

            log.info("[OtpService] [FALLBACK_OTP_GENERATED] Mã OTP cho User ID={}: {}", userId, fallbackCode);
            return fallbackCode;
        }

        return null;
    }

    /**
     * Ủy quyền cho otp-service xác thực mã OTP (kèm fallback nếu otp-service offline)
     */
    public boolean verifyOtp(String tenantId, Long userId, String purpose, String rawOtp) {
        String safeTenant = (tenantId != null && !tenantId.isBlank()) ? tenantId : "SYSTEM";

        if (rawOtp == null || rawOtp.trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_OTP, "Mã OTP không được để trống");
        }
        String cleanOtp = rawOtp.trim();

        // 1. Thử gọi qua otp-service tập trung
        try {
            OtpVerifyRequest request = OtpVerifyRequest.builder()
                    .tenantId(safeTenant)
                    .userId(userId)
                    .purpose(purpose)
                    .otpCode(cleanOtp)
                    .build();

            ApiResponse<OtpVerifyResponse> response = otpServiceClient.verifyOtp(request);
            if (response != null && response.getData() != null && Boolean.TRUE.equals(response.getData().getSuccess())) {
                log.info("[OtpService] Xác thực OTP thành công qua otp-service cho User ID={}, Purpose={}", userId, purpose);
                return true;
            }
        } catch (Exception e) {
            log.warn("[OtpService] otp-service không khả dụng ({}), kiểm tra Fallback OTP Cache", e.getMessage());
        }

        // 2. Kiểm tra Fallback Cache (Local Caffeine & Redis)
        String cacheKey = "fallback:otp:" + safeTenant + ":" + userId + ":" + purpose;
        String cachedOtp = fallbackOtpCache.getIfPresent(cacheKey);

        if (cachedOtp == null && redisTemplate.isPresent()) {
            try {
                cachedOtp = redisTemplate.get().opsForValue().get(cacheKey);
            } catch (Exception ignored) {}
        }

        // Xác thực chính xác với mã OTP thực tế đã sinh
        if (cachedOtp != null && cachedOtp.equals(cleanOtp)) {
            fallbackOtpCache.invalidate(cacheKey);
            redisTemplate.ifPresent(rt -> {
                try {
                    rt.delete(cacheKey);
                } catch (Exception ignored) {}
            });
            log.info("[OtpService] Xác thực OTP thành công qua Fallback Cache cho User ID: {}", userId);
            return true;
        }

        throw new AppException(ErrorCode.INVALID_OTP, "Mã xác thực OTP không chính xác hoặc đã hết hạn");
    }
}

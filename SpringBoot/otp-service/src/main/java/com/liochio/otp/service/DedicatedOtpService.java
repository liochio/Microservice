package com.liochio.otp.service;

import com.liochio.common.dto.otp.OtpGenerateRequest;
import com.liochio.common.dto.otp.OtpGenerateResponse;
import com.liochio.common.dto.otp.OtpVerifyRequest;
import com.liochio.common.dto.otp.OtpVerifyResponse;
import com.liochio.common.exception.AppException;
import com.liochio.common.exception.ErrorCode;
import com.liochio.common.utils.SecurityUtils;
import com.liochio.otp.entity.OtpVerificationEntity;
import com.liochio.otp.repository.OtpVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

/**
 * ==============================================================================
 * Động Cơ Xác Thực OTP Độc Lập Chuẩn Banking (Dedicated OTP Security Service)
 * ==============================================================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DedicatedOtpService {

    private final OtpVerificationRepository otpRepository;
    private final OtpConfigService otpConfigService;
    private final Optional<org.springframework.jdbc.core.JdbcTemplate> jdbcTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    public static final int OTP_EXPIRY_SECONDS = 300; // 5 phút hiệu lực
    public static final int MAX_ATTEMPTS = 3;
    public static final int COOLDOWN_SECONDS = 60; // 60s cooldown chống spam
    public static final int DAILY_OTP_LIMIT = 5; // Tối đa 5 OTP / ngày

    /**
     * Sinh mã OTP 6 số với Cooldown, Quota và Transaction-Bound Security
     */
    @Transactional
    public OtpGenerateResponse generateOtp(OtpGenerateRequest request) {
        String tenantId = (request.getTenantId() != null && !request.getTenantId().isBlank()) ? request.getTenantId() : "SYSTEM";
        boolean isBypassed = otpConfigService.isVerificationBypassed(tenantId);
        String devBypassCode = otpConfigService.getDevBypassCode(tenantId);
        Instant now = Instant.now();

        // 1. Kiểm tra Cooldown Anti-Flood (Chống spam request liên tục)
        if (!isBypassed && request.getDestination() != null && !request.getDestination().isBlank()) {
            Optional<OtpVerificationEntity> lastOtpOpt = otpRepository.findTopByTargetDestinationOrderByCreatedAtDesc(request.getDestination());
            if (lastOtpOpt.isPresent()) {
                long elapsed = ChronoUnit.SECONDS.between(lastOtpOpt.get().getCreatedAt(), now);
                if (elapsed < COOLDOWN_SECONDS) {
                    long waitSeconds = COOLDOWN_SECONDS - elapsed;
                    log.warn("[DedicatedOtpService] Yêu cầu OTP quá nhanh cho destination='{}'. Vui lòng đợi {}s", request.getDestination(), waitSeconds);
                    throw new AppException(ErrorCode.INVALID_REQUEST,
                            "Yêu cầu gửi mã OTP quá nhanh. Vui lòng thử lại sau " + waitSeconds + " giây.");
                }
            }

            // 2. Kiểm tra Daily Quota (Giới hạn tối đa 5 OTP / 24h)
            long dailyCount = otpRepository.countByTargetDestinationAndCreatedAtAfter(
                    request.getDestination(),
                    now.minus(24, ChronoUnit.HOURS)
            );
            if (dailyCount >= DAILY_OTP_LIMIT) {
                log.warn("[DedicatedOtpService] Destination='{}' đã vượt quá hạn mức {} OTP / ngày", request.getDestination(), DAILY_OTP_LIMIT);
                throw new AppException(ErrorCode.OTP_MAX_ATTEMPTS_EXCEEDED,
                        "Bạn đã vượt quá hạn mức nhận " + DAILY_OTP_LIMIT + " mã OTP trong vòng 24 giờ.");
            }
        }

        String rawOtp = isBypassed ? devBypassCode : String.format("%06d", secureRandom.nextInt(1000000));
        String hashedOtp = SecurityUtils.encodePassword(rawOtp);
        String referenceId = "ref_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        Instant expiresAt = now.plus(OTP_EXPIRY_SECONDS, ChronoUnit.SECONDS);

        OtpVerificationEntity entity = OtpVerificationEntity.builder()
                .tenantId(tenantId)
                .userId(request.getUserId())
                .otpType(request.getOtpType() != null ? request.getOtpType() : "EMAIL")
                .otpPurpose(request.getPurpose())
                .otpCodeHash(hashedOtp)
                .targetDestination(request.getDestination())
                .txContextHash(request.getTxContextHash())
                .clientIp(request.getClientIp())
                .attemptCount(0)
                .maxAttempts(MAX_ATTEMPTS)
                .status("PENDING")
                .referenceId(referenceId)
                .expiresAt(expiresAt)
                .createdAt(now)
                .build();

        otpRepository.save(entity);

        log.info("[DedicatedOtpService] Đã phát hành mã OTP cho User ID: {}, Purpose: {}, Reference: {}",
                request.getUserId(), request.getPurpose(), referenceId);

        // Tự động đẩy vào bảng mail_logs của worker-service
        jdbcTemplate.ifPresent(jdbc -> {
            try {
                String subject = "Mã xác thực OTP Liochio: " + rawOtp;
                String bodyContent = "Kính gửi quý khách,\n\nMã OTP xác thực của bạn là: " + rawOtp + "\nMã có hiệu lực trong 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.\n\nTrân trọng,\nLiochio Enterprise Team";
                jdbc.update("INSERT INTO liochio_app_db.mail_logs (trace_id, recipient, channel, template_code, language_code, subject, content, status, execution_time_ms, retry_count, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())",
                        referenceId, request.getDestination(), "EMAIL", "OTP_REGISTER_MAIL", "vi", subject, bodyContent, "PENDING", 0, 0);
                log.info("[DedicatedOtpService] -> Đã tạo bản ghi mail_log PENDING cho mã OTP '{}'", referenceId);
            } catch (Exception e) {
                log.warn("[DedicatedOtpService] Không thể ghi mail_logs: {}", e.getMessage());
            }
        });

        return OtpGenerateResponse.builder()
                .referenceId(referenceId)
                .destination(request.getDestination())
                .expiresInSeconds(OTP_EXPIRY_SECONDS)
                .isBypassed(isBypassed)
                .devBypassCode(rawOtp)
                .expiresAt(expiresAt)
                .build();
    }

    /**
     * Xác thực mã OTP (Kiểm tra Strict Mode, Transaction Context & Brute Force Lock)
     */
    @Transactional
    public OtpVerifyResponse verifyOtp(OtpVerifyRequest request) {
        String tenantId = (request.getTenantId() != null && !request.getTenantId().isBlank()) ? request.getTenantId() : "SYSTEM";
        boolean isBypassed = otpConfigService.isVerificationBypassed(tenantId);
        Instant now = Instant.now();

        // 1. Kiểm tra nếu chế độ Bypass đang BẬT
        if (isBypassed) {
            log.info("[DedicatedOtpService] [DEV BYPASS ACTIVE] Chấp thuận xác thực OTP tự động cho User ID: {}, Purpose: {}",
                    request.getUserId(), request.getPurpose());

            otpRepository.findTopByTenantIdAndUserIdAndOtpPurposeAndStatusOrderByCreatedAtDesc(
                    tenantId, request.getUserId(), request.getPurpose(), "PENDING"
            ).ifPresent(entity -> {
                entity.setStatus("VERIFIED");
                entity.setVerifiedAt(now);
                otpRepository.save(entity);
            });

            return OtpVerifyResponse.builder()
                    .success(true)
                    .isBypassed(true)
                    .message("OTP verification bypassed for dev environment")
                    .verifiedAt(now)
                    .build();
        }

        // 2. Chế độ xác thực đầy đủ (Strict Mode)
        Optional<OtpVerificationEntity> otpOpt = otpRepository.findTopByTenantIdAndUserIdAndOtpPurposeAndStatusOrderByCreatedAtDesc(
                tenantId, request.getUserId(), request.getPurpose(), "PENDING"
        );

        if (otpOpt.isEmpty()) {
            log.warn("[DedicatedOtpService] Không tìm thấy OTP pending cho User ID: {}, Purpose: {}", request.getUserId(), request.getPurpose());
            throw new AppException(ErrorCode.INVALID_OTP, "Mã OTP không tồn tại hoặc đã được sử dụng");
        }

        OtpVerificationEntity entity = otpOpt.get();

        if (entity.getExpiresAt().isBefore(now)) {
            entity.setStatus("EXPIRED");
            otpRepository.save(entity);
            throw new AppException(ErrorCode.OTP_EXPIRED, "Mã OTP đã hết hạn (Vui lòng yêu cầu gửi lại mã mới)");
        }

        if (entity.getAttemptCount() >= entity.getMaxAttempts()) {
            entity.setStatus("BLOCKED");
            otpRepository.save(entity);
            throw new AppException(ErrorCode.OTP_MAX_ATTEMPTS_EXCEEDED, "Bạn đã nhập sai OTP quá 3 lần. Mã OTP đã bị vô hiệu hóa.");
        }

        // 3. Kiểm tra ràng buộc ngữ cảnh giao dịch (Transaction-Bound Context Check)
        if (entity.getTxContextHash() != null && !entity.getTxContextHash().isBlank()) {
            if (request.getTxContextHash() == null || !entity.getTxContextHash().equalsIgnoreCase(request.getTxContextHash())) {
                log.warn("[DedicatedOtpService] Transaction Context Hash không khớp! Expected: {}, Actual: {}",
                        entity.getTxContextHash(), request.getTxContextHash());
                entity.setAttemptCount(entity.getAttemptCount() + 1);
                otpRepository.save(entity);
                throw new AppException(ErrorCode.INVALID_OTP, "Chữ ký ngữ cảnh giao dịch không khớp. Thao tác bị từ chối.");
            }
        }

        entity.setAttemptCount(entity.getAttemptCount() + 1);

        if (!SecurityUtils.matchesPassword(request.getOtpCode(), entity.getOtpCodeHash())) {
            otpRepository.save(entity);
            int remaining = entity.getMaxAttempts() - entity.getAttemptCount();
            throw new AppException(ErrorCode.INVALID_OTP, "Mã OTP không chính xác. Bạn còn " + remaining + " lần thử.");
        }

        entity.setStatus("VERIFIED");
        entity.setVerifiedAt(now);
        otpRepository.save(entity);
        log.info("[DedicatedOtpService] Xác thực OTP thành công cho User ID: {}, Purpose: {}", request.getUserId(), request.getPurpose());

        return OtpVerifyResponse.builder()
                .success(true)
                .isBypassed(false)
                .message("Xác thực mã OTP thành công")
                .verifiedAt(now)
                .build();
    }
}
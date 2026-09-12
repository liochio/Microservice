package com.liochio.auth.service;

import com.liochio.auth.entity.SecurityLoginHistoryEntity;
import com.liochio.auth.entity.UserEntity;
import com.liochio.auth.repository.SecurityLoginHistoryRepository;
import com.liochio.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * ==============================================================================
 * Dịch Vụ Kiểm Soát Đăng Nhập & Bảo Mật Độc Lập (Login Security & Audit Service)
 * ==============================================================================
 * 
 * Sử dụng `Propagation.REQUIRES_NEW` để đảm bảo:
 * - Ghi nhận 100% lịch sử đăng nhập thành công và thất bại vào bảng `security_login_histories`.
 * - Tăng và lưu biến đếm `failed_login_attempts` trong bảng `users` ngay cả khi API ném Exception.
 * - Khóa tài khoản 15 phút (Brute-force protection) độc lập với transaction chính.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginSecurityService {

    private final UserRepository userRepository;
    private final SecurityLoginHistoryRepository loginHistoryRepository;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;

    /**
     * Ghi nhận đăng nhập thất bại và cập nhật bộ đếm Brute-force trong transaction độc lập
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int recordFailedLogin(String tenantId, String username, String ip, String userAgent, String reason) {
        String safeTenant = (tenantId != null && !tenantId.isBlank()) ? tenantId : "SYSTEM";
        int currentAttempts = 1;
        Long userId = null;

        try {
            var userOpt = userRepository.findByUsernameAndTenantId(username, safeTenant)
                    .or(() -> userRepository.findByUsername(username));

            if (userOpt.isPresent()) {
                UserEntity user = userOpt.get();
                userId = user.getId();
                currentAttempts = (user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0) + 1;
                user.setFailedLoginAttempts(currentAttempts);

                String historyStatus = "WRONG_PASSWORD";
                String finalReason = reason + " (Lần thử " + currentAttempts + "/" + MAX_FAILED_ATTEMPTS + ")";

                if (currentAttempts >= MAX_FAILED_ATTEMPTS) {
                    user.setLockoutUntil(Instant.now().plus(LOCKOUT_DURATION_MINUTES, ChronoUnit.MINUTES));
                    historyStatus = "LOCKED";
                    finalReason = "Đăng nhập sai quá " + MAX_FAILED_ATTEMPTS + " lần. Tài khoản bị tạm khóa 15 phút.";
                    log.warn("[LoginSecurityService] Tài khoản '{}' đã bị khóa 15 phút do nhập sai mật khẩu 5 lần!", username);
                }

                userRepository.save(user);

                saveHistory(safeTenant, userId, username, ip, userAgent, historyStatus, finalReason, 60);
            } else {
                saveHistory(safeTenant, null, username, ip, userAgent, "UNKNOWN_USER", "Tài khoản không tồn tại trong hệ thống", 80);
            }
        } catch (Exception e) {
            log.error("[LoginSecurityService] Lỗi khi lưu failed login history: {}", e.getMessage());
        }

        return currentAttempts;
    }

    /**
     * Ghi nhận đăng nhập thành công và reset bộ đếm trong transaction độc lập
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSuccessfulLogin(String tenantId, Long userId, String username, String ip, String userAgent) {
        String safeTenant = (tenantId != null && !tenantId.isBlank()) ? tenantId : "SYSTEM";
        try {
            if (userId != null) {
                userRepository.findById(userId).ifPresent(user -> {
                    user.setFailedLoginAttempts(0);
                    user.setLockoutUntil(null);
                    userRepository.save(user);
                });
            }
            saveHistory(safeTenant, userId, username, ip, userAgent, "SUCCESS", "Đăng nhập thành công", 0);
        } catch (Exception e) {
            log.error("[LoginSecurityService] Lỗi khi lưu success login history: {}", e.getMessage());
        }
    }

    /**
     * Ghi nhận sự kiện thách thức 2FA thiết bị lạ
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordDeviceChallenge(String tenantId, Long userId, String username, String ip, String userAgent, String deviceId) {
        String safeTenant = (tenantId != null && !tenantId.isBlank()) ? tenantId : "SYSTEM";
        saveHistory(safeTenant, userId, username, ip, userAgent, "UNTRUSTED_DEVICE_CHALLENGE", "Phát hiện thiết bị lạ: " + deviceId, 40);
    }

    private void saveHistory(String tenantId, Long userId, String username, String ip, String userAgent, String status, String reason, int riskScore) {
        try {
            SecurityLoginHistoryEntity history = SecurityLoginHistoryEntity.builder()
                    .tenantId(tenantId)
                    .userId(userId)
                    .attemptedUsername(username)
                    .ipAddress(ip != null ? ip : "127.0.0.1")
                    .userAgent(userAgent != null && userAgent.length() > 500 ? userAgent.substring(0, 500) : (userAgent != null ? userAgent : "Unknown"))
                    .loginStatus(status)
                    .failureReason(reason)
                    .riskScore(riskScore)
                    .createdAt(Instant.now())
                    .build();

            loginHistoryRepository.save(history);
            log.info("[LoginSecurityService] Đã ghi nhận lịch sử bảo mật: User='{}', Status='{}', Reason='{}'", username, status, reason);
        } catch (Exception e) {
            log.warn("[LoginSecurityService] Không thể ghi SecurityLoginHistory: {}", e.getMessage());
        }
    }
}

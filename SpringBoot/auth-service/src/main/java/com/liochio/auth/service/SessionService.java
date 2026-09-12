package com.liochio.auth.service;

import com.liochio.auth.dto.ActiveSessionResponse;
import com.liochio.auth.entity.UserSessionEntity;
import com.liochio.auth.repository.UserSessionRepository;
import com.liochio.common.constant.SecurityConstants;
import com.liochio.common.exception.AppException;
import com.liochio.common.exception.ErrorCode;
import com.liochio.common.security.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ==============================================================================
 * Dịch Vụ Quản Lý Phiên Đăng Nhập & Xoay Vòng Token (Session & Rotation Service)
 * ==============================================================================
 * 
 * Mục đích:
 * - Theo dõi các phiên đăng nhập hoạt động (Active Sessions) của từng người dùng.
 * - Cơ chế phát hiện tái sử dụng Token (Token Reuse Detection): Nếu Refresh Token cũ
 *   đã từng bị thu hồi được gửi lên, hệ thống sẽ kích hoạt Family Revocation lập tức
 *   thu hồi toàn bộ phiên của tài khoản đó.
 * - Đồng bộ tức thời danh sách thu hồi vào Redis Blacklist phân tán.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final UserSessionRepository sessionRepository;
    private final TokenBlacklistService tokenBlacklistService;

    @Transactional
    public String createSession(String tenantId, Long userId, String deviceId, String refreshToken, String ip, String userAgent) {
        String sessionId = "sess_" + UUID.randomUUID().toString().replace("-", "");
        String tokenHash = hashToken(refreshToken);

        UserSessionEntity session = UserSessionEntity.builder()
                .id(sessionId)
                .tenantId(tenantId != null ? tenantId : "SYSTEM")
                .userId(userId)
                .deviceId(deviceId != null ? deviceId : "unknown_device")
                .refreshTokenHash(tokenHash)
                .ipAddress(ip != null ? ip : "127.0.0.1")
                .userAgent(userAgent != null && userAgent.length() > 500 ? userAgent.substring(0, 500) : (userAgent != null ? userAgent : "Unknown"))
                .isRevoked(false)
                .expiresAt(Instant.now().plusMillis(SecurityConstants.REFRESH_TOKEN_VALIDITY_MS))
                .createdAt(Instant.now())
                .lastAccessedAt(Instant.now())
                .build();

        sessionRepository.save(session);
        log.info("[SessionService] Tạo phiên mới: SessionID={}, UserID={}, DeviceID={}", sessionId, userId, deviceId);
        return sessionId;
    }

    @Transactional
    public RotateResult rotateSession(String oldRefreshToken, String newRefreshToken, String ip, String userAgent, String deviceId) {
        String oldHash = hashToken(oldRefreshToken);
        UserSessionEntity session = sessionRepository.findFirstByRefreshTokenHashOrderByCreatedAtDesc(oldHash)
                .orElseThrow(() -> {
                    log.warn("[SessionService] Refresh Token không tồn tại trong cơ sở dữ liệu");
                    return new AppException(ErrorCode.TOKEN_INVALID);
                });

        // 1. Kiểm tra Token Reuse Detection
        if (Boolean.TRUE.equals(session.getIsRevoked())) {
            log.error("[SECURITY ALERT] PHÁT HIỆN REFRESH TOKEN ĐÃ THU HỒI BỊ TÁI SỬ DỤNG! User ID: {}, Session: {}", session.getUserId(), session.getId());
            // Kích hoạt Family Revocation: Thu hồi toàn bộ phiên của User này
            sessionRepository.revokeAllByUserId(session.getUserId(), "TOKEN_REUSE_DETECTED");
            tokenBlacklistService.blacklistUser(session.getUserId(), Duration.ofDays(7));
            throw new AppException(ErrorCode.REFRESH_TOKEN_REUSED);
        }

        // 2. Kiểm tra hạn sống
        if (session.getExpiresAt().isBefore(Instant.now())) {
            session.setIsRevoked(true);
            session.setRevokedReason("EXPIRED");
            sessionRepository.save(session);
            tokenBlacklistService.blacklistSession(session.getId(), Duration.ofDays(7), "EXPIRED");
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        // 3. Đánh dấu thu hồi token cũ do đã xoay vòng
        session.setIsRevoked(true);
        session.setRevokedReason("TOKEN_ROTATED");
        session.setLastAccessedAt(Instant.now());
        sessionRepository.save(session);
        tokenBlacklistService.blacklistSession(session.getId(), Duration.ofDays(7), "TOKEN_ROTATED");

        // 4. Tạo phiên mới cho Token mới
        String newDeviceId = (deviceId != null && !deviceId.isBlank()) ? deviceId : session.getDeviceId();
        String newSessionId = createSession(session.getTenantId(), session.getUserId(), newDeviceId, newRefreshToken, ip, userAgent);

        return new RotateResult(session.getTenantId(), session.getUserId(), newSessionId, newDeviceId);
    }

    @Transactional
    public void revokeSession(String sessionId, String reason) {
        sessionRepository.findById(sessionId).ifPresent(s -> {
            s.setIsRevoked(true);
            s.setRevokedReason(reason != null ? reason : "LOGOUT");
            sessionRepository.save(s);
            tokenBlacklistService.blacklistSession(sessionId, Duration.ofDays(7), reason);
            log.info("[SessionService] Đã thu hồi phiên: ID={}, Reason={}", sessionId, reason);
        });
    }

    @Transactional
    public void kickOutOtherMobileSessions(Long userId, String currentDeviceId) {
        List<UserSessionEntity> activeSessions = sessionRepository.findByUserIdAndIsRevokedFalse(userId);
        for (UserSessionEntity s : activeSessions) {
            if (s.getDeviceId() != null && !s.getDeviceId().equals(currentDeviceId)) {
                s.setIsRevoked(true);
                s.setRevokedReason("CONCURRENT_SESSION_KICK_OUT");
                sessionRepository.save(s);
                tokenBlacklistService.blacklistSession(s.getId(), Duration.ofDays(7), "CONCURRENT_SESSION_KICK_OUT");
                log.warn("[SessionService] [CONCURRENT KICK-OUT] Đã thu hồi phiên cũ {} của User ID: {} do đăng nhập trên thiết bị mới {}",
                        s.getId(), userId, currentDeviceId);
            }
        }
    }

    @Transactional
    public void revokeAllUserSessions(Long userId, String reason) {
        tokenBlacklistService.blacklistUser(userId, Duration.ofDays(7));
        int count = sessionRepository.revokeAllByUserId(userId, reason != null ? reason : "FORCE_LOGOUT");
        log.info("[SessionService] Đã thu hồi {} phiên của User ID: {}", count, userId);
    }

    @Transactional(readOnly = true)
    public List<ActiveSessionResponse> getUserActiveSessions(String tenantId, Long userId, String currentSessionId) {
        return sessionRepository.findByTenantIdAndUserIdAndIsRevokedFalse(tenantId, userId).stream()
                .map(s -> ActiveSessionResponse.builder()
                        .id(s.getId())
                        .deviceId(s.getDeviceId())
                        .ipAddress(s.getIpAddress())
                        .userAgent(s.getUserAgent())
                        .isCurrentSession(s.getId().equals(currentSessionId))
                        .isRevoked(s.getIsRevoked())
                        .revokedReason(s.getRevokedReason())
                        .expiresAt(s.getExpiresAt())
                        .createdAt(s.getCreatedAt())
                        .lastAccessedAt(s.getLastAccessedAt())
                        .build())
                .collect(Collectors.toList());
    }

    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return String.valueOf(token.hashCode());
        }
    }

    public record RotateResult(String tenantId, Long userId, String newSessionId, String deviceId) {}
}

package com.liochio.auth.service;

import com.liochio.auth.dto.*;
import com.liochio.auth.entity.QrLoginSessionEntity;
import com.liochio.auth.entity.RoleEntity;
import com.liochio.auth.entity.UserEntity;
import com.liochio.auth.repository.QrLoginSessionRepository;
import com.liochio.auth.repository.UserRepository;
import com.liochio.common.constant.SecurityConstants;
import com.liochio.common.dto.ClientContextRequest;
import com.liochio.common.exception.AppException;
import com.liochio.common.exception.ErrorCode;
import com.liochio.common.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ==============================================================================
 * Dịch Vụ Đăng Nhập Quét Mã QR Đồng Bộ Web - App (QR Code Login Service)
 * ==============================================================================
 * 
 * Chuỗi vòng đời (State Machine):
 * 1. Web Init: Sinh sessionId (Hạn 120s), Web lắng nghe WebSocket `/topic/qr-login/{sessionId}`.
 * 2. Mobile Scan: App quét mã -> Cập nhật trạng thái `SCANNED`.
 * 3. Mobile Confirm: App xác thực FaceID/PIN -> Cập nhật trạng thái `CONFIRMED`, cấp `exchangeAuthCode` (Hạn 10s).
 * 4. Web Exchange: Web gửi `exchangeAuthCode` -> Đổi lấy cặp Access Token + Refresh Token hoàn tất.
 */
@Service
@RequiredArgsConstructor
public class QrLoginService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(QrLoginService.class);

    private final QrLoginSessionRepository qrRepository;
    private final UserRepository userRepository;
    private final SessionService sessionService;
    private final DeviceService deviceService;
    private final JwtUtils jwtUtils;

    private static final int QR_SESSION_TTL_SECONDS = 120;
    private static final int EXCHANGE_CODE_TTL_SECONDS = 10;

    @Transactional
    public QrInitResponse initQrSession(String tenantId, ClientContextRequest context) {
        String sessionId = "qr_" + UUID.randomUUID().toString().replace("-", "");
        Instant expiresAt = Instant.now().plus(QR_SESSION_TTL_SECONDS, ChronoUnit.SECONDS);

        QrLoginSessionEntity session = QrLoginSessionEntity.builder()
                .id(sessionId)
                .tenantId(tenantId != null ? tenantId : "SYSTEM")
                .webDeviceId(context.getDeviceId() != null ? context.getDeviceId() : "web_unknown")
                .webIpAddress(context.getClientIp() != null ? context.getClientIp() : "127.0.0.1")
                .webUserAgent(context.getUserAgent() != null ? context.getUserAgent() : "Web Browser")
                .status("PENDING")
                .expiresAt(expiresAt)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        qrRepository.save(session);
        log.info("[QrLoginService] Khởi tạo phiên QR Login: ID={}, WebDevice={}", sessionId, session.getWebDeviceId());

        return QrInitResponse.builder()
                .sessionId(sessionId)
                .qrCodeUri("portfolio://qr-login?session=" + sessionId)
                .wsTopic("/topic/qr-login/" + sessionId)
                .expiresInSeconds(QR_SESSION_TTL_SECONDS)
                .expiresAt(expiresAt)
                .build();
    }

    @Transactional
    public boolean scanQrSession(QrScanRequest request, Long mobileUserId) {
        QrLoginSessionEntity session = qrRepository.findById(request.getSessionId())
                .orElseThrow(() -> new AppException(ErrorCode.QR_SESSION_INVALID));

        if (session.getExpiresAt().isBefore(Instant.now())) {
            session.setStatus("EXPIRED");
            qrRepository.save(session);
            throw new AppException(ErrorCode.QR_SESSION_EXPIRED);
        }

        if (!"PENDING".equalsIgnoreCase(session.getStatus())) {
            throw new AppException(ErrorCode.QR_SESSION_INVALID);
        }

        session.setStatus("SCANNED");
        session.setMobileUserId(mobileUserId);
        session.setMobileDeviceId(request.getMobileDeviceId());
        session.setUpdatedAt(Instant.now());
        qrRepository.save(session);

        log.info("[QrLoginService] App Mobile đã quét QR: Session={}, User={}", session.getId(), mobileUserId);
        return true;
    }

    @Transactional
    public String confirmQrSession(QrConfirmRequest request, Long mobileUserId) {
        QrLoginSessionEntity session = qrRepository.findById(request.getSessionId())
                .orElseThrow(() -> new AppException(ErrorCode.QR_SESSION_INVALID));

        if (session.getExpiresAt().isBefore(Instant.now())) {
            session.setStatus("EXPIRED");
            qrRepository.save(session);
            throw new AppException(ErrorCode.QR_SESSION_EXPIRED);
        }

        String exchangeCode = "exc_" + UUID.randomUUID().toString().replace("-", "");

        session.setStatus("CONFIRMED");
        session.setMobileUserId(mobileUserId);
        session.setExchangeAuthCode(exchangeCode);
        session.setExpiresAt(Instant.now().plus(EXCHANGE_CODE_TTL_SECONDS, ChronoUnit.SECONDS)); // Chỉ có 10 giây để Web đổi
        session.setUpdatedAt(Instant.now());
        qrRepository.save(session);

        log.info("[QrLoginService] Người dùng xác nhận đăng nhập QR: Session={}, User={}, ExchangeCode={}", session.getId(), mobileUserId, exchangeCode);
        return exchangeCode;
    }

    @Transactional
    public TokenResponse exchangeQrCode(QrExchangeRequest request, ClientContextRequest context) {
        QrLoginSessionEntity session = qrRepository.findByExchangeAuthCode(request.getExchangeAuthCode())
                .orElseThrow(() -> new AppException(ErrorCode.QR_SESSION_INVALID));

        if (!session.getId().equals(request.getSessionId()) || session.getExpiresAt().isBefore(Instant.now())) {
            session.setStatus("EXPIRED");
            qrRepository.save(session);
            throw new AppException(ErrorCode.QR_SESSION_EXPIRED);
        }

        Long userId = session.getMobileUserId();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Thu hồi mã đổi Token (Single-Use)
        session.setStatus("EXPIRED");
        session.setExchangeAuthCode(null);
        session.setUpdatedAt(Instant.now());
        qrRepository.save(session);

        // Ghi nhận thiết bị Web
        context.setDeviceId(request.getWebDeviceId());
        deviceService.getOrCreateDevice(user.getTenantId(), user.getId(), context);

        // Sinh Token JWT cho Web
        Set<String> roles = user.getRoles().stream()
                .map(RoleEntity::getRoleName)
                .collect(Collectors.toSet());

        Set<String> permissions = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> p.getPermissionCode())
                .collect(Collectors.toSet());

        String newRefreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getUsername(), user.getTenantId());
        String sessionId = sessionService.createSession(user.getTenantId(), user.getId(), request.getWebDeviceId(), newRefreshToken, context.getClientIp(), context.getUserAgent());
        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getUsername(), user.getTenantId(), roles, permissions, request.getWebDeviceId(), sessionId);

        log.info("[QrLoginService] Web đổi Token thành công qua QR: User={}, WebDevice={}, Session={}", user.getUsername(), request.getWebDeviceId(), sessionId);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(SecurityConstants.ACCESS_TOKEN_VALIDITY_MS / 1000)
                .userId(user.getId())
                .username(user.getUsername())
                .tenantId(user.getTenantId())
                .roles(roles)
                .permissions(permissions)
                .requires2Fa(false)
                .deviceTrusted(true)
                .deviceId(request.getWebDeviceId())
                .sessionId(sessionId)
                .issuedAt(Instant.now())
                .build();
    }
}

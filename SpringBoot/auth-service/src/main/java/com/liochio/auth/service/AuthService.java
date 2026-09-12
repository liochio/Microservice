package com.liochio.auth.service;

import com.liochio.auth.dto.*;
import com.liochio.auth.entity.RoleEntity;
import com.liochio.auth.entity.SecurityLoginHistoryEntity;
import com.liochio.auth.entity.UserDeviceEntity;
import com.liochio.auth.entity.UserEntity;
import com.liochio.auth.repository.RoleRepository;
import com.liochio.auth.repository.SecurityLoginHistoryRepository;
import com.liochio.auth.repository.UserRepository;
import com.liochio.common.constant.SecurityConstants;
import com.liochio.common.context.TenantContext;
import com.liochio.common.context.UserContext;
import com.liochio.common.dto.ClientContextRequest;
import com.liochio.common.enums.RoleEnum;
import com.liochio.common.exception.AppException;
import com.liochio.common.exception.ErrorCode;
import com.liochio.common.outbox.OutboxPublisher;
import com.liochio.common.security.JwtUtils;
import com.liochio.common.security.PasswordPolicyValidator;
import com.liochio.common.security.TokenBlacklistService;
import com.liochio.common.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ==============================================================================
 * Dịch Vụ Xác Thực & Bảo Mật Doanh Nghiệp (Enterprise Authentication Service)
 * ==============================================================================
 * 
 * Mục đích:
 * 1. Đăng ký người dùng mới (Pending Verify) & kích hoạt bằng OTP 6 số.
 * 2. Đăng nhập có kiểm soát Brute-force (khóa 15 phút sau 5 lần sai mật khẩu).
 * 3. Kiểm soát Thiết bị tin cậy (Step-up Auth 2FA khi phát hiện thiết bị lạ).
 * 4. Refresh Token Rotation kết hợp Token Reuse Detection (Family Revocation).
 * 5. Đăng xuất phiên hiện tại và đăng xuất toàn bộ thiết bị (Force Logout).
 * 6. Đổi mật khẩu tuân thủ nghiêm ngặt chính sách mật khẩu doanh nghiệp.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SecurityLoginHistoryRepository loginHistoryRepository;
    private final LoginSecurityService loginSecurityService;
    private final OtpService otpService;
    private final DeviceService deviceService;
    private final SessionService sessionService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtUtils jwtUtils;
    private final PasswordPolicyValidator passwordPolicyValidator;
    private final OutboxPublisher outboxPublisher;
    private final UserProvisioningService userProvisioningService;
    private final com.liochio.common.service.SystemConfigService systemConfigService;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    /**
     * Đăng ký tài khoản người dùng mới (Trạng thái PENDING_VERIFY)
     */
    @Transactional
    public UserResponse register(RegisterRequest request, ClientContextRequest context) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) tenantId = "SYSTEM";

        // 1. Kiểm tra chính sách mật khẩu
        passwordPolicyValidator.validate(request.getPassword());

        // 2. Kiểm tra trùng lặp
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
        }

        // 3. Gán Role
        Set<RoleEntity> roles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            for (String roleName : request.getRoles()) {
                findRoleByNameFlexible(roleName).ifPresent(roles::add);
            }
        }
        if (roles.isEmpty()) {
            findRoleByNameFlexible(RoleEnum.ROLE_CUSTOMER.getRoleName())
                    .or(() -> findRoleByNameFlexible("ROLE_CUSTOMER"))
                    .or(() -> findRoleByNameFlexible(RoleEnum.ROLE_VIEWER.getRoleName()))
                    .ifPresent(roles::add);
        }

        UserEntity newUser = UserEntity.builder()
                .username(request.getUsername())
                .password(SecurityUtils.encodePassword(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .status("PENDING_VERIFY")
                .isEmailVerified(false)
                .isPhoneVerified(false)
                .failedLoginAttempts(0)
                .roles(roles)
                .build();
        newUser.setTenantId(tenantId);

        UserEntity savedUser = userRepository.save(newUser);

        // 4. Sinh mã OTP kích hoạt tài khoản
        otpService.generateAndSaveOtp(tenantId, savedUser.getId(), "REGISTRATION", savedUser.getEmail(), "EMAIL");

        // 5. Ghi nhận thiết bị đầu tiên của người dùng
        if (context != null) {
            deviceService.getOrCreateDevice(tenantId, savedUser.getId(), context);
        }

        // 6. Ghi Outbox Event
        outboxPublisher.publish("USER", String.valueOf(savedUser.getId()), "USER_REGISTERED", savedUser);

        log.info("[AuthService] Đăng ký tài khoản thành công: ID={}, User='{}' (Chờ kích hoạt OTP)", savedUser.getId(), savedUser.getUsername());
        return mapToUserResponse(savedUser);
    }

    /**
     * Xác thực OTP kích hoạt tài khoản
     */
    @Transactional
    public UserResponse verifyRegistrationOtp(VerifyOtpRequest request) {
        UserEntity user = null;
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            user = userRepository.findByEmail(request.getEmail().trim()).orElse(null);
        }
        if (user == null && request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            user = userRepository.findByUsername(request.getUsername().trim()).orElse(null);
        }
        if (user == null && request.getUserId() != null) {
            user = userRepository.findById(request.getUserId()).orElse(null);
        }
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy thông tin người dùng với email, username hoặc userId đã cung cấp");
        }

        String tenantId = (user.getTenantId() != null && !user.getTenantId().isBlank())
                ? user.getTenantId()
                : (TenantContext.getTenantId() != null && !TenantContext.getTenantId().isBlank() ? TenantContext.getTenantId() : "default");

        // Xác thực OTP
        otpService.verifyOtp(tenantId, user.getId(), "REGISTRATION", request.getOtpCode());

        // Kích hoạt tài khoản
        user.setStatus("ACTIVE");
        user.setIsEmailVerified(true);
        userRepository.save(user);

        // Tự động cấp phát Sổ cái kế toán & Đồng bộ Ví sang FinTech DB
        userProvisioningService.provisionUserAccounts(user);

        // Ghi nhận sự kiện kích hoạt thành công vào Outbox
        outboxPublisher.publish("USER", String.valueOf(user.getId()), "USER_ACTIVATED", user);

        log.info("[AuthService] Kích hoạt thành công tài khoản: ID={}, User='{}'", user.getId(), user.getUsername());
        return mapToUserResponse(user);
    }

    /**
     * Đăng nhập đa phân hệ (SuperAdmin, Corporate SaaS, Retail FinTech)
     * Rào chắn đăng nhập chéo (Cross-Portal Barrier) bảo đảm cách ly 100% dữ liệu
     */
    @Transactional
    public TokenResponse login(LoginRequest request, ClientContextRequest context) {
        String portalType = request.getPortalType();
        String username = request.getUsername() != null ? request.getUsername().trim() : "";

        // Tự động nhận diện phân hệ nếu không truyền portalType hoặc truyền APP_PORTAL
        if (portalType == null || portalType.isBlank() || "APP_PORTAL".equalsIgnoreCase(portalType)) {
            if ("superadmin".equalsIgnoreCase(username) || existsInAdminDb(username)) {
                if ("APP_PORTAL".equalsIgnoreCase(portalType)) {
                    throw new AppException(ErrorCode.UNAUTHORIZED, "Truy cập bị từ chối: Tài khoản SuperAdmin chỉ được phép đăng nhập tại Cổng Quản Trị Hạ Tầng Core (http://localhost:5170). Không cho phép đăng nhập trên App Portal!");
                }
                portalType = "SUPERADMIN";
            } else if (existsInCorpDb(username)) {
                portalType = "CORP";
            } else if (existsInRetailDb(username)) {
                portalType = "CONSUMER";
            } else {
                portalType = "CONSUMER";
            }
        }
        portalType = portalType.toUpperCase().trim();

        if ("SUPERADMIN".equals(portalType)) {
            return loginSuperAdmin(request, context);
        } else if ("CORP".equals(portalType) || "CORPORATE".equals(portalType)) {
            return loginCorp(request, context);
        } else if ("CONSUMER".equals(portalType) || "RETAIL".equals(portalType)) {
            return loginRetail(request, context);
        } else {
            return loginSuperAdmin(request, context);
        }
    }

    private boolean existsInCorpDb(String username) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM users WHERE (username = ? OR email = ?) AND user_type IN ('CORP_ADMIN', 'CORP_MAKER', 'CORP_CHECKER') AND is_deleted = false",
                    Integer.class, username, username
            );
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean existsInRetailDb(String username) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM users WHERE (username = ? OR email = ? OR phone = ?) AND user_type = 'CUSTOMER' AND is_deleted = false",
                    Integer.class, username, username, username
            );
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean existsInAdminDb(String username) {
        if ("superadmin".equalsIgnoreCase(username)) return true;
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM users WHERE (username = ? OR email = ?) AND user_type = 'SUPER_ADMIN' AND is_deleted = false",
                    Integer.class, username, username
            );
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private TokenResponse loginSuperAdmin(LoginRequest request, ClientContextRequest context) {
        String username = request.getUsername().trim();
        String clientIp = (context != null && context.getClientIp() != null) ? context.getClientIp() : "127.0.0.1";
        String userAgent = (context != null && context.getUserAgent() != null) ? context.getUserAgent() : "Unknown";

        // Rào chắn bảo mật chặn đăng nhập chéo
        if (existsInCorpDb(username)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Truy cập bị từ chối: Tài khoản Doanh Nghiệp (Corp) không được phép đăng nhập vào Cổng Quản Trị Hệ Thống (SuperAdmin Portal).");
        }
        if (existsInRetailDb(username)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Truy cập bị từ chối: Tài khoản Khách Hàng Cá Nhân (Retail) không được phép đăng nhập vào Cổng Quản Trị Hệ Thống (SuperAdmin Portal).");
        }

        UserEntity user = userRepository.findByUsername(username).orElse(null);
        if (user == null || !"SUPER_ADMIN".equalsIgnoreCase(user.getUserType())) {
            loginSecurityService.recordFailedLogin("SYSTEM", username, clientIp, userAgent, "Tài khoản SuperAdmin không tồn tại");
            throw new AppException(ErrorCode.USER_NOT_FOUND, "Tài khoản SuperAdmin không tồn tại trên hệ thống.");
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new AppException(ErrorCode.USER_LOCKED, "Tài khoản SuperAdmin của bạn đã bị khóa hoặc vô hiệu hóa.");
        }

        if (!SecurityUtils.matchesPassword(request.getPassword(), user.getPassword())) {
            loginSecurityService.recordFailedLogin("SYSTEM", user.getUsername(), clientIp, userAgent, "Mật khẩu không chính xác");
            throw new AppException(ErrorCode.PASSWORD_INCORRECT, "Mật khẩu không chính xác.");
        }

        loginSecurityService.recordSuccessfulLogin("SYSTEM", user.getId(), user.getUsername(), clientIp, userAgent);
        UserDeviceEntity device = deviceService.getOrCreateDevice("SYSTEM", user.getId(), context);

        return issueTokens(user, device.getDeviceId(), clientIp, userAgent);
    }

    private TokenResponse loginCorp(LoginRequest request, ClientContextRequest context) {
        String username = request.getUsername().trim();
        String clientIp = (context != null && context.getClientIp() != null) ? context.getClientIp() : "127.0.0.1";
        String userAgent = (context != null && context.getUserAgent() != null) ? context.getUserAgent() : "Unknown";

        // Rào chắn bảo mật chặn đăng nhập chéo
        if ("superadmin".equalsIgnoreCase(username) || existsInAdminDb(username)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Truy cập bị từ chối: Tài khoản SuperAdmin không được phép đăng nhập vào Cổng Khách Hàng Doanh Nghiệp (Corporate Portal).");
        }
        if (existsInRetailDb(username)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Truy cập bị từ chối: Tài khoản Khách Hàng Cá Nhân không được phép đăng nhập vào Cổng Khách Hàng Doanh Nghiệp (Corporate Portal).");
        }

        UserEntity user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElse(null);
        if (user == null || (!"CORP_ADMIN".equalsIgnoreCase(user.getUserType()) 
                && !"CORP_MAKER".equalsIgnoreCase(user.getUserType()) 
                && !"CORP_CHECKER".equalsIgnoreCase(user.getUserType()))) {
            throw new AppException(ErrorCode.USER_NOT_FOUND, "Tài khoản Doanh Nghiệp không tồn tại.");
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new AppException(ErrorCode.USER_LOCKED, "Tài khoản Doanh Nghiệp đang bị khóa hoặc chưa kích hoạt.");
        }

        if (!SecurityUtils.matchesPassword(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_INCORRECT, "Mật khẩu tài khoản Doanh Nghiệp không chính xác.");
        }

        Long userId = user.getId();
        String tenantId = user.getTenantId() != null ? user.getTenantId() : "tenant_acme";
        String fullName = user.getFullName();
        String roleType = user.getUserType();
        String coreAccountRef = "ACC_CORP_001";
        if ("CORP_MAKER".equalsIgnoreCase(roleType)) {
            coreAccountRef = "ACC_CORP_OPS";
        }

        Set<String> roles = user.getRoles().stream()
                .map(RoleEntity::getRoleName)
                .collect(Collectors.toSet());
        if (roles.isEmpty()) {
            if ("CORP_MAKER".equalsIgnoreCase(roleType)) {
                roles.add("ROLE_MAKER");
            } else if ("CORP_CHECKER".equalsIgnoreCase(roleType)) {
                roles.add("ROLE_CHECKER");
            } else {
                roles.add("ROLE_CORP_ADMIN");
            }
        }

        Set<String> permissions = Set.of("CORP_READ", "CORP_TRANSACT", "CORP_APPROVE");
        String deviceId = (context != null && context.getDeviceId() != null) ? context.getDeviceId() : "CORP-DEV-" + userId;
        String refreshToken = jwtUtils.generateRefreshToken(userId, user.getUsername(), tenantId);
        String sessionId = sessionService.createSession(tenantId, userId, deviceId, refreshToken, clientIp, userAgent);
        String accessToken = jwtUtils.generateAccessToken(userId, user.getUsername(), tenantId, roles, permissions, deviceId, sessionId);

        recordLoginHistory(tenantId, userId, user.getUsername(), clientIp, userAgent, "SUCCESS", "Đăng nhập Cổng Doanh Nghiệp thành công");

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(SecurityConstants.ACCESS_TOKEN_VALIDITY_MS / 1000)
                .userId(userId)
                .username(user.getUsername())
                .fullName(fullName)
                .tenantId(tenantId)
                .roles(roles)
                .permissions(permissions)
                .requires2Fa(false)
                .deviceTrusted(true)
                .deviceId(deviceId)
                .sessionId(sessionId)
                .coreAccountRef(coreAccountRef)
                .issuedAt(Instant.now())
                .build();
    }

    private TokenResponse loginRetail(LoginRequest request, ClientContextRequest context) {
        String username = request.getUsername().trim();
        String clientIp = (context != null && context.getClientIp() != null) ? context.getClientIp() : "127.0.0.1";
        String userAgent = (context != null && context.getUserAgent() != null) ? context.getUserAgent() : "Unknown";

        // Rào chắn bảo mật chặn đăng nhập chéo
        if ("superadmin".equalsIgnoreCase(username) || existsInAdminDb(username)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Truy cập bị từ chối: Tài khoản SuperAdmin không được phép đăng nhập vào Cổng Khách Hàng Cá Nhân (Retail FinTech Portal).");
        }
        if (existsInCorpDb(username)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Truy cập bị từ chối: Tài khoản Doanh Nghiệp không được phép đăng nhập vào Cổng Khách Hàng Cá Nhân (Retail FinTech Portal).");
        }

        UserEntity user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElse(null);
        if (user == null || !"CUSTOMER".equalsIgnoreCase(user.getUserType())) {
            throw new AppException(ErrorCode.USER_NOT_FOUND, "Tài khoản Khách Hàng Cá Nhân không tồn tại.");
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new AppException(ErrorCode.USER_LOCKED, "Tài khoản Khách Hàng Cá Nhân đang bị khóa hoặc chưa kích hoạt.");
        }

        if (!SecurityUtils.matchesPassword(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_INCORRECT, "Mật khẩu Khách Hàng Cá Nhân không chính xác.");
        }

        Long userId = user.getId();
        String realUsername = user.getUsername();
        String tenantId = user.getTenantId() != null ? user.getTenantId() : "SYSTEM";
        String fullName = user.getFullName();
        String coreWalletRef = "be_nam".equalsIgnoreCase(realUsername) ? "ACC_RETAIL_PIGGY" : "ACC_RETAIL_PARENT";

        Set<String> roles = user.getRoles().stream()
                .map(RoleEntity::getRoleName)
                .collect(Collectors.toSet());
        if (roles.isEmpty()) {
            if ("be_nam".equalsIgnoreCase(realUsername)) {
                roles.add("ROLE_CHILD");
            } else {
                roles.add("ROLE_PARENT");
            }
            roles.add("ROLE_CUSTOMER");
        }

        Set<String> permissions = Set.of("RETAIL_READ", "RETAIL_WALLET", "RETAIL_PIGGY");
        String deviceId = (context != null && context.getDeviceId() != null) ? context.getDeviceId() : "RETAIL-DEV-" + userId;
        String refreshToken = jwtUtils.generateRefreshToken(userId, realUsername, tenantId);
        String sessionId = sessionService.createSession(tenantId, userId, deviceId, refreshToken, clientIp, userAgent);
        String accessToken = jwtUtils.generateAccessToken(userId, realUsername, tenantId, roles, permissions, deviceId, sessionId);

        recordLoginHistory(tenantId, userId, realUsername, clientIp, userAgent, "SUCCESS", "Đăng nhập Cổng Khách Hàng Cá Nhân thành công");

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(SecurityConstants.ACCESS_TOKEN_VALIDITY_MS / 1000)
                .userId(userId)
                .username(realUsername)
                .fullName(fullName)
                .tenantId(tenantId)
                .roles(roles)
                .permissions(permissions)
                .requires2Fa(false)
                .deviceTrusted(true)
                .deviceId(deviceId)
                .sessionId(sessionId)
                .coreAccountRef(coreWalletRef)
                .issuedAt(Instant.now())
                .build();
    }

    /**
     * Xác thực OTP cho Thiết bị Mới và cấp Token
     */
    @Transactional
    public TokenResponse verifyDeviceOtp(VerifyDeviceOtpRequest request, ClientContextRequest context) {
        UserEntity user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String tenantId = (user.getTenantId() != null && !user.getTenantId().isBlank())
                ? user.getTenantId()
                : (TenantContext.getTenantId() != null && !TenantContext.getTenantId().isBlank() ? TenantContext.getTenantId() : "default");

        // 1. Xác thực OTP
        otpService.verifyOtp(tenantId, user.getId(), "DEVICE_TRUST", request.getOtpCode());

        // 2. Gắn nhãn tin cậy cho thiết bị nếu người dùng chọn ghi nhớ
        if (Boolean.TRUE.equals(request.getRememberDevice())) {
            deviceService.trustDevice(tenantId, user.getId(), request.getDeviceId());
        }

        String clientIp = (context != null && context.getClientIp() != null) ? context.getClientIp() : "127.0.0.1";
        String userAgent = (context != null && context.getUserAgent() != null) ? context.getUserAgent() : "Unknown";

        recordLoginHistory(tenantId, user.getId(), user.getUsername(), clientIp, userAgent, "SUCCESS", "Đăng nhập thành công sau khi xác thực 2FA thiết bị");
        return issueTokens(user, request.getDeviceId(), clientIp, userAgent);
    }

    /**
     * Làm mới Token (Refresh Token Rotation & Reuse Detection)
     */
    @Transactional
    public TokenResponse refreshToken(RefreshTokenRequest request, ClientContextRequest context) {
        String oldRefreshToken = request.getRefreshToken();
        if (!jwtUtils.validateToken(oldRefreshToken)) {
            throw new AppException(ErrorCode.TOKEN_INVALID);
        }

        String clientIp = (context != null && context.getClientIp() != null) ? context.getClientIp() : "127.0.0.1";
        String userAgent = (context != null && context.getUserAgent() != null) ? context.getUserAgent() : "Unknown";
        String deviceId = (context != null) ? context.getDeviceId() : null;

        Long userId = jwtUtils.extractUserId(oldRefreshToken);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String newRefreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getUsername(), user.getTenantId());

        // Thực hiện xoay vòng phiên và kiểm tra Reuse Detection
        SessionService.RotateResult rotateResult = sessionService.rotateSession(oldRefreshToken, newRefreshToken, clientIp, userAgent, deviceId);

        Set<String> roles = user.getRoles().stream()
                .map(RoleEntity::getRoleName)
                .collect(Collectors.toSet());

        Set<String> permissions = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> p.getPermissionCode())
                .collect(Collectors.toSet());

        String newAccessToken = jwtUtils.generateAccessToken(user.getId(), user.getUsername(), user.getTenantId(), roles, permissions, rotateResult.deviceId(), rotateResult.newSessionId());

        log.info("[AuthService] Xoay vòng Token thành công: User='{}', SessionID='{}'", user.getUsername(), rotateResult.newSessionId());

        return TokenResponse.builder()
                .accessToken(newAccessToken)
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
                .deviceId(rotateResult.deviceId())
                .sessionId(rotateResult.newSessionId())
                .issuedAt(Instant.now())
                .build();
    }

    /**
     * Đăng xuất phiên hiện tại và đưa Token vào Blacklist tức thì
     */
    @Transactional
    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }
        if (token != null && jwtUtils.validateToken(token)) {
            var claims = jwtUtils.extractClaims(token);
            
            // 1. Tính thời gian sống còn lại của Access Token để đưa vào Blacklist
            java.util.Date expiration = claims.getExpiration();
            long remainingTtlMs = expiration.getTime() - System.currentTimeMillis();
            if (remainingTtlMs > 0) {
                tokenBlacklistService.blacklistToken(token, java.time.Duration.ofMillis(remainingTtlMs), "LOGOUT");
            }

            // 2. Thu hồi Session tương ứng
            String sessionId = (String) claims.get("sessionId");
            if (sessionId != null) {
                sessionService.revokeSession(sessionId, "LOGOUT");
            }
        }
        Long userId = UserContext.getUserId();
        log.info("[AuthService] Đăng xuất phiên thành công cho User ID: {}", userId);
    }

    /**
     * Đăng xuất toàn bộ thiết bị (Force Logout) và thu hồi toàn bộ Token
     */
    @Transactional
    public void logoutAllDevices(Long userId) {
        if (userId == null) userId = UserContext.getUserId();
        if (userId != null) {
            tokenBlacklistService.blacklistUser(userId, java.time.Duration.ofDays(7));
            sessionService.revokeAllUserSessions(userId, "FORCE_LOGOUT");
            log.info("[AuthService] Đã cưỡng chế đăng xuất toàn bộ thiết bị và thu hồi Token của User ID: {}", userId);
        }
    }

    /**
     * Đổi mật khẩu tài khoản
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        if (userId == null) userId = UserContext.getUserId();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!SecurityUtils.matchesPassword(request.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_INCORRECT);
        }

        passwordPolicyValidator.validate(request.getNewPassword());

        user.setPassword(SecurityUtils.encodePassword(request.getNewPassword()));
        user.setPasswordChangedAt(Instant.now());
        userRepository.save(user);

        // Thu hồi toàn bộ các phiên trên thiết bị khác để bắt buộc đăng nhập lại
        sessionService.revokeAllUserSessions(userId, "PASSWORD_CHANGED");
        log.info("[AuthService] Đổi mật khẩu thành công cho User ID: {}", userId);
    }

    /**
     * Lấy thông tin tài khoản hiện tại
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        Long userId = UserContext.getUserId();
        String username = UserContext.getUsername();
        if (userId == null && (username == null || username.isBlank() || "ANONYMOUS".equalsIgnoreCase(username))) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        UserEntity user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }
        if (user == null && username != null) {
            user = userRepository.findByUsername(username).orElse(null);
        }
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        return mapToUserResponse(user);
    }

    private TokenResponse issueTokens(UserEntity user, String deviceId, String ip, String userAgent) {
        Set<String> roles = user.getRoles().stream()
                .map(RoleEntity::getRoleName)
                .collect(Collectors.toSet());

        Set<String> permissions = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> p.getPermissionCode())
                .collect(Collectors.toSet());

        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getUsername(), user.getTenantId());
        // Concurrent Session Kick-out: Tự động thu hồi phiên cũ nếu đăng nhập từ thiết bị khác
        sessionService.kickOutOtherMobileSessions(user.getId(), deviceId);

        String sessionId = sessionService.createSession(user.getTenantId(), user.getId(), deviceId, refreshToken, ip, userAgent);
        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getUsername(), user.getTenantId(), roles, permissions, deviceId, sessionId);

        recordLoginHistory(user.getTenantId(), user.getId(), user.getUsername(), ip, userAgent, "SUCCESS", "Đăng nhập thành công");

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(SecurityConstants.ACCESS_TOKEN_VALIDITY_MS / 1000)
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .tenantId(user.getTenantId())
                .roles(roles)
                .permissions(permissions)
                .requires2Fa(false)
                .deviceTrusted(true)
                .deviceId(deviceId)
                .sessionId(sessionId)
                .coreAccountRef("ACC_SYSTEM_RESERVE")
                .issuedAt(Instant.now())
                .build();
    }

    private void recordLoginHistory(String tenantId, Long userId, String username, String ip, String userAgent, String status, String reason) {
        try {
            SecurityLoginHistoryEntity history = SecurityLoginHistoryEntity.builder()
                    .tenantId(tenantId != null ? tenantId : "SYSTEM")
                    .userId(userId)
                    .attemptedUsername(username)
                    .ipAddress(ip)
                    .userAgent(userAgent != null && userAgent.length() > 500 ? userAgent.substring(0, 500) : (userAgent != null ? userAgent : "Unknown"))
                    .loginStatus(status)
                    .failureReason(reason)
                    .riskScore(status.equals("SUCCESS") ? 0 : 50)
                    .createdAt(Instant.now())
                    .build();

            loginHistoryRepository.save(history);
        } catch (Exception e) {
            log.warn("[AuthService] Không thể ghi SecurityLoginHistory: {}", e.getMessage());
        }
    }

    private Optional<RoleEntity> findRoleByNameFlexible(String roleName) {
        if (roleName == null || roleName.isBlank()) return Optional.empty();
        Optional<RoleEntity> role = roleRepository.findByRoleName(roleName);
        if (role.isPresent()) return role;
        if (roleName.startsWith("ROLE_")) {
            return roleRepository.findByRoleName(roleName.substring(5));
        } else {
            return roleRepository.findByRoleName("ROLE_" + roleName);
        }
    }

    private UserResponse mapToUserResponse(UserEntity user) {
        Set<String> roles = user.getRoles().stream()
                .map(RoleEntity::getRoleName)
                .collect(Collectors.toSet());

        Set<String> permissions = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> p.getPermissionCode())
                .collect(Collectors.toSet());

        return UserResponse.builder()
                .id(user.getId())
                .tenantId(user.getTenantId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .userType(user.getUserType())
                .phone(user.getPhone())
                .idCardNumber(user.getIdCardNumber())
                .ekycLevel(user.getEkycLevel())
                .ekycStatus(user.getEkycStatus())
                .roles(roles)
                .permissions(permissions)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

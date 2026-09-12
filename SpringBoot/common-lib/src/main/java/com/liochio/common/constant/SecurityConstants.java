package com.liochio.common.constant;

/**
 * ==============================================================================
 * Hệ thống Hằng số Bảo mật & JWT (Security Constants)
 * ==============================================================================
 * 
 * Mục đích:
 * - Định nghĩa các hằng số liên quan đến mã hóa, xác thực JWT, claim keys,
 *   thời hạn sống của Token, và danh sách các endpoint công khai (Whitelist).
 * 
 * Khi nào sử dụng:
 * - Được JwtUtils, CustomAuthenticationEntryPoint, CustomAccessDeniedHandler,
 *   SecurityConfig và API Gateway Auth Filter sử dụng khi phân tích/tạo Token.
 */
public final class SecurityConstants {

    private SecurityConstants() {
        // Chống khởi tạo instance cho Utility Class
    }

    /**
     * Tiền tố của Bearer Token trong Authorization Header
     */
    public static final String BEARER_PREFIX = "Bearer ";

    /**
     * Tên các trường Claim trong Payload của JWT
     */
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_TENANT_ID = "tenantId";
    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_PERMISSIONS = "permissions";
    public static final String CLAIM_TOKEN_TYPE = "tokenType";

    /**
     * Loại Token
     */
    public static final String TOKEN_TYPE_ACCESS = "ACCESS";
    public static final String TOKEN_TYPE_REFRESH = "REFRESH";

    /**
     * Thời gian sống mặc định của Token (Miliseconds)
     * - Access Token: 1 giờ (3,600,000 ms)
     * - Refresh Token: 7 ngày (604,800,000 ms)
     */
    public static final long ACCESS_TOKEN_VALIDITY_MS = 3600 * 1000L;
    public static final long REFRESH_TOKEN_VALIDITY_MS = 7 * 24 * 3600 * 1000L;

    /**
     * Khóa bí mật mặc định dùng cho ký HMAC-SHA256 (Khuyến nghị ghi đè qua biến môi trường .env)
     */
    public static final String DEFAULT_JWT_SECRET = "portfolio-engine-super-secret-jwt-key-minimum-256-bits-for-security-2026";

    /**
     * Danh sách URL công khai không yêu cầu xác thực JWT (Whitelist)
     */
    public static final String[] PUBLIC_URL_PATTERNS = {
            "/.well-known/**",
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/verify-otp",
            "/api/auth/verify-device-otp",
            "/api/auth/refresh",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/api/auth/qr/init",
            "/api/auth/qr/exchange",
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/verify-otp",
            "/api/v1/auth/verify-device-otp",
            "/api/v1/auth/refresh",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password",
            "/api/v1/auth/qr/init",
            "/api/v1/auth/qr/exchange",
            "/api/v1/otp/**",
            "/api/otp/**",
            "/api/v1/system/**",
            "/api/system/**",
            "/api/public/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/**",
            "/ws/**",
            "/api/payments/webhook/**",
            "/api/payments/ipn/**",
            "/api/v1/ledger/m2m/**",
            "/api/ledger/m2m/**"
    };
}

package com.liochio.common.constant;

/**
 * ==============================================================================
 * Hệ thống Mã Lỗi Số Nguyên (Error Code Constants)
 * ==============================================================================
 * 
 * Mục đích:
 * - Quy định bảng mã lỗi số nguyên chuẩn hóa cho toàn bộ Backend, giúp Frontend
 *   dễ dàng map mã lỗi với giao diện hoặc logic xử lý tương ứng.
 * 
 * Quy ước phân nhóm:
 * - 1000 - 1999: Lỗi xác thực & phân quyền (Auth, Security, Token, Permissions)
 * - 2000 - 2999: Lỗi dữ liệu & Request Validation (Missing param, Malformed body, XSS)
 * - 3000 - 3999: Lỗi Dynamic Entity & Server-Driven UI
 * - 4000 - 4999: Lỗi Media, Notification & Payment Gateways
 * - 5000 - 5999: Lỗi Hệ thống, Hạ tầng & External Services (DB, Circuit Breaker, 500)
 */
public final class ErrorCodeConstants {

    private ErrorCodeConstants() {
        // Chống khởi tạo instance cho Utility Class
    }

    // Nhóm 1000: Auth & Security
    public static final int AUTH_UNAUTHENTICATED = 1001;
    public static final int AUTH_ACCESS_DENIED = 1002;
    public static final int AUTH_TOKEN_EXPIRED = 1003;
    public static final int AUTH_TOKEN_INVALID = 1004;
    public static final int AUTH_USER_NOT_FOUND = 1005;
    public static final int AUTH_USER_ALREADY_EXISTS = 1006;
    public static final int AUTH_PASSWORD_MISMATCH = 1007;
    public static final int AUTH_REFRESH_TOKEN_REUSED = 1008;
    public static final int AUTH_UNTRUSTED_DEVICE = 1009;
    public static final int AUTH_INVALID_OTP = 1010;
    public static final int AUTH_OTP_EXPIRED = 1011;
    public static final int AUTH_OTP_MAX_ATTEMPTS = 1012;
    public static final int AUTH_USER_LOCKED = 1013;
    public static final int AUTH_PASSWORD_POLICY_VIOLATION = 1014;
    public static final int AUTH_QR_SESSION_EXPIRED = 1015;
    public static final int AUTH_QR_SESSION_INVALID = 1016;
    public static final int AUTH_SMART_OTP_INVALID_PIN = 1017;
    public static final int AUTH_SMART_OTP_INVALID_CODE = 1018;
    public static final int AUTH_ABAC_POLICY_VIOLATION = 1019;
    public static final int AUTH_IMPOSSIBLE_TRAVEL = 1020;
    public static final int AUTH_ACTION_TOKEN_INVALID = 1021;
    public static final int AUTH_SESSION_KICKED_OUT = 1022;
    public static final int AUTH_TOKEN_REVOKED = 1023;

    // Nhóm 2000: Validation & Request
    public static final int VALIDATION_FAILED = 2001;
    public static final int MISSING_PARAMETER = 2002;
    public static final int MALFORMED_REQUEST_BODY = 2003;
    public static final int RESOURCE_NOT_FOUND = 2004;
    public static final int DUPLICATE_RESOURCE = 2005;
    public static final int IDEMPOTENCY_CONFLICT = 2006;
    public static final int METHOD_NOT_ALLOWED = 2007;
    public static final int EKYC_LIMIT_EXCEEDED = 2008;
    public static final int EKYC_NOT_VERIFIED = 2009;

    // Nhóm 3000: Dynamic Engine & Multi-Tenancy
    public static final int TENANT_NOT_FOUND = 3001;
    public static final int DYNAMIC_SCHEMA_INVALID = 3002;
    public static final int UI_CONFIG_NOT_FOUND = 3003;

    // Nhóm 4000: Media, Noti, Payment & Core Banking Ledger
    public static final int MEDIA_UPLOAD_FAILED = 4001;
    public static final int MEDIA_FILE_TOO_LARGE = 4002;
    public static final int NOTIFICATION_SEND_FAILED = 4003;
    public static final int PAYMENT_GATEWAY_ERROR = 4004;
    public static final int PAYMENT_SIGNATURE_INVALID = 4005;
    public static final int LEDGER_INSUFFICIENT_BALANCE = 4006;
    public static final int LEDGER_ACCOUNT_FROZEN = 4007;
    public static final int LEDGER_ACCOUNT_NOT_FOUND = 4008;
    public static final int LEDGER_DOUBLE_ENTRY_IMBALANCE = 4009;
    public static final int M2M_HMAC_INVALID = 4010;
    public static final int M2M_TIMESTAMP_EXPIRED = 4011;

    // Nhóm 5000: System & External
    public static final int INTERNAL_SERVER_ERROR = 5000;
    public static final int DATABASE_ERROR = 5001;
    public static final int SERVICE_UNAVAILABLE = 5002;
    public static final int CIRCUIT_BREAKER_TRIGGERED = 5003;
}

package com.liochio.common.constant;

/**
 * ==============================================================================
 * Hệ thống Khóa Thông báo Đa ngôn ngữ (i18n Message Key Constants)
 * ==============================================================================
 * 
 * Mục đích:
 * - Tập trung hóa các Key trong file Resource Bundle (messages_vi, messages_en, messages_zh)
 *   giúp tránh lỗi chính tả (typo) khi gọi MessageService dịch nội dung trả về cho Client.
 * 
 * Khi nào sử dụng:
 * - Được Service, Exception Handler và Controller gọi khi tạo ApiResponse hoặc ném AppException.
 */
public final class MessageConstants {

    private MessageConstants() {
        // Chống khởi tạo instance cho Utility Class
    }

    // General Success Messages
    public static final String MSG_SUCCESS = "api.response.success";
    public static final String MSG_CREATED = "api.response.created";
    public static final String MSG_UPDATED = "api.response.updated";
    public static final String MSG_DELETED = "api.response.deleted";

    // Authentication & Security Success Messages
    public static final String MSG_AUTH_REGISTER_SUCCESS = "api.response.auth.register_success";
    public static final String MSG_AUTH_VERIFY_SUCCESS = "api.response.auth.verify_success";
    public static final String MSG_AUTH_LOGIN_SUCCESS = "api.response.auth.login_success";
    public static final String MSG_AUTH_LOGIN_2FA_CHALLENGE = "api.response.auth.login_2fa_challenge";
    public static final String MSG_AUTH_VERIFY_DEVICE_SUCCESS = "api.response.auth.verify_device_success";
    public static final String MSG_AUTH_REFRESH_SUCCESS = "api.response.auth.refresh_success";
    public static final String MSG_AUTH_LOGOUT_SUCCESS = "api.response.auth.logout_success";
    public static final String MSG_AUTH_LOGOUT_ALL_SUCCESS = "api.response.auth.logout_all_success";
    public static final String MSG_AUTH_CHANGE_PASSWORD_SUCCESS = "api.response.auth.change_password_success";
    public static final String MSG_AUTH_QR_INIT_SUCCESS = "api.response.auth.qr_init_success";
    public static final String MSG_AUTH_QR_SCANNED = "api.response.auth.qr_scanned";
    public static final String MSG_AUTH_QR_CONFIRM_SUCCESS = "api.response.auth.qr_confirm_success";
    public static final String MSG_AUTH_QR_EXCHANGE_SUCCESS = "api.response.auth.qr_exchange_success";
    public static final String MSG_AUTH_DEVICE_REVOKED = "api.response.auth.device_revoked";
    public static final String MSG_AUTH_SESSION_REVOKED = "api.response.auth.session_revoked";
    public static final String MSG_AUTH_SMART_OTP_SETUP_SUCCESS = "api.response.auth.smart_otp_setup_success";
    public static final String MSG_AUTH_SMART_OTP_VERIFY_SUCCESS = "api.response.auth.smart_otp_verify_success";
    public static final String MSG_AUTH_ROLE_PERMISSIONS_UPDATED = "api.response.auth.role_permissions_updated";
    public static final String MSG_AUTH_TENANT_CREATED = "api.response.auth.tenant_created";
    public static final String MSG_AUTH_USER_DELETED = "api.response.auth.user_deleted";

    // Dedicated OTP Service Success Messages
    public static final String MSG_OTP_GENERATED = "api.response.otp.generated";
    public static final String MSG_OTP_VERIFIED = "api.response.otp.verified";
    public static final String MSG_OTP_BYPASSED = "api.response.otp.bypassed";
    public static final String MSG_OTP_CONFIG_UPDATED = "api.response.otp.config_updated";

    // Dynamic Entity & Portfolio Success Messages
    public static final String MSG_ENTITY_CREATED = "api.response.entity.created";
    public static final String MSG_ENTITY_UPDATED = "api.response.entity.updated";
    public static final String MSG_ENTITY_DELETED = "api.response.entity.deleted";
    public static final String MSG_PORTFOLIO_CREATED = "api.response.portfolio.created";
    public static final String MSG_PORTFOLIO_UPDATED = "api.response.portfolio.updated";
    public static final String MSG_PORTFOLIO_DELETED = "api.response.portfolio.deleted";
    public static final String MSG_FORM_SAVED = "api.response.form.saved";
    public static final String MSG_MENU_SAVED = "api.response.menu.saved";
    public static final String MSG_UI_CONFIG_SAVED = "api.response.ui_config.saved";

    // Media & Notification Success Messages
    public static final String MSG_MEDIA_UPLOAD_SUCCESS = "api.response.media.upload_success";
    public static final String MSG_MEDIA_CHUNK_SUCCESS = "api.response.media.chunk_success";
    public static final String MSG_NOTIFICATION_SENT = "api.response.notification.sent";

    // Payment & Booking Success Messages
    public static final String MSG_PAYMENT_URL_CREATED = "api.response.payment.url_created";
    public static final String MSG_BOOKING_CREATED = "api.response.booking.created";

    // Tour, Music, Film, AI, Realtime Success Messages
    public static final String MSG_TOUR_CREATED = "api.response.tour.created";
    public static final String MSG_MUSIC_SONG_CREATED = "api.response.music.song_created";
    public static final String MSG_FILM_MOVIE_CREATED = "api.response.film.movie_created";
    public static final String MSG_AI_SESSION_CREATED = "api.response.ai.session_created";
    public static final String MSG_AI_MESSAGE_SENT = "api.response.ai.message_sent";
    public static final String MSG_REALTIME_PUSH_SUCCESS = "api.response.realtime.push_success";

    // Authentication & Authorization Errors
    public static final String ERR_UNAUTHENTICATED = "api.error.unauthenticated";
    public static final String ERR_UNAUTHORIZED = "api.error.unauthorized";
    public static final String ERR_TOKEN_EXPIRED = "api.error.token.expired";
    public static final String ERR_TOKEN_INVALID = "api.error.token.invalid";
    public static final String ERR_USER_NOT_FOUND = "api.error.user.not_found";
    public static final String ERR_USER_EXISTED = "api.error.user.existed";
    public static final String ERR_PASSWORD_INCORRECT = "api.error.password.incorrect";
    public static final String ERR_UNTRUSTED_DEVICE = "api.error.auth.untrusted_device";
    public static final String ERR_INVALID_OTP = "api.error.auth.invalid_otp";
    public static final String ERR_OTP_EXPIRED = "api.error.auth.otp_expired";
    public static final String ERR_OTP_MAX_ATTEMPTS = "api.error.auth.otp_max_attempts";
    public static final String ERR_USER_LOCKED = "api.error.auth.user_locked";
    public static final String ERR_PASSWORD_POLICY = "api.error.auth.password_policy";
    public static final String ERR_QR_EXPIRED = "api.error.auth.qr_expired";
    public static final String ERR_QR_INVALID = "api.error.auth.qr_invalid";
    public static final String ERR_SMART_OTP_PIN = "api.error.auth.smart_otp_pin";
    public static final String ERR_SMART_OTP_CODE = "api.error.auth.smart_otp_code";

    // General & Validation Errors
    public static final String ERR_INVALID_REQUEST = "api.error.invalid_request";
    public static final String ERR_RESOURCE_NOT_FOUND = "api.error.resource.not_found";
    public static final String ERR_INTERNAL_SERVER_ERROR = "api.error.internal_server_error";
    public static final String ERR_METHOD_NOT_ALLOWED = "api.error.method_not_allowed";
    public static final String ERR_MISSING_PARAM = "api.error.missing_param";
    public static final String ERR_DUPLICATE_RESOURCE = "api.error.duplicate_resource";
    public static final String ERR_IDEMPOTENCY_CONFLICT = "api.error.idempotency_conflict";

    // Business Logic Errors
    public static final String ERR_TENANT_NOT_FOUND = "api.error.tenant.not_found";
    public static final String ERR_PAYMENT_FAILED = "api.error.payment.failed";
    public static final String ERR_FILE_TOO_LARGE = "api.error.file.too_large";
    public static final String ERR_FILE_TYPE_INVALID = "api.error.file.type_invalid";
}

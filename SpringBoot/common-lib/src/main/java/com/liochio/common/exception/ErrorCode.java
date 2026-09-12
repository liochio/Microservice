package com.liochio.common.exception;

import com.liochio.common.constant.ErrorCodeConstants;
import com.liochio.common.constant.MessageConstants;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * ==============================================================================
 * Bảng Định Nghĩa Mã Lỗi Nghiệp Vụ Toàn Hệ Thống (Application Error Codes)
 * ==============================================================================
 * 
 * Mục đích:
 * - Tập trung hóa mã lỗi (code dạng số nguyên), khóa thông báo i18n (messageKey),
 *   thông điệp mặc định (fallback) và HTTP Status tương ứng.
 * 
 * Khi nào gọi:
 * - Được ném kèm theo `AppException` trong các tầng Service/Domain logic.
 */
@Getter
public enum ErrorCode {

    // Nhóm 1000: Authentication & Authorization
    UNAUTHENTICATED(ErrorCodeConstants.AUTH_UNAUTHENTICATED, MessageConstants.ERR_UNAUTHENTICATED, "Chưa xác thực danh tính", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(ErrorCodeConstants.AUTH_ACCESS_DENIED, MessageConstants.ERR_UNAUTHORIZED, "Bạn không có quyền truy cập", HttpStatus.FORBIDDEN),
    TOKEN_EXPIRED(ErrorCodeConstants.AUTH_TOKEN_EXPIRED, MessageConstants.ERR_TOKEN_EXPIRED, "Token đã hết hạn", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID(ErrorCodeConstants.AUTH_TOKEN_INVALID, MessageConstants.ERR_TOKEN_INVALID, "Token không hợp lệ", HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND(ErrorCodeConstants.AUTH_USER_NOT_FOUND, MessageConstants.ERR_USER_NOT_FOUND, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS(ErrorCodeConstants.AUTH_USER_ALREADY_EXISTS, MessageConstants.ERR_USER_EXISTED, "Tài khoản đã tồn tại", HttpStatus.BAD_REQUEST),
    PASSWORD_INCORRECT(ErrorCodeConstants.AUTH_PASSWORD_MISMATCH, MessageConstants.ERR_PASSWORD_INCORRECT, "Mật khẩu không chính xác", HttpStatus.BAD_REQUEST),
    REFRESH_TOKEN_REUSED(ErrorCodeConstants.AUTH_REFRESH_TOKEN_REUSED, MessageConstants.ERR_TOKEN_INVALID, "Phát hiện Refresh Token bị tái sử dụng trái phép", HttpStatus.UNAUTHORIZED),
    UNTRUSTED_DEVICE_CHALLENGE(ErrorCodeConstants.AUTH_UNTRUSTED_DEVICE, MessageConstants.ERR_UNTRUSTED_DEVICE, "Phát hiện thiết bị lạ, yêu cầu xác thực 2FA", HttpStatus.UNAUTHORIZED),
    INVALID_OTP(ErrorCodeConstants.AUTH_INVALID_OTP, MessageConstants.ERR_INVALID_OTP, "Mã xác thực OTP không chính xác", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED(ErrorCodeConstants.AUTH_OTP_EXPIRED, MessageConstants.ERR_OTP_EXPIRED, "Mã xác thực OTP đã hết hạn", HttpStatus.BAD_REQUEST),
    OTP_MAX_ATTEMPTS_EXCEEDED(ErrorCodeConstants.AUTH_OTP_MAX_ATTEMPTS, MessageConstants.ERR_OTP_MAX_ATTEMPTS, "Đã vượt quá số lần nhập OTP cho phép", HttpStatus.BAD_REQUEST),
    USER_LOCKED(ErrorCodeConstants.AUTH_USER_LOCKED, MessageConstants.ERR_USER_LOCKED, "Tài khoản tạm thời bị khóa do đăng nhập sai nhiều lần", HttpStatus.FORBIDDEN),
    PASSWORD_POLICY_VIOLATION(ErrorCodeConstants.AUTH_PASSWORD_POLICY_VIOLATION, MessageConstants.ERR_PASSWORD_POLICY, "Mật khẩu không thỏa mãn chính sách bảo mật", HttpStatus.BAD_REQUEST),
    QR_SESSION_EXPIRED(ErrorCodeConstants.AUTH_QR_SESSION_EXPIRED, MessageConstants.ERR_QR_EXPIRED, "Phiên quét mã QR đã hết hạn", HttpStatus.BAD_REQUEST),
    QR_SESSION_INVALID(ErrorCodeConstants.AUTH_QR_SESSION_INVALID, MessageConstants.ERR_QR_INVALID, "Phiên quét mã QR không hợp lệ hoặc đã sử dụng", HttpStatus.BAD_REQUEST),
    SMART_OTP_INVALID_PIN(ErrorCodeConstants.AUTH_SMART_OTP_INVALID_PIN, MessageConstants.ERR_SMART_OTP_PIN, "Mã PIN SmartOTP không chính xác", HttpStatus.BAD_REQUEST),
    SMART_OTP_INVALID_CODE(ErrorCodeConstants.AUTH_SMART_OTP_INVALID_CODE, MessageConstants.ERR_SMART_OTP_CODE, "Mã SmartOTP không chính xác hoặc đã hết hạn", HttpStatus.BAD_REQUEST),
    ABAC_POLICY_VIOLATION(ErrorCodeConstants.AUTH_ABAC_POLICY_VIOLATION, MessageConstants.ERR_UNAUTHORIZED, "Không thỏa mãn chính sách bảo mật ngữ cảnh (ABAC Policy)", HttpStatus.FORBIDDEN),
    IMPOSSIBLE_TRAVEL_DETECTED(ErrorCodeConstants.AUTH_IMPOSSIBLE_TRAVEL, MessageConstants.ERR_UNAUTHORIZED, "Phát hiện vị trí đăng nhập bất thường (Impossible Travel), yêu cầu xác thực 2FA", HttpStatus.UNAUTHORIZED),
    ACTION_TOKEN_INVALID(ErrorCodeConstants.AUTH_ACTION_TOKEN_INVALID, MessageConstants.ERR_TOKEN_INVALID, "Action Token xác thực bước đệm không hợp lệ hoặc đã hết hạn", HttpStatus.UNAUTHORIZED),
    SESSION_KICKED_OUT(ErrorCodeConstants.AUTH_SESSION_KICKED_OUT, MessageConstants.ERR_UNAUTHORIZED, "Tài khoản của bạn đã được đăng nhập từ một thiết bị di động khác", HttpStatus.UNAUTHORIZED),
    TOKEN_REVOKED(ErrorCodeConstants.AUTH_TOKEN_REVOKED, MessageConstants.ERR_TOKEN_INVALID, "Token đã bị thu hồi hoặc tài khoản đã đăng xuất", HttpStatus.UNAUTHORIZED),

    // Nhóm 2000: Request & Validation
    INVALID_REQUEST(ErrorCodeConstants.VALIDATION_FAILED, MessageConstants.ERR_INVALID_REQUEST, "Dữ liệu yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST),
    MISSING_PARAMETER(ErrorCodeConstants.MISSING_PARAMETER, MessageConstants.ERR_MISSING_PARAM, "Thiếu tham số bắt buộc", HttpStatus.BAD_REQUEST),
    MALFORMED_REQUEST_BODY(ErrorCodeConstants.MALFORMED_REQUEST_BODY, MessageConstants.ERR_INVALID_REQUEST, "Cấu trúc body request sai định dạng", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND(ErrorCodeConstants.RESOURCE_NOT_FOUND, MessageConstants.ERR_RESOURCE_NOT_FOUND, "Không tìm thấy tài nguyên yêu cầu", HttpStatus.NOT_FOUND),
    DUPLICATE_RESOURCE(ErrorCodeConstants.DUPLICATE_RESOURCE, MessageConstants.ERR_DUPLICATE_RESOURCE, "Tài nguyên đã tồn tại", HttpStatus.CONFLICT),
    IDEMPOTENCY_CONFLICT(ErrorCodeConstants.IDEMPOTENCY_CONFLICT, MessageConstants.ERR_IDEMPOTENCY_CONFLICT, "Yêu cầu trùng lặp đang được xử lý", HttpStatus.CONFLICT),
    METHOD_NOT_ALLOWED(ErrorCodeConstants.METHOD_NOT_ALLOWED, MessageConstants.ERR_METHOD_NOT_ALLOWED, "Phương thức HTTP không được hỗ trợ", HttpStatus.METHOD_NOT_ALLOWED),
    EKYC_LIMIT_EXCEEDED(ErrorCodeConstants.EKYC_LIMIT_EXCEEDED, MessageConstants.ERR_INVALID_REQUEST, "Giao dịch vượt quá hạn mức cho phép của cấp độ eKYC", HttpStatus.BAD_REQUEST),
    EKYC_NOT_VERIFIED(ErrorCodeConstants.EKYC_NOT_VERIFIED, MessageConstants.ERR_UNAUTHORIZED, "Tài khoản chưa hoàn tất định danh eKYC bắt buộc", HttpStatus.FORBIDDEN),

    // Nhóm 3000: Multi-Tenancy & Dynamic Engine
    TENANT_NOT_FOUND(ErrorCodeConstants.TENANT_NOT_FOUND, MessageConstants.ERR_TENANT_NOT_FOUND, "Không tìm thấy thông tin khách thuê", HttpStatus.NOT_FOUND),
    DYNAMIC_SCHEMA_INVALID(ErrorCodeConstants.DYNAMIC_SCHEMA_INVALID, MessageConstants.ERR_INVALID_REQUEST, "Cấu hình Dynamic Entity không hợp lệ", HttpStatus.BAD_REQUEST),

    // Nhóm 4000: Media, Notification, Payment & Core Banking Ledger
    MEDIA_UPLOAD_FAILED(ErrorCodeConstants.MEDIA_UPLOAD_FAILED, MessageConstants.ERR_INTERNAL_SERVER_ERROR, "Tải lên tệp tin thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_TOO_LARGE(ErrorCodeConstants.MEDIA_FILE_TOO_LARGE, MessageConstants.ERR_FILE_TOO_LARGE, "Tệp tin vượt quá dung lượng cho phép", HttpStatus.BAD_REQUEST),
    PAYMENT_FAILED(ErrorCodeConstants.PAYMENT_GATEWAY_ERROR, MessageConstants.ERR_PAYMENT_FAILED, "Giao dịch thanh toán thất bại", HttpStatus.BAD_REQUEST),
    PAYMENT_SIGNATURE_INVALID(ErrorCodeConstants.PAYMENT_SIGNATURE_INVALID, MessageConstants.ERR_INVALID_REQUEST, "Chữ ký bảo mật IPN không hợp lệ", HttpStatus.BAD_REQUEST),
    LEDGER_INSUFFICIENT_BALANCE(ErrorCodeConstants.LEDGER_INSUFFICIENT_BALANCE, MessageConstants.ERR_INVALID_REQUEST, "Số dư tài khoản khả dụng không đủ để thực hiện giao dịch", HttpStatus.BAD_REQUEST),
    LEDGER_ACCOUNT_FROZEN(ErrorCodeConstants.LEDGER_ACCOUNT_FROZEN, MessageConstants.ERR_UNAUTHORIZED, "Tài khoản kế toán đang bị tạm khóa hoặc đóng băng", HttpStatus.FORBIDDEN),
    LEDGER_ACCOUNT_NOT_FOUND(ErrorCodeConstants.LEDGER_ACCOUNT_NOT_FOUND, MessageConstants.ERR_RESOURCE_NOT_FOUND, "Không tìm thấy tài khoản sổ cái kế toán", HttpStatus.NOT_FOUND),
    LEDGER_DOUBLE_ENTRY_IMBALANCE(ErrorCodeConstants.LEDGER_DOUBLE_ENTRY_IMBALANCE, MessageConstants.ERR_INVALID_REQUEST, "Bút toán sổ cái không cân đối giữa vế Nợ (Debit) và vế Có (Credit)", HttpStatus.BAD_REQUEST),
    M2M_HMAC_INVALID(ErrorCodeConstants.M2M_HMAC_INVALID, MessageConstants.ERR_UNAUTHORIZED, "Chữ ký số M2M HMAC không hợp lệ hoặc đã bị can thiệp", HttpStatus.UNAUTHORIZED),
    M2M_TIMESTAMP_EXPIRED(ErrorCodeConstants.M2M_TIMESTAMP_EXPIRED, MessageConstants.ERR_UNAUTHORIZED, "Thời gian yêu cầu M2M đã quá hạn (lệch quá 5 phút)", HttpStatus.UNAUTHORIZED),

    // Nhóm 5000: Internal System
    UNCATEGORIZED_EXCEPTION(ErrorCodeConstants.INTERNAL_SERVER_ERROR, MessageConstants.ERR_INTERNAL_SERVER_ERROR, "Lỗi hệ thống không xác định", HttpStatus.INTERNAL_SERVER_ERROR),
    DATABASE_ERROR(ErrorCodeConstants.DATABASE_ERROR, MessageConstants.ERR_INTERNAL_SERVER_ERROR, "Lỗi tương tác cơ sở dữ liệu", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_UNAVAILABLE(ErrorCodeConstants.SERVICE_UNAVAILABLE, MessageConstants.ERR_INTERNAL_SERVER_ERROR, "Dịch vụ tạm thời không khả dụng", HttpStatus.SERVICE_UNAVAILABLE);

    private final int code;
    private final String messageKey;
    private final String defaultMessage;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String messageKey, String defaultMessage, HttpStatus httpStatus) {
        this.code = code;
        this.messageKey = messageKey;
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }
}
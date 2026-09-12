package com.liochio.common.exception;

import lombok.Getter;

/**
 * ==============================================================================
 * Ngoại Lệ Nghiệp Vụ Toàn Cục (Application Business Exception)
 * ==============================================================================
 * 
 * Mục đích:
 * - Đại diện cho tất cả các ngoại lệ nghiệp vụ có chủ đích được ném ra từ tầng Service/Domain.
 * - Mang theo ErrorCode chuẩn, thông báo tùy biến và các tham số phục vụ cho việc
 *   format đa ngôn ngữ tự động tại GlobalExceptionHandler.
 * 
 * Khi nào gọi:
 * - Được ném ra bất cứ khi nào một điều kiện nghiệp vụ không được thỏa mãn
 *   (vd: User không tồn tại, sai mật khẩu, thiếu quyền, dữ liệu xung đột).
 */
@Getter
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Object[] messageArgs;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.messageArgs = null;
    }

    public AppException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.messageArgs = null;
    }

    public AppException(ErrorCode errorCode, Object[] messageArgs) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.messageArgs = messageArgs;
    }
}
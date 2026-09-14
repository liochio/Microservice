package com.liochio.common.exception;

import com.liochio.common.dto.ApiResponse;
import com.liochio.common.i18n.MessageService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * ==============================================================================
 * Bộ Xử Lý Ngoại Lệ Toàn Cục Tập Trung (Centralized Global Exception Handler)
 * ==============================================================================
 * 
 * Mục đích:
 * - Bắt 100% các ngoại lệ phát sinh trong hệ thống, chuyển đổi thành định dạng ApiResponse chuẩn
 *   { status, message, data, timestamp (Instant UTC) } đồng nhất.
 * - Tự động dịch thông báo lỗi sang Tiếng Việt, Tiếng Anh hoặc Tiếng Trung dựa vào Accept-Language.
 * - Gom toàn bộ lỗi sai trường (@Valid), thiếu tham số (@RequestParam), sai cú pháp JSON thành dữ liệu chi tiết.
 * 
 * Khi nào gọi:
 * - Tự động được Spring Framework kích hoạt khi bất kỳ Controller nào ném ra Exception.
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageService messageService;

    /**
     * 1. Bắt ngoại lệ nghiệp vụ có chủ đích (AppException)
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Object>> handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        String customMsg = ex.getMessage();
        String localizedMessage;
        
        if (customMsg != null && !customMsg.isBlank() && !customMsg.equals(errorCode.getDefaultMessage())) {
            localizedMessage = customMsg;
        } else {
            localizedMessage = messageService.getMessage(errorCode.getMessageKey(), ex.getMessageArgs());
            if (localizedMessage == null || localizedMessage.equals(errorCode.getMessageKey())) {
                localizedMessage = errorCode.getDefaultMessage();
            }
        }

        log.warn("[AppException] Code: {} - Message: {}", errorCode.getCode(), localizedMessage);

        ApiResponse<Object> response = ApiResponse.error(errorCode.getCode(), localizedMessage);
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    /**
     * 2. Bắt lỗi Validate DTO qua @Valid, @NotNull, @NotBlank, @Size... (MethodArgumentNotValidException)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        String message = messageService.getMessage(ErrorCode.INVALID_REQUEST.getMessageKey());
        log.warn("[ValidationException] Field errors: {}", errors);

        ApiResponse<Map<String, String>> response = ApiResponse.error(
                ErrorCode.INVALID_REQUEST.getCode(),
                message,
                errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 3. Bắt lỗi vi phạm ràng buộc ConstraintViolationException (Validate trên @RequestParam / @PathVariable)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.put(violation.getPropertyPath().toString(), violation.getMessage());
        }

        String message = messageService.getMessage(ErrorCode.INVALID_REQUEST.getMessageKey());
        ApiResponse<Map<String, String>> response = ApiResponse.error(
                ErrorCode.INVALID_REQUEST.getCode(),
                message,
                errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 4. Bắt lỗi thiếu tham số bắt buộc trong Request (MissingServletRequestParameterException)
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingParam(MissingServletRequestParameterException ex) {
        String message = messageService.getMessage(ErrorCode.MISSING_PARAMETER.getMessageKey(), new Object[]{ex.getParameterName()});
        log.warn("[MissingParam] Parameter '{}' is missing", ex.getParameterName());

        ApiResponse<Object> response = ApiResponse.error(ErrorCode.MISSING_PARAMETER.getCode(), message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 5. Bắt lỗi Payload JSON sai định dạng hoặc không thể parse (HttpMessageNotReadableException)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotReadable(HttpMessageNotReadableException ex) {
        String message = messageService.getMessage(ErrorCode.MALFORMED_REQUEST_BODY.getMessageKey());
        log.warn("[HttpMessageNotReadable] Malformed JSON: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(ErrorCode.MALFORMED_REQUEST_BODY.getCode(), message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 6. Bắt lỗi 404 Không tìm thấy Endpoint API (NoResourceFoundException - Spring Boot 3)
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoResourceFound(NoResourceFoundException ex) {
        String message = messageService.getMessage(ErrorCode.RESOURCE_NOT_FOUND.getMessageKey()) + ": " + ex.getResourcePath();
        ApiResponse<Object> response = ApiResponse.error(HttpStatus.NOT_FOUND.value(), message);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * 7. Bắt lỗi sai phương thức HTTP (HttpRequestMethodNotSupportedException)
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        String message = messageService.getMessage(ErrorCode.METHOD_NOT_ALLOWED.getMessageKey());
        ApiResponse<Object> response = ApiResponse.error(HttpStatus.METHOD_NOT_ALLOWED.value(), message);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    /**
     * 8. Bắt lỗi từ chối truy cập do thiếu quyền hạn (AccessDeniedException - 403 Forbidden)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException ex) {
        String message = messageService.getMessage(ErrorCode.UNAUTHORIZED.getMessageKey());
        ApiResponse<Object> response = ApiResponse.error(ErrorCode.UNAUTHORIZED.getCode(), message);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * 9. Bắt lỗi chưa xác thực hoặc token không hợp lệ (AuthenticationException - 401 Unauthorized)
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthentication(AuthenticationException ex) {
        String message = messageService.getMessage(ErrorCode.UNAUTHENTICATED.getMessageKey());
        ApiResponse<Object> response = ApiResponse.error(ErrorCode.UNAUTHENTICATED.getCode(), message);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * 10. Bắt lỗi sai kiểu dữ liệu tham số URL/Query (MethodArgumentTypeMismatchException - 400 Bad Request)
     */
    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatch(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex) {
        String paramName = ex.getName();
        Object value = ex.getValue();
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "hợp lệ";
        String message = String.format("Tham số '%s' nhận giá trị '%s' không đúng định dạng (yêu cầu kiểu %s)", paramName, value, requiredType);
        log.warn("[TypeMismatch] Param: {}, Value: {}, Required: {}", paramName, value, requiredType);

        ApiResponse<Object> response = ApiResponse.error(ErrorCode.INVALID_REQUEST.getCode(), message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 11. Bắt lỗi xung đột ràng buộc dữ liệu CSDL (DataIntegrityViolationException - 409 Conflict)
     */
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolation(org.springframework.dao.DataIntegrityViolationException ex) {
        log.warn("[DataIntegrityViolation] Database constraint violated: {}", ex.getMostSpecificCause().getMessage());
        String message = "Dữ liệu bị trùng lặp hoặc vi phạm ràng buộc toàn vẹn của hệ thống";

        ApiResponse<Object> response = ApiResponse.error(HttpStatus.CONFLICT.value(), message);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * 12. Bắt lỗi tham số nghiệp vụ không hợp lệ (IllegalArgumentException, IllegalStateException - 400 Bad Request)
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ApiResponse<Object>> handleIllegalArguments(RuntimeException ex) {
        log.warn("[IllegalArgument] {}", ex.getMessage());
        ApiResponse<Object> response = ApiResponse.error(ErrorCode.INVALID_REQUEST.getCode(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 13. Fallback: Bắt toàn bộ các lỗi 500 chưa được định nghĩa khác
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneralException(Exception ex) {
        log.error("[UnhandledException] Internal server error occurred: ", ex);
        String message = messageService.getMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessageKey());
        ApiResponse<Object> response = ApiResponse.error(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode(), message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
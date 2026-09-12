# Package: com.liochio.common.exception

## 1. Vai trò & Chức năng
- Gom toàn bộ việc xử lý ngoại lệ về một nơi tập trung theo nguyên tắc Chuẩn hóa và Tập trung hóa.
- Tự động bản địa hóa (i18n) thông điệp lỗi sang tiếng Việt, Anh, Trung theo header `Accept-Language`.

## 2. Các thành phần chính
- `ErrorCode.java`: Enum bảng mã lỗi số nguyên và mapping messageKey.
- `AppException.java`: RuntimeException đại diện cho lỗi nghiệp vụ có chủ đích.
- `GlobalExceptionHandler.java`: `@RestControllerAdvice` bắt và xử lý 100% lỗi HTTP (400, 401, 403, 404, 500, Validations, Deserialization).

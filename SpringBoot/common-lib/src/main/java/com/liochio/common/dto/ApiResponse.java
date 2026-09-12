package com.liochio.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * ==============================================================================
 * Chuẩn Hóa Định Dạng Phản Hồi Dữ Liệu Toàn Hệ Thống (Standardized API Response)
 * ==============================================================================
 * 
 * Mục đích:
 * - Đồng nhất 100% định dạng JSON đầu ra cho tất cả các Microservices theo format:
 *   {
 *       "status": 200,
 *       "message": "Thành công",
 *       "data": { ... },
 *       "timestamp": "2026-08-24T01:23:45.678Z"
 *   }
 * - Đảm bảo toàn bộ timestamp luôn sử dụng kiểu Instant (UTC ISO-8601) chuẩn quốc tế.
 * 
 * Khi nào gọi:
 * - Tự động được GlobalResponseWrapper bọc lại hoặc được gọi trực tiếp trong Controller
 *   khi cần trả về dữ liệu tuỳ biến mã trạng thái (200, 201, 204, 400, 500...).
 * 
 * @param <T> Kiểu dữ liệu của payload trả về trong trường "data"
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Mã trạng thái HTTP hoặc mã nghiệp vụ (vd: 200, 201, 400, 404, 500)
     */
    @Builder.Default
    private int status = 200;

    /**
     * Thông báo kết quả thân thiện với người dùng (đã được i18n đa ngôn ngữ)
     */
    private String message;

    /**
     * Dữ liệu chính trả về cho Client
     */
    private T data;

    /**
     * Thời điểm phản hồi theo chuẩn múi giờ UTC (Instant ISO-8601)
     */
    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * Factory Method tạo phản hồi thành công (200 OK) kèm data
     *
     * @param data Dữ liệu trả về
     * @param <T>  Kiểu dữ liệu
     * @return ApiResponse thành công
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .status(200)
                .message("Thành công")
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Factory Method tạo phản hồi thành công (200 OK) kèm data và thông điệp tùy biến
     *
     * @param data    Dữ liệu trả về
     * @param message Thông báo tùy chỉnh
     * @param <T>     Kiểu dữ liệu
     * @return ApiResponse thành công
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .status(200)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Factory Method tạo phản hồi khởi tạo thành công tài nguyên (201 CREATED)
     *
     * @param data    Dữ liệu tài nguyên mới tạo
     * @param message Thông báo
     * @param <T>     Kiểu dữ liệu
     * @return ApiResponse 201 CREATED
     */
    public static <T> ApiResponse<T> created(T data, String message) {
        return ApiResponse.<T>builder()
                .status(201)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Factory Method tạo phản hồi rỗng (204 NO CONTENT hoặc 200 OK cho tác vụ DELETE)
     *
     * @param message Thông điệp xóa hoặc hoàn tất
     * @return ApiResponse không chứa data
     */
    public static ApiResponse<Void> noContent(String message) {
        return ApiResponse.<Void>builder()
                .status(204)
                .message(message)
                .data(null)
                .timestamp(Instant.now())
                .build();
    }

    public static ApiResponse<Void> noContent() {
        return ApiResponse.<Void>builder()
                .status(204)
                .message("OK")
                .data(null)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Factory Method tạo phản hồi lỗi
     *
     * @param status  Mã trạng thái lỗi
     * @param message Thông báo chi tiết lỗi
     * @param <T>     Kiểu dữ liệu
     * @return ApiResponse chứa thông tin lỗi
     */
    public static <T> ApiResponse<T> error(int status, String message) {
        return ApiResponse.<T>builder()
                .status(status)
                .message(message)
                .data(null)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Factory Method tạo phản hồi lỗi kèm chi tiết (validation map / error details)
     *
     * @param status  Mã trạng thái lỗi
     * @param message Thông báo tóm tắt
     * @param details Chi tiết danh sách lỗi các trường
     * @param <T>     Kiểu dữ liệu details
     * @return ApiResponse chứa thông tin chi tiết lỗi
     */
    public static <T> ApiResponse<T> error(int status, String message, T details) {
        return ApiResponse.<T>builder()
                .status(status)
                .message(message)
                .data(details)
                .timestamp(Instant.now())
                .build();
    }
}
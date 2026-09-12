package com.liochio.common.advice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liochio.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * ==============================================================================
 * Tự Động Đóng Gói Phản Hồi API Toàn Cục (Global Response Wrapper Advice)
 * ==============================================================================
 * 
 * Mục đích:
 * - Tự động bọc kết quả trả về từ tất cả các Controller vào đối tượng `ApiResponse<T>`
 *   chuẩn hóa mà lập trình viên không cần phải viết tay bọc `ApiResponse.success(...)` lặp lại.
 * - Bỏ qua các endpoint đặc thù của Swagger/OpenAPI, Actuator và các response đã là `ApiResponse`.
 * - Xử lý chuyển đổi riêng biệt kiểu `String` để tránh lỗi ClassCastException trong StringHttpMessageConverter.
 * 
 * Khi nào gọi:
 * - Tự động được Spring MVC kích hoạt ngay trước khi ghi dữ liệu phản hồi xuống luồng HTTP OutputStream.
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalResponseWrapper implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // Áp dụng bọc tự động cho tất cả các Controller endpoints
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response
    ) {
        // Nếu response đã là ApiResponse thì giữ nguyên
        if (body instanceof ApiResponse) {
            return body;
        }

        // Bỏ qua Swagger / OpenAPI documentation endpoints và Spring Actuator
        String path = request.getURI().getPath();
        if (path.contains("/v3/api-docs") || path.contains("/swagger-ui") || path.contains("/actuator")) {
            return body;
        }

        // Xử lý riêng cho kiểu dữ liệu String để tránh StringHttpMessageConverter ClassCastException
        if (body instanceof String || StringHttpMessageConverter.class.isAssignableFrom(selectedConverterType)) {
            try {
                response.getHeaders().setContentType(MediaType.valueOf("application/json;charset=UTF-8"));
                return objectMapper.writeValueAsString(ApiResponse.success(body));
            } catch (JsonProcessingException e) {
                log.error("[GlobalResponseWrapper] Lỗi serialize JSON string: ", e);
                return ApiResponse.error(500, "Lỗi xử lý dữ liệu phản hồi");
            }
        }

        // Tự động bọc dữ liệu thành công
        return ApiResponse.success(body);
    }
}
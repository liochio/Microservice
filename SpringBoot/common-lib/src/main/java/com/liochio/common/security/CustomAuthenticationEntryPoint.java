package com.liochio.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liochio.common.dto.ApiResponse;
import com.liochio.common.exception.ErrorCode;
import com.liochio.common.i18n.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * ==============================================================================
 * Xử Lý Lỗi Xác Thực 401 Unauthorized (Custom Authentication Entry Point)
 * ==============================================================================
 * 
 * Mục đích:
 * - Bắt các yêu cầu chưa gửi Token, Token hết hạn hoặc sai chữ ký khi tiếp cận
 *   các Endpoint được bảo vệ, trả về JSON ApiResponse {status: 401, ...} chuẩn hóa.
 * 
 * Khi nào gọi:
 * - Spring Security Filter Chain kích hoạt khi xác thực thất bại.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final MessageService messageService;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        log.warn("[AuthenticationEntryPoint] 401 Unauthorized tại URI: {} - Lý do: {}", 
                request.getRequestURI(), authException.getMessage());

        response.setCharacterEncoding(java.nio.charset.StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String message = messageService.getMessage(ErrorCode.UNAUTHENTICATED.getMessageKey());
        ApiResponse<Object> apiResponse = ApiResponse.error(ErrorCode.UNAUTHENTICATED.getCode(), message);

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
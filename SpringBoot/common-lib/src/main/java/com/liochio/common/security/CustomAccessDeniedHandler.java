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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * ==============================================================================
 * Xử Lý Lỗi Phân Quyền 403 Forbidden (Custom Access Denied Handler)
 * ==============================================================================
 * 
 * Mục đích:
 * - Bắt các yêu cầu của người dùng đã đăng nhập nhưng không đủ quyền hạn (Role/Permission)
 *   để thực hiện hành động, trả về JSON ApiResponse {status: 403, ...} chuẩn hóa.
 * 
 * Khi nào gọi:
 * - Spring Security kích hoạt khi người dùng vi phạm quyền hạn truy cập tài nguyên.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final MessageService messageService;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        log.warn("[AccessDeniedHandler] 403 Forbidden tại URI: {} - Lý do: {}", 
                request.getRequestURI(), accessDeniedException.getMessage());

        response.setCharacterEncoding(java.nio.charset.StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        String message = messageService.getMessage(ErrorCode.UNAUTHORIZED.getMessageKey());
        ApiResponse<Object> apiResponse = ApiResponse.error(ErrorCode.UNAUTHORIZED.getCode(), message);

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
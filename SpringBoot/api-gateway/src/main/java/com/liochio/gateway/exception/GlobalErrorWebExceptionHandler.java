package com.liochio.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.cloud.gateway.support.TimeoutException;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ==============================================================================
 * Bộ Xử Lý Ngoại Lệ Mạng & Gateway Toàn Cục (Global Error Web Exception Handler)
 * ==============================================================================
 * 
 * Mục đích:
 * 1. Bắt 100% ngoại lệ cấp mạng (404 Route Not Found, 502 Bad Gateway, 503 Service Unavailable, 504 Timeout).
 * 2. Chuẩn hóa format phản hồi JSON 'ApiResponse<T>' đồng nhất với toàn bộ hệ sinh thái Microservices.
 * 3. Loại bỏ hoàn toàn trang lỗi HTML Whitelabel mặc định của Spring Boot / Netty.
 */
@Slf4j
@Component
@Order(-2) // Ưu tiên chạy trước DefaultErrorWebExceptionHandler của Spring
@RequiredArgsConstructor
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        HttpStatus status = determineHttpStatus(ex);
        String message = determineErrorMessage(ex, status);
        String path = exchange.getRequest().getURI().getPath();
        String traceId = exchange.getRequest().getHeaders().getFirst("X-Trace-ID");
        if (traceId == null || traceId.isBlank()) {
            traceId = exchange.getRequest().getHeaders().getFirst("CF-Ray");
        }
        if (traceId == null || traceId.isBlank()) {
            traceId = "tr_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }

        log.error("[GATEWAY_EXCEPTION] traceId={} path={} status={} error={}",
                traceId != null ? traceId : "NO_TRACE", path, status.value(), ex.getMessage());

        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.valueOf("application/json;charset=UTF-8"));
        if (traceId != null && !traceId.isBlank()) {
            response.getHeaders().set("X-Trace-ID", traceId);
            response.getHeaders().set("x-correlation-id", traceId);
            String cfRay = exchange.getRequest().getHeaders().getFirst("CF-Ray");
            if (cfRay != null && !cfRay.isBlank()) {
                response.getHeaders().set("CF-Ray", cfRay);
            }
        }

        Map<String, Object> errorBody = new LinkedHashMap<>();
        errorBody.put("status", status.value());
        errorBody.put("message", message);
        errorBody.put("data", null);
        errorBody.put("timestamp", Instant.now().toString());
        if (traceId != null) {
            errorBody.put("traceId", traceId);
        }

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsString(errorBody).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            bytes = ("{\"status\":" + status.value() + ",\"message\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    private HttpStatus determineHttpStatus(Throwable ex) {
        if (ex instanceof NotFoundException) {
            return HttpStatus.NOT_FOUND;
        } else if (ex instanceof ResponseStatusException rse) {
            return HttpStatus.valueOf(rse.getStatusCode().value());
        } else if (ex instanceof TimeoutException || ex instanceof java.util.concurrent.TimeoutException) {
            return HttpStatus.GATEWAY_TIMEOUT;
        } else if (ex instanceof ConnectException) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        } else if (ex.getCause() instanceof ConnectException) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        } else if (ex.getCause() instanceof io.netty.channel.ConnectTimeoutException) {
            return HttpStatus.GATEWAY_TIMEOUT;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String determineErrorMessage(Throwable ex, HttpStatus status) {
        if (status == HttpStatus.NOT_FOUND) {
            return "Đường dẫn tài nguyên không tồn tại trên hệ thống (Route Not Found)";
        } else if (status == HttpStatus.GATEWAY_TIMEOUT) {
            return "Dịch vụ đích không phản hồi kịp thời trong giới hạn thời gian (5000ms Gateway Timeout)";
        } else if (status == HttpStatus.SERVICE_UNAVAILABLE) {
            return "Dịch vụ đích hiện không khả dụng hoặc đang bảo trì (503 Service Unavailable)";
        } else if (status == HttpStatus.BAD_GATEWAY) {
            return "Không thể kết nối đến máy chủ đích (502 Bad Gateway)";
        } else if (status == HttpStatus.UNAUTHORIZED) {
            return "Chưa được xác thực hoặc phiên đăng nhập đã hết hạn (401 Unauthorized)";
        } else if (status == HttpStatus.FORBIDDEN) {
            return "Bạn không có quyền truy cập tài nguyên này (403 Forbidden)";
        }
        return ex.getMessage() != null && !ex.getMessage().isBlank() ? ex.getMessage() : "Lỗi xử lý nội bộ tại API Gateway";
    }
}

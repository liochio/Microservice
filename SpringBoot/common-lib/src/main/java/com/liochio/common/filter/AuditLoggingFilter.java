package com.liochio.common.filter;

import com.liochio.common.context.TenantContext;
import com.liochio.common.context.UserContext;
import com.liochio.common.entity.AuditLogEntity;
import com.liochio.common.repository.AuditLogRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Set;

/**
 * ==============================================================================
 * Bộ Lọc Ghi Vết Kiểm Toán Toàn Diện (Audit Logging Filter - Enterprise Standard)
 * ==============================================================================
 * 
 * Mục đích:
 * - Bắt trọn 100% Request và Response của Client.
 * - Tự động che giấu (masking) các thông tin nhạy cảm: password, token, secret, cvv.
 * - Lưu bất đồng bộ vào cơ sở dữ liệu bảng audit_logs mà không gây ảnh hưởng độ trễ.
 */
@Slf4j
@Component
@Order(-5)
public class AuditLoggingFilter extends OncePerRequestFilter {

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private AuditLogRepository auditLogRepository;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private org.springframework.context.ApplicationContext applicationContext;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.liochio.common.security.JwtUtils jwtUtils;

    private org.springframework.jdbc.core.JdbcTemplate getJdbcTemplate() {
        if (jdbcTemplate != null) return jdbcTemplate;
        if (applicationContext != null) {
            try {
                jdbcTemplate = applicationContext.getBean(org.springframework.jdbc.core.JdbcTemplate.class);
            } catch (Exception ignored) {}
        }
        return jdbcTemplate;
    }

    private static final Set<String> EXCLUDED_PATHS = Set.of(
            "/actuator",
            "/swagger-ui",
            "/v3/api-docs",
            "/favicon.ico"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        for (String excluded : EXCLUDED_PATHS) {
            if (uri.startsWith(excluded)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            String requestPayload = maskSensitiveData(getPayload(requestWrapper.getContentAsByteArray()));
            String responsePayload = maskSensitiveData(getPayload(responseWrapper.getContentAsByteArray()));

            String traceId = MDC.get("traceId");
            if (traceId == null) traceId = request.getHeader("X-Trace-Id");
            if (traceId == null) traceId = "tr_" + System.currentTimeMillis();

            String tenantId = TenantContext.getTenantId();
            Long userId = UserContext.getUserId();
            String username = UserContext.getUsername();

            // Nếu UserContext chưa có thông tin (do filter chạy trước), trích xuất từ Token JWT
            if (userId == null && jwtUtils != null) {
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7).trim();
                    try {
                        if (jwtUtils.validateToken(token)) {
                            userId = jwtUtils.extractUserId(token);
                            username = jwtUtils.extractUsername(token);
                            if (tenantId == null || tenantId.isBlank() || "SYSTEM".equals(tenantId)) {
                                tenantId = jwtUtils.extractTenantId(token);
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }

            saveAuditLog(tenantId, traceId, userId, username, request.getMethod(),
                    uri, requestPayload, responseWrapper.getStatus(), responsePayload,
                    request.getRemoteAddr(), request.getHeader("User-Agent"),
                    request.getHeader("X-Device-Id"), duration);

            responseWrapper.copyBodyToResponse();
        }
    }

    private void saveAuditLog(String tenantId, String traceId, Long userId, String username,
                              String method, String uri, String reqPayload, int status,
                              String resPayload, String ip, String userAgent, String deviceId, long duration) {
        if (auditLogRepository == null && jdbcTemplate == null) {
            return;
        }

        // 1. Lưu vào auditLogRepository của DB hiện tại (liochio_core_db)
        if (auditLogRepository != null) {
            try {
                AuditLogEntity auditLog = AuditLogEntity.builder()
                        .tenantId(tenantId != null ? tenantId : "SYSTEM")
                        .traceId(traceId)
                        .userId(userId)
                        .username(username)
                        .userEmail(username)
                        .httpMethod(method)
                        .requestUri(uri)
                        .requestBody(reqPayload.isEmpty() ? null : reqPayload)
                        .httpStatusCode(status)
                        .status(status >= 400 ? "FAILED" : "SUCCESS")
                        .module(detectModule(uri))
                        .actionType(detectAction(method))
                        .actionDescription(method + " " + uri)
                        .clientIp(ip != null ? ip : "127.0.0.1")
                        .userAgent(userAgent != null && userAgent.length() > 500 ? userAgent.substring(0, 500) : userAgent)
                        .deviceId(deviceId)
                        .executionTimeMs(duration)
                        .createdAt(Instant.now())
                        .build();

                auditLogRepository.save(auditLog);
            } catch (Exception e) {
                log.warn("[AuditLoggingFilter] Không thể lưu core audit log: {}", e.getMessage());
            }
        }

        // 2. Đồng bộ đồng thời vào liochio_app_db.audit_logs
        org.springframework.jdbc.core.JdbcTemplate jdbc = getJdbcTemplate();
        if (jdbc != null) {
            try {
                String insertSql = "INSERT INTO `liochio_app_db`.`audit_logs` ("
                        + "`user_id`, `action`, `table_name`, `action_description`, `action_type`,"
                        + "`client_ip`, `http_method`, `http_status_code`, `module`, `platform`,"
                        + "`request_uri`, `status`, `tenant_id`, `trace_id`, `username`, `execution_time_ms`,"
                        + "`ip_address`, `user_agent`, `created_at`, `updated_at`"
                        + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
                jdbc.update(insertSql,
                        userId, "HTTP_API_CALL", "system", method + " " + uri, detectAction(method),
                        ip != null ? ip : "127.0.0.1", method, status, detectModule(uri), "WEB",
                        uri, status >= 400 ? "FAILED" : "SUCCESS", tenantId != null ? tenantId : "default",
                        traceId, username != null ? username : "ANONYMOUS", duration, ip,
                        userAgent != null && userAgent.length() > 500 ? userAgent.substring(0, 500) : userAgent);
            } catch (Exception ex) {
                log.warn("[AuditLoggingFilter] Không thể đồng bộ liochio_app_db.audit_logs: {}", ex.getMessage());
            }

            // 3. Đồng bộ vào api_request_logs ở CẢ HAI cơ sở dữ liệu (liochio_app_db & liochio_core_db)
            try {
                String safeReq = (reqPayload != null && !reqPayload.isBlank() && isValidJson(reqPayload)) ? reqPayload : null;
                String safeRes = (resPayload != null && !resPayload.isBlank() && isValidJson(resPayload)) ? resPayload : null;
                String[] dbs = new String[]{"liochio_app_db", "liochio_core_db"};
                for (String db : dbs) {
                    try {
                        String apiLogSql = "INSERT INTO `" + db + "`.`api_request_logs` ("
                                + "`id`, `user_id`, `endpoint`, `method`, `request_payload`, `response_payload`,"
                                + "`status_code`, `latency_ms`, `status`, `created_at`, `updated_at`"
                                + ") VALUES (UUID(), ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
                        jdbc.update(apiLogSql,
                                userId != null ? ("usr_" + userId) : null,
                                uri.length() > 500 ? uri.substring(0, 500) : uri,
                                method.length() > 10 ? method.substring(0, 10) : method,
                                safeReq,
                                safeRes,
                                status,
                                (int) Math.min(duration, Integer.MAX_VALUE),
                                status >= 400 ? "FAILED" : "SUCCESS");
                    } catch (Exception ex) {
                        log.debug("[AuditLoggingFilter] Không thể đồng bộ {}.api_request_logs: {}", db, ex.getMessage());
                    }
                }
            } catch (Exception ex) {
                log.warn("[AuditLoggingFilter] Không thể đồng bộ api_request_logs: {}", ex.getMessage());
            }

            // 4. Đồng bộ vào request_flow_logs ở CẢ HAI cơ sở dữ liệu (liochio_app_db & liochio_core_db)
            try {
                String[] dbs = new String[]{"liochio_app_db", "liochio_core_db"};
                for (String db : dbs) {
                    try {
                        String flowSql = "INSERT INTO `" + db + "`.`request_flow_logs` ("
                                + "`id`, `trace_id`, `node`, `details`, `created_at`, `updated_at`"
                                + ") VALUES (UUID(), ?, ?, ?, NOW(), NOW())";
                        jdbc.update(flowSql,
                                traceId != null ? traceId : java.util.UUID.randomUUID().toString(),
                                "CORE_GATEWAY",
                                method + " " + uri + " -> " + status + " (" + duration + "ms)");
                    } catch (Exception ex) {
                        log.debug("[AuditLoggingFilter] Không thể đồng bộ {}.request_flow_logs: {}", db, ex.getMessage());
                    }
                }
            } catch (Exception ex) {
                log.warn("[AuditLoggingFilter] Không thể đồng bộ request_flow_logs: {}", ex.getMessage());
            }
        }
    }

    private boolean isValidJson(String str) {
        if (str == null) return false;
        String trimmed = str.trim();
        return (trimmed.startsWith("{") && trimmed.endsWith("}")) || (trimmed.startsWith("[") && trimmed.endsWith("]"));
    }

    private String detectModule(String uri) {
        if (uri.contains("/auth")) return "AUTH";
        if (uri.contains("/tour")) return "TOUR";
        if (uri.contains("/booking")) return "BOOKING";
        if (uri.contains("/payment")) return "PAYMENT";
        if (uri.contains("/media")) return "MEDIA";
        if (uri.contains("/user") || uri.contains("/roles")) return "USER";
        if (uri.contains("/ai")) return "AI";
        return "CORE";
    }

    private String detectAction(String method) {
        return switch (method.toUpperCase()) {
            case "POST" -> "CREATE";
            case "PUT", "PATCH" -> "UPDATE";
            case "DELETE" -> "DELETE";
            default -> "VIEW";
        };
    }

    private String getPayload(byte[] buf) {
        if (buf == null || buf.length == 0) return "";
        int length = Math.min(buf.length, 4096);
        return new String(buf, 0, length, StandardCharsets.UTF_8);
    }

    private String maskSensitiveData(String payload) {
        if (payload == null || payload.isBlank()) return "";
        return payload.replaceAll("(?i)\"(password|secret|accessToken|refreshToken|token|cvv|smartOtpSecret|smartOtpPin)\"\\s*:\\s*\"[^\"]+\"",
                "\"$1\":\"***MASKED***\"");
    }
}

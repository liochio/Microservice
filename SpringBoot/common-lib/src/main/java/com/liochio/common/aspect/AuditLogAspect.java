package com.liochio.common.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liochio.common.annotation.AuditLog;
import com.liochio.common.context.TenantContext;
import com.liochio.common.context.UserContext;
import com.liochio.common.entity.AuditLogEntity;
import com.liochio.common.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;

/**
 * ==============================================================================
 * Khía Cạnh Ghi Vết Nghiệp Vụ Chuyên Sâu (Business Audit Log Aspect)
 * ==============================================================================
 * 
 * Mục đích:
 * - Bắt trọn vẹn ngữ cảnh thao tác của các Controller / Service đánh dấu '@AuditLog'.
 * - Đo thời gian thực thi 'execution_time_ms', dữ liệu cũ/mới, kết quả thành công/thất bại.
 * - Lưu bản ghi kiểm toán toàn diện vào bảng 'audit_logs'.
 */
@Slf4j
@Aspect
@Component
@Order(20)
@RequiredArgsConstructor
public class AuditLogAspect {

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private AuditLogRepository auditLogRepository;

    private final ObjectMapper objectMapper;

    @Around("@annotation(auditLog)")
    public Object handleAuditLog(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        HttpServletRequest request = null;
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            request = attributes.getRequest();
        }

        String traceId = MDC.get("traceId");
        if (traceId == null && request != null) {
            traceId = request.getHeader("X-Trace-Id");
        }
        if (traceId == null) {
            traceId = "tr_" + System.currentTimeMillis();
        }

        String tenantId = TenantContext.getTenantId();
        Long userId = UserContext.getUserId();
        String username = UserContext.getUsername();
        String clientIp = request != null ? request.getRemoteAddr() : "127.0.0.1";
        String userAgent = request != null ? request.getHeader("User-Agent") : "Unknown";
        String deviceId = request != null ? request.getHeader("X-Device-Id") : null;
        String requestUri = request != null ? request.getRequestURI() : joinPoint.getSignature().toShortString();
        String httpMethod = request != null ? request.getMethod() : "EXEC";

        String requestBody = null;
        if (auditLog.logRequestBody() && joinPoint.getArgs() != null && joinPoint.getArgs().length > 0) {
            try {
                requestBody = maskSensitiveData(objectMapper.writeValueAsString(joinPoint.getArgs()));
            } catch (Exception ignored) {}
        }

        Object result = null;
        Throwable error = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable t) {
            error = t;
            throw t;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            String responseBody = null;
            if (auditLog.logResponseBody() && result != null) {
                try {
                    responseBody = maskSensitiveData(objectMapper.writeValueAsString(result));
                } catch (Exception ignored) {}
            }

            String status = (error == null) ? "SUCCESS" : "FAILED";
            int statusCode = (error == null) ? 200 : 500;
            String errorMsg = (error != null) ? error.getMessage() : null;

            saveAuditLogAsync(tenantId, traceId, userId, username, clientIp, userAgent, deviceId,
                    auditLog.module(), auditLog.action().name(), auditLog.description(),
                    httpMethod, requestUri, requestBody, responseBody, status, statusCode, errorMsg, duration);
        }
    }

    private void saveAuditLogAsync(String tenantId, String traceId, Long userId, String username,
                                   String clientIp, String userAgent, String deviceId,
                                   String module, String actionType, String description,
                                   String method, String uri, String reqBody, String resBody,
                                   String status, int statusCode, String errorMsg, long duration) {
        if (auditLogRepository == null) return;
        try {
            AuditLogEntity entity = AuditLogEntity.builder()
                    .tenantId(tenantId != null ? tenantId : "SYSTEM")
                    .traceId(traceId)
                    .userId(userId)
                    .username(username)
                    .userEmail(username)
                    .clientIp(clientIp)
                    .userAgent(userAgent != null && userAgent.length() > 500 ? userAgent.substring(0, 500) : userAgent)
                    .deviceId(deviceId)
                    .module(module)
                    .actionType(actionType)
                    .actionDescription(description.isEmpty() ? (actionType + " on " + uri) : description)
                    .httpMethod(method)
                    .requestUri(uri)
                    .requestBody(reqBody)
                    .newData(resBody)
                    .status(status)
                    .httpStatusCode(statusCode)
                    .errorMessage(errorMsg)
                    .executionTimeMs(duration)
                    .createdAt(Instant.now())
                    .build();

            auditLogRepository.save(entity);
        } catch (Exception e) {
            log.warn("[AuditLogAspect] Không thể lưu audit log: {}", e.getMessage());
        }
    }

    private String maskSensitiveData(String json) {
        if (json == null || json.isBlank()) return "";
        return json.replaceAll("(?i)\"(password|secret|accessToken|refreshToken|token|cvv|smartOtpSecret|smartOtpPin)\"\\s*:\\s*\"[^\"]+\"",
                "\"$1\":\"***MASKED***\"");
    }
}

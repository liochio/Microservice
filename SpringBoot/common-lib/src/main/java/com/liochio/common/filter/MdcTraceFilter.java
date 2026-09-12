package com.liochio.common.filter;

import com.liochio.common.constant.AppConstants;
import com.liochio.common.constant.HeaderConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * ==============================================================================
 * Bộ Lọc Truy Vết Phân Tán (Distributed Tracing & MDC Filter)
 * ==============================================================================
 * 
 * Mục đích:
 * - Trích xuất hoặc tự sinh `traceId` (mã định danh duy nhất của request).
 * - Nạp `traceId`, `tenantId`, `userId` vào Mapped Diagnostic Context (MDC) của SLF4J
 *   để tự động xuất hiện trên mọi dòng log console và file log của microservice.
 * - Trả `X-Trace-ID` về Header của Response để Frontend/Client có thể dùng tra cứu khi gặp sự cố.
 * 
 * Khi nào gọi:
 * - Chạy ở mức ưu tiên cao nhất (HIGHEST_PRECEDENCE) ngay khi request đi vào microservice.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcTraceFilter extends OncePerRequestFilter {

    public static final String MDC_TRACE_ID = "traceId";
    public static final String MDC_TENANT_ID = "tenantId";
    public static final String MDC_USER_ID = "userId";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String traceId = request.getHeader(HeaderConstants.X_TRACE_ID);
        if (traceId == null || traceId.isBlank()) {
            traceId = request.getHeader(HeaderConstants.X_REQUEST_ID);
        }
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }

        String tenantId = request.getHeader(HeaderConstants.X_TENANT_ID);
        if (tenantId == null || tenantId.isBlank()) {
            tenantId = AppConstants.DEFAULT_TENANT_ID;
        }

        String userId = request.getHeader(HeaderConstants.X_USER_ID);

        // Nạp vào SLF4J MDC
        MDC.put(MDC_TRACE_ID, traceId);
        MDC.put(MDC_TENANT_ID, tenantId);
        if (userId != null && !userId.isBlank()) {
            MDC.put(MDC_USER_ID, userId);
        }

        // Gắn vào response header để client theo dõi
        response.setHeader(HeaderConstants.X_TRACE_ID, traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_TRACE_ID);
            MDC.remove(MDC_TENANT_ID);
            MDC.remove(MDC_USER_ID);
            MDC.clear();
        }
    }
}

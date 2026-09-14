package com.liochio.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * ==============================================================================
 * BỘ LỌC CHUẨN RESTFUL: ĐIỀU KHIỂN BỘ NHỚ ĐỆM (REST Cache-Control Filter)
 * & BẢO VỆ HỆ THỐNG PHÂN LỚP (Layered System Security Headers)
 * ==============================================================================
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class RestCacheControlFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String uri = request.getRequestURI();

        // 1. Layered System Security Headers
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");

        // 2. Cacheable Constraint
        if (isPublicMetadata(uri)) {
            response.setHeader("Cache-Control", "public, max-age=3600, stale-while-revalidate=600");
        } else {
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicMetadata(String uri) {
        if (uri == null) return false;
        return uri.startsWith("/.well-known")
                || uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/actuator/health")
                || uri.startsWith("/favicon.ico");
    }
}

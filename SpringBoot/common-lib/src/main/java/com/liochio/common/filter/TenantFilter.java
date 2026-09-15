package com.liochio.common.filter;

import com.liochio.common.constant.AppConstants;
import com.liochio.common.constant.HeaderConstants;
import com.liochio.common.context.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * ==============================================================================
 * Bộ Lọc Định Danh Đa Khách Thuê (Tenant Extraction Filter)
 * ==============================================================================
 * 
 * Mục đích:
 * - Trích xuất mã khách thuê từ HTTP Header 'X-Tenant-ID' hoặc Host Subdomain.
 * - Nạp mã này vào 'TenantContext' (ThreadLocal) để toàn bộ các tầng Service
 *   và Hibernate Filter áp dụng điều kiện cô lập dữ liệu 'WHERE tenant_id = :tenantId'.
 * - Tự động xóa ThreadLocal ở khối finally để phòng tránh Memory Leak.
 * 
 * Khi nào gọi:
 * - Chạy ở mức ưu tiên cao nhất (HIGHEST_PRECEDENCE) trước khi xử lý nghiệp vụ.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TenantFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String tenantId = request.getHeader(HeaderConstants.X_TENANT_ID);

        // Nếu header không có, kiểm tra subdomain (ví dụ: client1.portfolio.com)
        if (tenantId == null || tenantId.isBlank()) {
            String serverName = request.getServerName();
            if (serverName != null && !serverName.matches("^[0-9.]+$") && !serverName.equalsIgnoreCase("localhost") && serverName.contains(".")) {
                String[] parts = serverName.split("\\.");
                if (parts.length > 2 && !parts[0].equalsIgnoreCase("www") && !parts[0].equalsIgnoreCase("api")) {
                    tenantId = parts[0];
                }
            }
        }

        if (tenantId == null || tenantId.isBlank()) {
            tenantId = AppConstants.DEFAULT_TENANT_ID;
        }

        TenantContext.setTenantId(tenantId);
        log.debug("[TenantFilter] Đã nạp Tenant ID: '{}' cho URI: {}", tenantId, request.getRequestURI());

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}

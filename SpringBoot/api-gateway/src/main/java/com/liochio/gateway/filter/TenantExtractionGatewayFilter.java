package com.liochio.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * ==============================================================================
 * Bộ Lọc Trích Xuất Khách Thuê Tại Gateway (Gateway Tenant Extraction Filter)
 * ==============================================================================
 * 
 * Mục đích:
 * - Tự động trích xuất Tenant ID từ Subdomain hoặc Header X-Tenant-ID
 *   và gắn vào request chuyển tiếp xuống các Microservices bên dưới.
 */
@Slf4j
@Component
public class TenantExtractionGatewayFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String tenantId = request.getHeaders().getFirst("X-Tenant-ID");

        if (tenantId == null || tenantId.isBlank()) {
            String host = request.getURI().getHost();
            if (host != null && host.contains(".")) {
                String[] parts = host.split("\\.");
                if (parts.length > 2 && !parts[0].equalsIgnoreCase("api") && !parts[0].equalsIgnoreCase("www")) {
                    tenantId = parts[0];
                }
            }
        }

        if (tenantId == null || tenantId.isBlank()) {
            tenantId = "default";
        }

        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-Tenant-ID", tenantId)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -20; // Ưu tiên chạy trước cả JWT Filter
    }
}

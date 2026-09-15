package com.liochio.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

/**
 * ==============================================================================
 * Cấu Hình Chia Sẻ Tài Nguyên Đa Nguồn (CORS Web Filter - Reactive Netty)
 * ==============================================================================
 * 
 * Mục đích:
 * - Cho phép các ứng dụng Frontend ReactJS, Vue, Next.js chạy trên các domain khác nhau
 *   truy cập an toàn vào hệ thống API Gateway.
 * - Hỗ trợ đầy đủ các method GET, POST, PUT, DELETE, PATCH, OPTIONS.
 */
@Configuration
public class GatewayCorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOriginPatterns(Collections.singletonList("*"));
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"));
        corsConfig.setAllowedHeaders(Arrays.asList(
                "Authorization", "Content-Type", "X-Tenant-ID", "Accept-Language", "Idempotency-Key",
                "X-Idempotency-Key", "X-Portal-Type",
                "X-Request-ID", "X-Trace-ID", "x-correlation-id", "CF-Ray", "CF-Connecting-IP",
                "X-Device-Id", "X-Session-ID", "X-API-Key", "*"
        ));
        corsConfig.setExposedHeaders(Arrays.asList(
                "Authorization", "X-Tenant-ID", "X-Request-ID", "X-Trace-ID", "x-correlation-id", "CF-Ray"
        ));
        corsConfig.setAllowCredentials(true);
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}

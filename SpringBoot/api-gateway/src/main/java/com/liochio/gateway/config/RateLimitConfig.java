package com.liochio.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

/**
 * ==============================================================================
 * Cấu Hình Phân Giải Khóa Giới Hạn Lưu Lượng (Business Rate Limiter Key Resolvers)
 * ==============================================================================
 * 
 * Mục đích:
 * 1. `userKeyResolver`: Giới hạn lưu lượng trên từng tài khoản người dùng (`X-User-ID`),
 *    ngăn chặn tình trạng spam API chuyển tiền, thanh toán từ cùng một tài khoản.
 * 2. `ipKeyResolver`: Giới hạn theo Real Client IP (nhận diện qua `CF-Connecting-IP`).
 * 3. `apiKeyResolver`: Giới hạn cho các đối tác / tích hợp API bên ngoài (`X-API-Key`).
 */
@Configuration
public class RateLimitConfig {

    @Bean
    @Primary
    public KeyResolver userKeyResolver() {
        return exchange -> {
            ServerHttpRequest request = exchange.getRequest();
            String userId = request.getHeaders().getFirst("X-User-ID");
            if (userId != null && !userId.isBlank()) {
                return Mono.just("user_" + userId);
            }

            String authHeader = request.getHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return Mono.just("token_" + authHeader.substring(7, Math.min(30, authHeader.length())));
            }

            return Mono.just("ip_" + extractClientIp(request));
        };
    }

    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just("ip_" + extractClientIp(exchange.getRequest()));
    }

    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> {
            String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-Key");
            if (apiKey != null && !apiKey.isBlank()) {
                return Mono.just("apikey_" + apiKey);
            }
            return Mono.just("ip_" + extractClientIp(exchange.getRequest()));
        };
    }

    private static String extractClientIp(ServerHttpRequest request) {
        String cfIp = request.getHeaders().getFirst("CF-Connecting-IP");
        if (cfIp != null && !cfIp.isBlank() && !"unknown".equalsIgnoreCase(cfIp)) {
            return cfIp.trim();
        }
        String xff = request.getHeaders().getFirst("X-Forwarded-For");
        if (xff != null && !xff.isBlank() && !"unknown".equalsIgnoreCase(xff)) {
            int comma = xff.indexOf(',');
            return comma > 0 ? xff.substring(0, comma).trim() : xff.trim();
        }
        String realIp = request.getHeaders().getFirst("X-Real-IP");
        if (realIp != null && !realIp.isBlank() && !"unknown".equalsIgnoreCase(realIp)) {
            return realIp.trim();
        }
        InetSocketAddress remote = request.getRemoteAddress();
        if (remote != null && remote.getAddress() != null) {
            return remote.getAddress().getHostAddress();
        }
        return "127.0.0.1";
    }
}

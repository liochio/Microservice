package com.liochio.common.config;

import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * ==============================================================================
 * Cấu Hình Khả Năng Chịu Lỗi & Cách Ly (Resilience4j Configuration - Enterprise Standard)
 * ==============================================================================
 * 
 * Mục đích:
 * - Cung cấp Circuit Breaker mặc định (ngắt cầu dao khi tỷ lệ lỗi > 50% hoặc timeout).
 * - Cung cấp Bulkhead phân vùng luồng (giới hạn concurrency tránh cạn kiệt Tomcat Thread Pool).
 * - Cung cấp TimeLimiter kiểm soát thời gian chờ tối đa khi gọi service ngoài.
 */
@Slf4j
@Configuration
public class ResilienceConfig {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10) // Xét 10 request gần nhất
                .minimumNumberOfCalls(5) // Tối thiểu 5 cuộc gọi để tính toán
                .failureRateThreshold(50.0f) // Tỷ lệ lỗi >= 50% -> Ngắt mạch OPEN
                .slowCallRateThreshold(70.0f) // Tỷ lệ gọi chậm >= 70% -> Ngắt mạch OPEN
                .slowCallDurationThreshold(Duration.ofSeconds(2)) // Gọi chậm nếu > 2 giây
                .waitDurationInOpenState(Duration.ofSeconds(10)) // Giữ trạng thái OPEN 10 giây trước khi thử lại
                .permittedNumberOfCallsInHalfOpenState(3) // Thử 3 cuộc gọi ở HALF_OPEN
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(defaultConfig);
        registry.getEventPublisher().onEntryAdded(entry -> 
                log.info("[ResilienceConfig] Đã khởi tạo CircuitBreaker '{}'", entry.getAddedEntry().getName()));

        return registry;
    }

    @Bean
    public BulkheadRegistry bulkheadRegistry() {
        BulkheadConfig defaultConfig = BulkheadConfig.custom()
                .maxConcurrentCalls(25) // Tối đa 25 cuộc gọi đồng thời cho 1 domain
                .maxWaitDuration(Duration.ofMillis(500)) // Đợi tối đa 500ms trước khi từ chối
                .build();

        return BulkheadRegistry.of(defaultConfig);
    }

    @Bean
    public TimeLimiterRegistry timeLimiterRegistry() {
        TimeLimiterConfig defaultConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(3)) // Hết hạn sau 3s nếu service ngoài không phản hồi
                .cancelRunningFuture(true)
                .build();

        return TimeLimiterRegistry.of(defaultConfig);
    }
}

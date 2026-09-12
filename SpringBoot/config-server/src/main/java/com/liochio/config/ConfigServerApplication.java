package com.liochio.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * ==============================================================================
 * Máy Chủ Quản Lý Cấu Hình Tập Trung (Spring Cloud Config Server)
 * ==============================================================================
 * 
 * Mục đích:
 * - Tập trung hóa toàn bộ cấu hình (Database URL, Credentials, Redis, Security,
 *   Eureka URL, Resilience4j rules) của tất cả các Microservices.
 * - Cho phép thay đổi cấu hình mà không cần phải rebuild/redeploy toàn bộ ứng dụng.
 * 
 * Cổng chạy mặc định: 8888 ({@code http://localhost:8888})
 */
@SpringBootApplication
@EnableConfigServer
@EnableDiscoveryClient
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
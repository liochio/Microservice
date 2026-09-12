package com.liochio.registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * ==============================================================================
 * Máy Chủ Đăng Ký & Khám Phá Dịch Vụ (Netflix Eureka Service Registry)
 * ==============================================================================
 * 
 * Mục đích:
 * - Đóng vai trò là trung tâm đăng ký (Service Registry) quản lý danh sách
 *   toàn bộ các Microservices đang hoạt động trong hệ thống.
 * - Cho phép các Microservices tự động phát hiện nhau (Service Discovery)
 *   và cân bằng tải (Client-side Load Balancing).
 * 
 * Cổng chạy mặc định: 8761 ({@code http://localhost:8761})
 */
@SpringBootApplication
@EnableEurekaServer
public class ServiceRegistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceRegistryApplication.class, args);
    }
}
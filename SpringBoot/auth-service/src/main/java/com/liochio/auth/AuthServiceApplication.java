package com.liochio.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * ==============================================================================
 * Dịch Vụ Xác Thực & Phân Quyền (Auth & RBAC Service)
 * ==============================================================================
 * 
 * Mục đích:
 * - Đăng nhập, đăng ký, cấp phát và thu hồi JWT Token, Refresh Token Rotation.
 * - Quản lý người dùng, vai trò (Roles), quyền hạn (Permissions) động.
 * 
 * Cổng chạy mặc định: 8081 ({@code http://localhost:8081})
 */
@SpringBootApplication(scanBasePackages = {"com.liochio.auth", "com.liochio.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.liochio.auth.client")
@EnableJpaRepositories(basePackages = {"com.liochio.auth", "com.liochio.common"})
@EntityScan(basePackages = {"com.liochio.auth", "com.liochio.common"})
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}

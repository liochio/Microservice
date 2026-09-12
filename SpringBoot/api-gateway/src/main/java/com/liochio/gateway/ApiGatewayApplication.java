package com.liochio.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * ==============================================================================
 * Cổng API Trung Tâm (Spring Cloud API Gateway)
 * ==============================================================================
 * 
 * Mục đích:
 * - Điểm tiếp nhận duy nhất (Single Entry Point) cho toàn bộ Client/Frontend (Port 8080).
 * - Chịu trách nhiệm:
 *   1. Định tuyến thông minh tới các microservices qua Eureka (lb://auth-service, lb://entity-service...)
 *   2. Kiểm soát CORS đa nguồn
 *   3. Lọc xác thực JWT và chuyển tiếp thông tin User ID / Tenant ID qua HTTP Headers
 *   4. Tự động chuyển đổi `X-Tenant-ID`
 * 
 * Cổng chạy mặc định: 8080 (http://localhost:8080)
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
package com.liochio.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * ==============================================================================
 * Dịch Vụ Trí Tuệ Nhân Tạo & AI Chatbot (AI Dedicated Microservice)
 * ==============================================================================
 * Database riêng: db_ai_vector
 * Cổng chạy: 8086 (http://localhost:8086)
 */
@SpringBootApplication(scanBasePackages = {"com.liochio.ai", "com.liochio.common"})
@EnableDiscoveryClient
@EnableJpaRepositories(basePackages = {"com.liochio.ai"})
@EntityScan(basePackages = {"com.liochio.ai"})
public class AiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiServiceApplication.class, args);
    }
}

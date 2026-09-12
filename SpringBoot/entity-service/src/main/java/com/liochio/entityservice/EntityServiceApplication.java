package com.liochio.entityservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * ==============================================================================
 * Dịch Vụ Quản Trị Thực Thể Động & Portfolio (Entity & Dynamic Engine Service)
 * ==============================================================================
 * 
 * Mục đích:
 * - Dynamic Schema Hybrid EAV + JSON Engine cho thực thể tùy biến.
 * - Server-Driven UI layout config, Form Definitions, Navigation Menus.
 * - CRUD danh mục dự án Portfolio, bài viết blog, kỹ năng, trải nghiệm.
 * 
 * Cổng chạy mặc định: 8082 ({@code http://localhost:8082})
 */
@SpringBootApplication(scanBasePackages = {"com.liochio.entityservice", "com.liochio.common"})
@EnableDiscoveryClient
@EnableCaching
@EnableJpaRepositories(basePackages = {"com.liochio.entityservice", "com.liochio.common"})
@EntityScan(basePackages = {"com.liochio.entityservice", "com.liochio.common"})
public class EntityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EntityServiceApplication.class, args);
    }
}

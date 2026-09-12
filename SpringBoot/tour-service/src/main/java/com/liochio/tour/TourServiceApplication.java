package com.liochio.tour;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * ==============================================================================
 * Dịch Vụ Du Lịch & Khám Phá (Tour & Travel Dedicated Microservice)
 * ==============================================================================
 * Database riêng: db_tour
 * Cổng chạy: 8091 (http://localhost:8091)
 */
@SpringBootApplication(scanBasePackages = {"com.liochio.tour", "com.liochio.common"})
@EnableDiscoveryClient
@EnableJpaRepositories(basePackages = {"com.liochio.tour"})
@EntityScan(basePackages = {"com.liochio.tour"})
public class TourServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TourServiceApplication.class, args);
    }
}

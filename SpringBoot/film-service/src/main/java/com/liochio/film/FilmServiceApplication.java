package com.liochio.film;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * ==============================================================================
 * Dịch Vụ Điện Ảnh & Phim Truyền Hình (Film & Movie Dedicated Microservice)
 * ==============================================================================
 * Database riêng: db_film
 * Cổng chạy: 8093 (http://localhost:8093)
 */
@SpringBootApplication(scanBasePackages = {"com.liochio.film", "com.liochio.common"})
@EnableDiscoveryClient
@EnableJpaRepositories(basePackages = {"com.liochio.film"})
@EntityScan(basePackages = {"com.liochio.film"})
public class FilmServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FilmServiceApplication.class, args);
    }
}

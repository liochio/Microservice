package com.liochio.music;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * ==============================================================================
 * Dịch Vụ Âm Nhạc & Audio (Music & Audio Dedicated Microservice)
 * ==============================================================================
 * Database riêng: db_music
 * Cổng chạy: 8092 (http://localhost:8092)
 */
@SpringBootApplication(scanBasePackages = {"com.liochio.music", "com.liochio.common"})
@EnableDiscoveryClient
@EnableJpaRepositories(basePackages = {"com.liochio.music"})
@EntityScan(basePackages = {"com.liochio.music"})
public class MusicServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MusicServiceApplication.class, args);
    }
}

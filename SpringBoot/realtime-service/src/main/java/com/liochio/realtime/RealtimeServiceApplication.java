package com.liochio.realtime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * ==============================================================================
 * Dịch Vụ Thời Gian Thực & WebSocket Hub (Realtime Dedicated Microservice)
 * ==============================================================================
 * Cổng chạy: 8087 (http://localhost:8087)
 */
@SpringBootApplication(
        scanBasePackages = {"com.liochio.realtime", "com.liochio.common"},
        exclude = {
                org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
                org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration.class
        }
)
@EnableDiscoveryClient
public class RealtimeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RealtimeServiceApplication.class, args);
    }
}

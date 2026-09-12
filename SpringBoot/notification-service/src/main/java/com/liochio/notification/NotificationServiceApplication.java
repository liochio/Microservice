package com.liochio.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * ==============================================================================
 * Trung Tâm Phát Tán Thông Báo Đa Kênh (Notification Hub Service)
 * ==============================================================================
 * 
 * Mục đích:
 * - Tiếp nhận và phát tán thông báo qua Email (SMTP/Mailpit), Telegram Webhook,
 *   và đẩy thời gian thực STOMP WebSocket tới Client.
 * 
 * Cổng chạy mặc định: 8084 ({@code http://localhost:8084})
 */
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.liochio.notification", "com.liochio.common"})
@EnableDiscoveryClient
@EnableJpaRepositories(basePackages = {"com.liochio.notification", "com.liochio.common"})
@EntityScan(basePackages = {"com.liochio.notification", "com.liochio.common"})
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}

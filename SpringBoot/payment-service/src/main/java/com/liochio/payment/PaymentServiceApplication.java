package com.liochio.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * ==============================================================================
 * Trung Tâm Cổng Thanh Toán (Payment Hub & Gateway Service)
 * ==============================================================================
 * 
 * Mục đích:
 * - Tích hợp các cổng thanh toán (VNPay, MoMo, Stripe, PayPal) qua Strategy Pattern.
 * - Xử lý tạo Payment URL và xác thực IPN Webhook callback có chữ ký số bảo mật.
 * 
 * Cổng chạy mặc định: 8085 ({@code http://localhost:8085})
 */
@SpringBootApplication(scanBasePackages = {"com.liochio.payment", "com.liochio.common"})
@EnableDiscoveryClient
@EnableJpaRepositories(basePackages = {"com.liochio.payment", "com.liochio.common"})
@EntityScan(basePackages = {"com.liochio.payment", "com.liochio.common"})
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}

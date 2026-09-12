package com.liochio.media;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * ==============================================================================
 * Dịch Vụ Lưu Trữ & Xử Lý Đa Phương Tiện (Media Service)
 * ==============================================================================
 * 
 * Mục đích:
 * - Tiếp nhận và xử lý tải lên tệp tin dung lượng lớn (Chunk Upload),
 *   tối ưu hóa hình ảnh, tích hợp linh hoạt các Storage Provider (Local, Cloudinary, S3/R2).
 * 
 * Cổng chạy mặc định: 8083 ({@code http://localhost:8083})
 */
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.liochio.media", "com.liochio.common"})
@EnableDiscoveryClient
@EnableJpaRepositories(basePackages = {"com.liochio.media", "com.liochio.common"})
@EntityScan(basePackages = {"com.liochio.media", "com.liochio.common"})
public class MediaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MediaServiceApplication.class, args);
    }
}

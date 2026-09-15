package com.liochio.common.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * ==============================================================================
 * Cấu Hình Xử Lý JSON Toàn Cục (Jackson Configuration)
 * ==============================================================================
 * 
 * Mục đích:
 * - Cấu hình Bean ObjectMapper dùng chung toàn bộ hệ thống:
 *   + Nạp JavaTimeModule hỗ trợ Java 8 Date/Time (Instant, LocalDateTime, LocalDate).
 *   + Tắt ghi date dạng timestamp số nguyên ('WRITE_DATES_AS_TIMESTAMPS = false') -> Xuất ISO-8601 UTC string.
 *   + Bỏ qua thuộc tính lạ ('FAIL_ON_UNKNOWN_PROPERTIES = false') để tương thích ngược API DTO.
 * 
 * Khi nào gọi:
 * - Tự động được Spring Boot nạp khi khởi động và inject vào RestController / Deserializers.
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }
}
package com.liochio.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * ==============================================================================
 * Cấu Hình Redis & Bộ Nhớ Đệm Phân Tán L2 (Redis L2 Cache Configuration)
 * ==============================================================================
 * 
 * Mục đích:
 * - Cấu hình `RedisTemplate<String, Object>` với Key là String UTF-8 và Value là JSON Object
 *   được serialize qua Jackson.
 * - Hỗ trợ lưu trữ Tokens, Blacklist, Rate Limiting, Idempotency và Cache phân tán.
 * 
 * Khi nào gọi:
 * - Được nạp khi ứng dụng có kết nối tới Redis Server.
 */
@Configuration
@ConditionalOnClass(RedisConnectionFactory.class)
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}

package com.liochio.common.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;

/**
 * ==============================================================================
 * Cấu Hình Khóa Phân Tán Cho Tác Vụ Ngầm (ShedLock Distributed Lock Configuration)
 * ==============================================================================
 * 
 * Mục đích:
 * - Đồng bộ hóa các tác vụ chạy định kỳ '@Scheduled' khi hệ thống mở rộng đa node (multi-instances),
 *   đảm bảo tại một thời điểm chỉ có DUY NHẤT 1 instance thực thi tác vụ (dọn token, xóa rác, outbox relay).
 * - MẶC ĐỊNH: Sử dụng MySQL DataSource (Bảng 'shedlock') đã được tạo sẵn trong Database Core 'portfolio-engine'.
 *   Điều này giúp Scheduler hoạt động 100% độc lập, không bị crash kể cả khi Redis Server chưa bật.
 * - FALLBACK: Sử dụng Redis nếu hệ thống chạy ở chế độ không có DataSource.
 */
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
public class ShedLockConfig {

    /**
     * Ưu tiên sử dụng JDBC DataSource và bảng 'shedlock' trong MySQL.
     */
    @Bean
    @Primary
    @ConditionalOnBean(DataSource.class)
    public LockProvider jdbcLockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(
            JdbcTemplateLockProvider.Configuration.builder()
                .withJdbcTemplate(new JdbcTemplate(dataSource))
                .usingDbTime()
                .build()
        );
    }

    /**
     * Fallback dùng Redis Lock nếu service không dùng JDBC DataSource.
     */
    @Bean
    @ConditionalOnMissingBean(LockProvider.class)
    @ConditionalOnBean(RedisConnectionFactory.class)
    public LockProvider redisLockProvider(RedisConnectionFactory connectionFactory) {
        return new RedisLockProvider(connectionFactory, "portfolio-shedlock");
    }
}

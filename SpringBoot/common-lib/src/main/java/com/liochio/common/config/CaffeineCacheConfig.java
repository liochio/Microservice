package com.liochio.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.liochio.common.constant.CacheConstants;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * ==============================================================================
 * Cấu Hình Bộ Nhớ Đệm Cục Bộ L1 (Caffeine L1 Cache Configuration)
 * ==============================================================================
 * 
 * Mục đích:
 * - Cung cấp bộ nhớ đệm trên RAM máy chủ (in-memory L1 cache) cực nhanh (độ trễ microsecond)
 *   cho các dữ liệu siêu tĩnh như: Quy tắc làm sạch, Từ điển i18n, Layout UI schema, Roles & Permissions.
 * 
 * Khi nào gọi:
 * - Được Spring Cache kích hoạt qua các annotation '@Cacheable(value = "tenantSanitizeRules")'.
 */
@Configuration
@EnableCaching
public class CaffeineCacheConfig {

    @Bean
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(5000)
                .expireAfterWrite(CacheConstants.TTL_L1_CAFFEINE_SECONDS, TimeUnit.SECONDS)
                .recordStats());
        return cacheManager;
    }
}

package com.liochio.common.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.liochio.common.entity.SystemConfigEntity;
import com.liochio.common.repository.SystemConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * ==============================================================================
 * Dịch Vụ Cấu Hình Môi Trường Động Trung Tâm (Dynamic System Configuration Engine)
 * ==============================================================================
 * 
 * Mục đích:
 * 1. Đọc cấu hình theo môi trường (dev, test, prod) từ DB `system_configs`.
 * 2. Lưu vào L1 Caffeine RAM Cache (O(1) lookup < 1ms).
 * 3. Hỗ trợ Hot-Reload cấu hình không cần khởi động lại Server.
 */
@Slf4j
@Service
public class SystemConfigService {

    private final Optional<SystemConfigRepository> configRepository;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Value("${spring.application.name:global}")
    private String applicationName;

    private final Cache<String, String> configCache = Caffeine.newBuilder()
            .maximumSize(5000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    public SystemConfigService(@Autowired(required = false) SystemConfigRepository configRepository) {
        this.configRepository = Optional.ofNullable(configRepository);
    }

    public String getConfigValue(String key, String defaultValue) {
        if (key == null || key.isBlank()) return defaultValue;

        String cacheKey = activeProfile + ":" + key;
        String cached = configCache.getIfPresent(cacheKey);
        if (cached != null) {
            return cached;
        }

        if (configRepository.isPresent()) {
            try {
                Optional<SystemConfigEntity> config = configRepository.get()
                        .findActiveConfig(activeProfile, applicationName, key);
                if (config.isPresent()) {
                    String val = config.get().getConfigValue();
                    configCache.put(cacheKey, val);
                    return val;
                }
            } catch (Exception e) {
                log.warn("[SystemConfigService] Không thể đọc cấu hình key '{}' từ DB: {}", key, e.getMessage());
            }
        }

        return defaultValue;
    }

    public int getInt(String key, int defaultValue) {
        try {
            String val = getConfigValue(key, String.valueOf(defaultValue));
            return Integer.parseInt(val.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        try {
            String val = getConfigValue(key, String.valueOf(defaultValue));
            return "1".equals(val.trim()) || "true".equalsIgnoreCase(val.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public void clearCache() {
        configCache.invalidateAll();
        log.info("[SystemConfigService] ✅ Đã làm mới L1 RAM Cache cấu hình hệ thống (Hot-Reload)");
    }
}

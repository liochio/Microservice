package com.liochio.entityservice.service;

import com.liochio.common.constant.CacheConstants;
import com.liochio.common.context.TenantContext;
import com.liochio.common.exception.AppException;
import com.liochio.common.exception.ErrorCode;
import com.liochio.entityservice.dto.UiConfigurationRequest;
import com.liochio.entityservice.dto.UiConfigurationResponse;
import com.liochio.entityservice.entity.UiConfigurationEntity;
import com.liochio.entityservice.repository.UiConfigurationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ==============================================================================
 * Dịch Vụ Cấu Hình Giao Diện Server-Driven UI (UI Configuration Service)
 * ==============================================================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UiConfigService {

    private final UiConfigurationRepository uiConfigurationRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = CacheConstants.CACHE_UI_CONFIGS, key = "#pageCode", unless = "#result == null")
    public UiConfigurationResponse getByPageCode(String pageCode) {
        String tenantId = TenantContext.getTenantId();
        UiConfigurationEntity entity = uiConfigurationRepository.findByTenantIdAndPageCode(tenantId, pageCode)
                .or(() -> uiConfigurationRepository.findByPageCode(pageCode))
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        return UiConfigurationResponse.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .pageCode(entity.getPageCode())
                .themeName(entity.getThemeName())
                .layoutSchema(entity.getLayoutSchema())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Transactional
    @CacheEvict(value = CacheConstants.CACHE_UI_CONFIGS, key = "#request.pageCode")
    public UiConfigurationResponse saveOrUpdate(UiConfigurationRequest request) {
        String tenantId = TenantContext.getTenantId();

        UiConfigurationEntity entity = uiConfigurationRepository.findByTenantIdAndPageCode(tenantId, request.getPageCode())
                .orElseGet(() -> {
                    UiConfigurationEntity newEntity = new UiConfigurationEntity();
                    newEntity.setTenantId(tenantId);
                    newEntity.setPageCode(request.getPageCode());
                    return newEntity;
                });

        entity.setThemeName(request.getThemeName());
        entity.setLayoutSchema(request.getLayoutSchema());

        UiConfigurationEntity saved = uiConfigurationRepository.save(entity);
        log.info("[UiConfigService] Đã lưu cấu hình UI cho pageCode='{}', tenant='{}'", saved.getPageCode(), saved.getTenantId());

        return UiConfigurationResponse.builder()
                .id(saved.getId())
                .tenantId(saved.getTenantId())
                .pageCode(saved.getPageCode())
                .themeName(saved.getThemeName())
                .layoutSchema(saved.getLayoutSchema())
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }
}

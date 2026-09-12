package com.liochio.entityservice.service;

import com.liochio.common.constant.CacheConstants;
import com.liochio.common.context.TenantContext;
import com.liochio.common.exception.AppException;
import com.liochio.common.exception.ErrorCode;
import com.liochio.entityservice.dto.NavigationMenuRequest;
import com.liochio.entityservice.dto.NavigationMenuResponse;
import com.liochio.entityservice.entity.NavigationMenuEntity;
import com.liochio.entityservice.repository.NavigationMenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ==============================================================================
 * Dịch Vụ Quản Lý Menu Điều Hướng Động (Navigation Menu Service)
 * ==============================================================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NavigationMenuService {

    private final NavigationMenuRepository navigationMenuRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = CacheConstants.CACHE_NAVIGATION_MENUS, key = "#menuCode", unless = "#result == null")
    public NavigationMenuResponse getByMenuCode(String menuCode) {
        String tenantId = TenantContext.getTenantId();
        NavigationMenuEntity entity = navigationMenuRepository.findByTenantIdAndMenuCode(tenantId, menuCode)
                .or(() -> navigationMenuRepository.findByMenuCode(menuCode))
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        return NavigationMenuResponse.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .menuCode(entity.getMenuCode())
                .title(entity.getTitle())
                .items(entity.getItems())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Transactional
    @CacheEvict(value = CacheConstants.CACHE_NAVIGATION_MENUS, key = "#request.menuCode")
    public NavigationMenuResponse saveOrUpdate(NavigationMenuRequest request) {
        String tenantId = TenantContext.getTenantId();

        NavigationMenuEntity entity = navigationMenuRepository.findByTenantIdAndMenuCode(tenantId, request.getMenuCode())
                .orElseGet(() -> {
                    NavigationMenuEntity newEntity = new NavigationMenuEntity();
                    newEntity.setTenantId(tenantId);
                    newEntity.setMenuCode(request.getMenuCode());
                    return newEntity;
                });

        entity.setTitle(request.getTitle());
        entity.setItems(request.getItems());

        NavigationMenuEntity saved = navigationMenuRepository.save(entity);
        log.info("[NavigationMenuService] Đã lưu menu navigation '{}'", saved.getMenuCode());

        return NavigationMenuResponse.builder()
                .id(saved.getId())
                .tenantId(saved.getTenantId())
                .menuCode(saved.getMenuCode())
                .title(saved.getTitle())
                .items(saved.getItems())
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }
}

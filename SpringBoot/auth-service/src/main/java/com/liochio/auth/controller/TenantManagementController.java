package com.liochio.auth.controller;

import com.liochio.auth.entity.SystemFeatureEntity;
import com.liochio.auth.entity.TenantEntity;
import com.liochio.auth.entity.TenantFeatureEntity;
import com.liochio.auth.repository.SystemFeatureRepository;
import com.liochio.auth.repository.TenantFeatureRepository;
import com.liochio.auth.repository.TenantRepository;
import com.liochio.common.annotation.RequirePermission;
import com.liochio.common.dto.ApiResponse;
import com.liochio.common.constant.MessageConstants;
import com.liochio.common.i18n.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ==============================================================================
 * Controller Quản Lý Tenant & Gói Tính Năng (Tenant Management Controller)
 * ==============================================================================
 */
@RestController
@RequestMapping({"/api/v1/tenants", "/api/tenants"})
@RequiredArgsConstructor
@Tag(name = "Tenant Management", description = "Các API cấp phát Tenant và phân phối gói tính năng (Super Admin)")
public class TenantManagementController {

    private final TenantRepository tenantRepository;
    private final SystemFeatureRepository systemFeatureRepository;
    private final TenantFeatureRepository tenantFeatureRepository;
    private final MessageService messageService;

    @GetMapping
    @RequirePermission(value = {"system:admin"}, superAdminOnly = true)
    @Operation(summary = "Lấy danh sách tất cả các Tenant trong hệ thống")
    public ApiResponse<List<TenantEntity>> getAllTenants() {
        return ApiResponse.success(tenantRepository.findAll());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequirePermission(value = {"system:admin"}, superAdminOnly = true)
    @Operation(summary = "Khởi tạo một Tenant mới (Cá nhân / Doanh nghiệp)")
    public ApiResponse<TenantEntity> createTenant(@RequestBody TenantEntity request) {
        TenantEntity saved = tenantRepository.save(request);
        return ApiResponse.created(saved, messageService.getMessage(MessageConstants.MSG_AUTH_TENANT_CREATED));
    }

    @GetMapping("/features/catalog")
    @Operation(summary = "Lấy danh mục tính năng hệ thống (Feature Catalog)")
    public ApiResponse<List<SystemFeatureEntity>> getFeatureCatalog() {
        return ApiResponse.success(systemFeatureRepository.findByIsActiveTrue());
    }

    @GetMapping("/{tenantId}/features")
    @Operation(summary = "Lấy danh sách tính năng được cấp phát của Tenant")
    public ApiResponse<List<TenantFeatureEntity>> getTenantFeatures(@PathVariable String tenantId) {
        return ApiResponse.success(tenantFeatureRepository.findByTenantIdAndIsEnabledTrue(tenantId));
    }
}

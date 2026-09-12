package com.liochio.auth.controller;

import com.liochio.auth.dto.SystemConfigDto;
import com.liochio.auth.entity.GlobalSystemConfigEntity;
import com.liochio.auth.service.ConfigMatrixService;
import com.liochio.common.context.TenantContext;
import com.liochio.common.context.UserContext;
import com.liochio.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class ConfigMatrixController {

    private final ConfigMatrixService configMatrixService;

    @GetMapping({"/api/v1/system/configs", "/api/system/configs"})
    public ResponseEntity<ApiResponse<List<SystemConfigDto>>> getConfigs(
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "tenantId", required = false) String paramTenantId
    ) {
        String tenantId = paramTenantId != null ? paramTenantId : TenantContext.getTenantId();
        List<SystemConfigDto> configs = configMatrixService.getEffectiveConfigs(tenantId, category);
        return ResponseEntity.ok(ApiResponse.success(configs, "Lấy danh sách tham số cấu hình thành công!"));
    }

    @PostMapping({"/api/v1/system/configs/global", "/api/system/configs/global"})
    @PreAuthorize("hasAnyAuthority('SA_FIN_POLICIES:WRITE', 'SA_SEC_AML:WRITE', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<GlobalSystemConfigEntity>> updateGlobalConfig(
            @Valid @RequestBody SystemConfigDto.GlobalConfigRequest request
    ) {
        Long userId = UserContext.getUserId() != null ? UserContext.getUserId() : 1L;
        GlobalSystemConfigEntity updated = configMatrixService.updateGlobalConfig(request, userId);
        return ResponseEntity.ok(ApiResponse.success(updated, "Cập nhật tham số toàn cục thành công!"));
    }

    @PostMapping({"/api/v1/corp/configs/{configKey}/override", "/api/corp/configs/{configKey}/override"})
    @PreAuthorize("hasAnyAuthority('CORP_CONFIG_OVERRIDES:WRITE', 'ROLE_SUPER_ADMIN', 'ROLE_TENANT_ADMIN', 'ROLE_MAKER')")
    public ResponseEntity<ApiResponse<SystemConfigDto>> setTenantOverride(
            @PathVariable("configKey") String configKey,
            @RequestBody SystemConfigDto.OverrideRequest request
    ) {
        String tenantId = TenantContext.getTenantId();
        Long userId = UserContext.getUserId() != null ? UserContext.getUserId() : 1L;
        SystemConfigDto updated = configMatrixService.setTenantOverride(tenantId, configKey, request.getOverrideValue(), userId);
        return ResponseEntity.ok(ApiResponse.success(updated, "Ghi đè cấu hình doanh nghiệp thành công!"));
    }
}

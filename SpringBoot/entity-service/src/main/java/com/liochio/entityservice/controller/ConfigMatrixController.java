package com.liochio.entityservice.controller;

import com.liochio.common.context.TenantContext;
import com.liochio.common.context.UserContext;
import com.liochio.common.dto.ApiResponse;
import com.liochio.entityservice.dto.SystemConfigDto;
import com.liochio.entityservice.entity.GlobalSystemConfigEntity;
import com.liochio.entityservice.service.ConfigMatrixService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "System Config Matrix Controller", description = "Các API ma trận cấu hình toàn cục và phân cấp đa người thuê")
public class ConfigMatrixController {

    private final ConfigMatrixService configMatrixService;

    @GetMapping({"/api/v1/system/configs", "/api/system/configs", "/api/v1/configs"})
    @Operation(summary = "Lấy danh sách tham số cấu hình hiệu lực theo Tenant và Category")
    public ResponseEntity<ApiResponse<List<SystemConfigDto>>> getConfigs(
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "tenantId", required = false) String paramTenantId
    ) {
        String tenantId = paramTenantId != null ? paramTenantId : TenantContext.getTenantId();
        List<SystemConfigDto> configs = configMatrixService.getEffectiveConfigs(tenantId, category);
        return ResponseEntity.ok(ApiResponse.success(configs, "Lấy danh sách tham số cấu hình thành công!"));
    }

    @PostMapping({"/api/v1/system/configs/global", "/api/system/configs/global", "/api/v1/configs/global"})
    @PreAuthorize("hasAnyAuthority('SA_FIN_POLICIES:WRITE', 'SA_SEC_AML:WRITE', 'ROLE_SUPER_ADMIN')")
    @Operation(summary = "Cập nhật tham số cấu hình toàn cục nền tảng")
    public ResponseEntity<ApiResponse<GlobalSystemConfigEntity>> updateGlobalConfig(
            @Valid @RequestBody SystemConfigDto.GlobalConfigRequest request
    ) {
        Long userId = UserContext.getUserId() != null ? UserContext.getUserId() : 1L;
        GlobalSystemConfigEntity updated = configMatrixService.updateGlobalConfig(request, userId);
        return ResponseEntity.ok(ApiResponse.success(updated, "Cập nhật tham số toàn cục thành công!"));
    }

    @PostMapping({"/api/v1/corp/configs/{configKey}/override", "/api/corp/configs/{configKey}/override", "/api/v1/configs/{configKey}/override"})
    @PreAuthorize("hasAnyAuthority('CORP_CONFIG_OVERRIDES:WRITE', 'ROLE_SUPER_ADMIN', 'ROLE_TENANT_ADMIN', 'ROLE_MAKER')")
    @Operation(summary = "Thiết lập giá trị ghi đè cấu hình cho Tenant / Doanh nghiệp")
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

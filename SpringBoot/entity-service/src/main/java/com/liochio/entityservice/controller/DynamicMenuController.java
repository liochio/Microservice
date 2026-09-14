package com.liochio.entityservice.controller;

import com.liochio.common.context.TenantContext;
import com.liochio.common.context.UserContext;
import com.liochio.common.dto.ApiResponse;
import com.liochio.entityservice.dto.DynamicMenuNodeDto;
import com.liochio.entityservice.service.DynamicMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping({"/api/v1/menus", "/api/menus"})
@RequiredArgsConstructor
@Tag(name = "Dynamic Menu Controller", description = "Các API truy xuất cây menu phân quyền động và đa ngôn ngữ")
public class DynamicMenuController {

    private final DynamicMenuService dynamicMenuService;

    @GetMapping("/tree")
    @Operation(summary = "Lấy cây Menu Động phân cấp theo Portal Type, Ngôn ngữ và Phân quyền")
    public ResponseEntity<ApiResponse<List<DynamicMenuNodeDto>>> getMenuTree(
            @RequestParam(name = "portalType", defaultValue = "SUPERADMIN") String portalType,
            @RequestParam(name = "lang", defaultValue = "vi") String lang,
            @RequestParam(name = "tenantId", required = false) String paramTenantId
    ) {
        String tenantId = paramTenantId != null ? paramTenantId : TenantContext.getTenantId();
        Set<String> rolesSet = UserContext.getRoles();
        List<String> roles = rolesSet != null ? new ArrayList<>(rolesSet) : new ArrayList<>();

        List<DynamicMenuNodeDto> tree = dynamicMenuService.getMenuTreeForUser(portalType, tenantId, lang, roles);
        return ResponseEntity.ok(ApiResponse.success(tree, "Lấy cây Menu Động thành công!"));
    }
}

package com.liochio.auth.controller;

import com.liochio.auth.dto.DynamicMenuNodeDto;
import com.liochio.auth.service.DynamicMenuService;
import com.liochio.common.context.TenantContext;
import com.liochio.common.context.UserContext;
import com.liochio.common.dto.ApiResponse;
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
public class DynamicMenuController {

    private final DynamicMenuService dynamicMenuService;

    @GetMapping("/tree")
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

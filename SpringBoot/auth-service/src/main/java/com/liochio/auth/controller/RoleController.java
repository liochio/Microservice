package com.liochio.auth.controller;

import com.liochio.auth.dto.PermissionResponse;
import com.liochio.auth.dto.RoleResponse;
import com.liochio.auth.service.RoleService;
import com.liochio.common.annotation.AuditLog;
import com.liochio.common.annotation.RequirePermission;
import com.liochio.common.dto.ApiResponse;
import com.liochio.common.enums.ActionType;
import com.liochio.common.constant.MessageConstants;
import com.liochio.common.i18n.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * ==============================================================================
 * Controller Quản Trị Vai Trò & Phân Quyền Động (Role & Permission Controller)
 * ==============================================================================
 */
@RestController
@RequestMapping({"/api/v1/roles", "/api/roles"})
@RequiredArgsConstructor
@Tag(name = "Role & Permission Controller", description = "Các API quản lý vai trò và phân quyền động RBAC")
public class RoleController {

    private final RoleService roleService;
    private final MessageService messageService;

    @GetMapping
    @RequirePermission({"user:read", "auth:login"})
    @Operation(summary = "Lấy danh sách tất cả các vai trò")
    public ApiResponse<List<RoleResponse>> getAllRoles() {
        List<RoleResponse> roles = roleService.getAllRoles();
        return ApiResponse.success(roles);
    }

    @GetMapping("/permissions")
    @RequirePermission({"user:read", "auth:login"})
    @Operation(summary = "Lấy danh sách tất cả các quyền hạn trong hệ thống")
    public ApiResponse<List<PermissionResponse>> getAllPermissions() {
        List<PermissionResponse> permissions = roleService.getAllPermissions();
        return ApiResponse.success(permissions);
    }

    @PutMapping("/{roleId}/permissions")
    @RequirePermission({"user:assign_role"})
    @AuditLog(module = "USER", action = ActionType.UPDATE, description = "Gán danh sách quyền hạn cho vai trò")
    @Operation(summary = "Gán danh sách quyền hạn cho một vai trò")
    public ApiResponse<RoleResponse> assignPermissions(
            @PathVariable Long roleId,
            @RequestBody Set<String> permissionCodes
    ) {
        RoleResponse response = roleService.assignPermissionsToRole(roleId, permissionCodes);
        return ApiResponse.success(response, messageService.getMessage(MessageConstants.MSG_AUTH_ROLE_PERMISSIONS_UPDATED));
    }
}

package com.liochio.auth.service;

import com.liochio.auth.dto.PermissionResponse;
import com.liochio.auth.dto.RoleResponse;
import com.liochio.auth.entity.PermissionEntity;
import com.liochio.auth.entity.RoleEntity;
import com.liochio.auth.repository.PermissionRepository;
import com.liochio.auth.repository.RoleRepository;
import com.liochio.common.exception.AppException;
import com.liochio.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ==============================================================================
 * Dịch Vụ Quản Lý Vai Trò & Phân Quyền Động (Role & Permission Service)
 * ==============================================================================
 */
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapToRoleResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(p -> PermissionResponse.builder()
                        .id(p.getId())
                        .permissionCode(p.getPermissionCode())
                        .resourceName(p.getResourceName())
                        .actionName(p.getActionName())
                        .description(p.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public RoleResponse assignPermissionsToRole(Long roleId, Set<String> permissionCodes) {
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        Set<PermissionEntity> permissions = permissionCodes.stream()
                .map(code -> permissionRepository.findByPermissionCode(code)
                        .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, new Object[]{code})))
                .collect(Collectors.toSet());

        role.setPermissions(permissions);
        RoleEntity updated = roleRepository.save(role);
        return mapToRoleResponse(updated);
    }

    private RoleResponse mapToRoleResponse(RoleEntity role) {
        Set<String> permissions = role.getPermissions().stream()
                .map(PermissionEntity::getPermissionCode)
                .collect(Collectors.toSet());

        return RoleResponse.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .description(role.getDescription())
                .permissions(permissions)
                .build();
    }
}

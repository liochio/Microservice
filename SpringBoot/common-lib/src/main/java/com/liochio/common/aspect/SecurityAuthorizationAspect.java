package com.liochio.common.aspect;

import com.liochio.common.annotation.RequirePermission;
import com.liochio.common.annotation.RequireRole;
import com.liochio.common.context.UserContext;
import com.liochio.common.enums.RoleEnum;
import com.liochio.common.exception.AppException;
import com.liochio.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;

/**
 * ==============================================================================
 * Khía Cạnh Kiểm Tra Phân Quyền Hai Tầng RBAC / ABAC (Security Authorization Aspect)
 * ==============================================================================
 * 
 * Mục đích:
 * - Chặn trước khi Controller/Service method thực thi.
 * - Đối chiếu `@RequireRole` và `@RequirePermission` với UserContext (nạp từ JWT / Header Gateway).
 * - Ném 403 Forbidden (`ErrorCode.UNAUTHORIZED`) kèm thông điệp rõ ràng nếu thiếu quyền.
 */
@Slf4j
@Aspect
@Component
@Order(10)
public class SecurityAuthorizationAspect {

    @Before("@annotation(requireRole)")
    public void checkRole(JoinPoint joinPoint, RequireRole requireRole) {
        Set<String> userRoles = UserContext.getRoles();
        String username = UserContext.getUsername();

        // 1. Quản trị viên tối cao luôn vượt qua
        if (isSuperAdmin(userRoles)) {
            return;
        }

        String[] allowedRoles = requireRole.value();
        boolean hasAllowedRole = Arrays.stream(allowedRoles)
                .anyMatch(role -> userRoles.contains(role) 
                        || userRoles.contains("ROLE_" + role) 
                        || userRoles.contains(role.replace("ROLE_", "")));

        if (!hasAllowedRole) {
            log.warn("[SecurityAspect] User '{}' bị từ chối do thiếu vai trò yêu cầu: {}", username, Arrays.toString(allowedRoles));
            throw new AppException(ErrorCode.UNAUTHORIZED, "Bạn không có vai trò phù hợp để thực hiện thao tác này");
        }
    }

    @Before("@annotation(requirePermission)")
    public void checkPermission(JoinPoint joinPoint, RequirePermission requirePermission) {
        Set<String> userRoles = UserContext.getRoles();
        Set<String> userPermissions = UserContext.getPermissions();
        String username = UserContext.getUsername();

        // 1. Quản trị viên tối cao luôn vượt qua
        if (isSuperAdmin(userRoles)) {
            return;
        }

        if (requirePermission.superAdminOnly()) {
            log.warn("[SecurityAspect] User '{}' bị từ chối do yêu cầu quyền SUPER_ADMIN", username);
            throw new AppException(ErrorCode.UNAUTHORIZED, "Thao tác này chỉ dành riêng cho Quản trị viên tối cao");
        }

        String[] requiredPerms = requirePermission.value();
        if (requiredPerms.length == 0) {
            return;
        }

        if (requirePermission.mode() == RequirePermission.Mode.ALL) {
            // Phải thỏa mãn toàn bộ các quyền
            boolean hasAll = Arrays.stream(requiredPerms)
                    .allMatch(p -> userPermissions.contains(p) || userPermissions.contains("system:admin"));
            if (!hasAll) {
                log.warn("[SecurityAspect] User '{}' thiếu một trong các quyền yêu cầu: {}", username, Arrays.toString(requiredPerms));
                throw new AppException(ErrorCode.UNAUTHORIZED, "Bạn không đủ quyền hạn thực hiện thao tác: " + Arrays.toString(requiredPerms));
            }
        } else {
            // Chỉ cần thỏa mãn ít nhất 1 quyền
            boolean hasAny = Arrays.stream(requiredPerms)
                    .anyMatch(p -> userPermissions.contains(p) || userPermissions.contains("system:admin"));
            if (!hasAny) {
                log.warn("[SecurityAspect] User '{}' không có quyền nào trong danh sách: {}", username, Arrays.toString(requiredPerms));
                throw new AppException(ErrorCode.UNAUTHORIZED, "Bạn không có quyền thực hiện thao tác: " + Arrays.toString(requiredPerms));
            }
        }
    }

    private boolean isSuperAdmin(Set<String> roles) {
        return roles != null && (
                roles.contains(RoleEnum.ROLE_SUPER_ADMIN.getRoleName())
                || roles.contains("ROLE_SUPER_ADMIN")
                || roles.contains("SUPER_ADMIN")
        );
    }
}

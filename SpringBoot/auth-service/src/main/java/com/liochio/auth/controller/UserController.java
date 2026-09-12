package com.liochio.auth.controller;

import com.liochio.auth.dto.UserResponse;
import com.liochio.auth.service.UserService;
import com.liochio.common.annotation.AuditLog;
import com.liochio.common.annotation.RequirePermission;
import com.liochio.common.constant.MessageConstants;
import com.liochio.common.dto.ApiResponse;
import com.liochio.common.dto.PageResponse;
import com.liochio.common.enums.ActionType;
import com.liochio.common.i18n.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

/**
 * ==============================================================================
 * Controller Quản Lý Người Dùng (User Management Controller)
 * ==============================================================================
 */
@RestController
@RequestMapping({"/api/v1/users", "/api/users"})
@RequiredArgsConstructor
@Tag(name = "User Management Controller", description = "Các API quản trị danh sách người dùng")
public class UserController {

    private final UserService userService;
    private final MessageService messageService;

    @GetMapping
    @RequirePermission({"user:read"})
    @Operation(summary = "Lấy danh sách người dùng có phân trang")
    public ApiResponse<PageResponse<UserResponse>> getUsers(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<UserResponse> response = userService.getUsers(pageable);
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}")
    @RequirePermission({"user:read"})
    @Operation(summary = "Lấy chi tiết người dùng theo ID")
    public ApiResponse<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getUserById(id);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{id}")
    @RequirePermission({"user:delete"})
    @AuditLog(module = "USER", action = ActionType.DELETE, description = "Xóa mềm người dùng theo ID")
    @Operation(summary = "Xóa mềm người dùng theo ID")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.noContent(messageService.getMessage(MessageConstants.MSG_AUTH_USER_DELETED));
    }
}

package com.liochio.common.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Set;

/**
 * ==============================================================================
 * Quản Lý Ngữ Cảnh Người Dùng Đã Xác Thực (User Security Context)
 * ==============================================================================
 * 
 * Mục đích:
 * - Lưu trữ thông tin định danh của người dùng đang thực hiện request (User ID, Username, Roles, Permissions).
 * - Cung cấp thông tin cho JPA Auditing (`AuditorAware`) tự động điền `createdBy` và `lastModifiedBy`.
 * 
 * Khi nào gọi:
 * - Được JwtAuthenticationFilter nạp sau khi giải mã Token thành công và dọn sạch ở khối `finally`.
 */
public final class UserContext {

    private static final ThreadLocal<UserInfo> CURRENT_USER = new ThreadLocal<>();

    private UserContext() {
        // Chống khởi tạo instance cho Utility Class
    }

    public static UserInfo getUser() {
        return CURRENT_USER.get();
    }

    public static Long getUserId() {
        UserInfo user = CURRENT_USER.get();
        return user != null ? user.getUserId() : null;
    }

    public static String getUsername() {
        UserInfo user = CURRENT_USER.get();
        return user != null ? user.getUsername() : "ANONYMOUS";
    }

    public static Set<String> getRoles() {
        UserInfo user = CURRENT_USER.get();
        return user != null ? user.getRoles() : Collections.emptySet();
    }

    public static Set<String> getPermissions() {
        UserInfo user = CURRENT_USER.get();
        return user != null ? user.getPermissions() : Collections.emptySet();
    }

    public static void setUser(UserInfo userInfo) {
        CURRENT_USER.set(userInfo);
    }

    public static void clear() {
        CURRENT_USER.remove();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long userId;
        private String username;
        private String tenantId;
        private Set<String> roles;
        private Set<String> permissions;
    }
}

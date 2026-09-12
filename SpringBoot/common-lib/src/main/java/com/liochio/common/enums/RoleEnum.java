package com.liochio.common.enums;

import lombok.Getter;

/**
 * ==============================================================================
 * Enum Danh mục Quyền hạn Người dùng (System Roles)
 * ==============================================================================
 * 
 * Mục đích:
 * - Định nghĩa các vai trò chuẩn trong hệ thống Multi-Tenancy:
 *   + SUPER_ADMIN: Quản trị viên tối cao của nền tảng (Toàn quyền trên mọi Tenant)
 *   + TENANT_ADMIN: Quản trị viên của một Website / Khách thuê cụ thể
 *   + EDITOR: Biên tập viên nội dung (được thêm/sửa portfolio, bài viết, media)
 *   + VIEWER: Người dùng thông thường chỉ có quyền đọc dữ liệu
 * 
 * Khi nào gọi:
 * - Được dùng trong Auth Service khi phân quyền, gán Role cho User, và trong AOP kiểm tra quyền.
 */
@Getter
public enum RoleEnum {
    ROLE_SUPER_ADMIN("ROLE_SUPER_ADMIN", "Quản trị viên cấp cao toàn hệ thống"),
    ROLE_CORP_ADMIN("ROLE_CORP_ADMIN", "Quản trị viên cấp doanh nghiệp Tenant"),
    ROLE_TENANT_ADMIN("ROLE_TENANT_ADMIN", "Quản trị viên khách thuê / Website"),
    ROLE_SALE_MANAGER("ROLE_SALE_MANAGER", "Quản lý kinh doanh và điều phối dịch vụ"),
    ROLE_CUSTOMER("ROLE_CUSTOMER", "Khách hàng / Thành viên hệ thống"),
    ROLE_EDITOR("ROLE_EDITOR", "Biên tập viên nội dung"),
    ROLE_VIEWER("ROLE_VIEWER", "Người xem thông thường");

    private final String roleName;
    private final String description;

    RoleEnum(String roleName, String description) {
        this.roleName = roleName;
        this.description = description;
    }
}

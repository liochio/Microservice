package com.liochio.common.context;

import com.liochio.common.constant.AppConstants;

/**
 * ==============================================================================
 * Quản Lý Ngữ Cảnh Đa Người Thuê (Tenant Context - Multi-Tenancy Isolation)
 * ==============================================================================
 * 
 * Mục đích:
 * - Sử dụng ThreadLocal để lưu trữ mã định danh `tenantId` cho luồng xử lý HTTP hiện tại.
 * - Cho phép bất kỳ tầng nào (Service, Repository, Hibernate Filter) truy xuất `tenantId`
 *   mà không cần phải truyền tham số qua từng hàm.
 * 
 * Khi nào gọi:
 * - Được TenantFilter thiết lập ngay khi nhận HTTP Request và dọn sạch (clear) ở khối `finally`.
 */
public final class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = ThreadLocal.withInitial(() -> AppConstants.DEFAULT_TENANT_ID);

    private TenantContext() {
        // Chống khởi tạo instance cho Utility Class
    }

    /**
     * Lấy Tenant ID của luồng request hiện tại
     *
     * @return Chuỗi tenantId (mặc định "default")
     */
    public static String getTenantId() {
        String tenant = CURRENT_TENANT.get();
        return (tenant != null && !tenant.isBlank()) ? tenant : AppConstants.DEFAULT_TENANT_ID;
    }

    /**
     * Thiết lập Tenant ID cho luồng request hiện tại
     *
     * @param tenantId Mã khách thuê nhận từ header X-Tenant-ID
     */
    public static void setTenantId(String tenantId) {
        if (tenantId != null && !tenantId.isBlank()) {
            CURRENT_TENANT.set(tenantId.trim());
        } else {
            CURRENT_TENANT.set(AppConstants.DEFAULT_TENANT_ID);
        }
    }

    /**
     * Xóa sạch ThreadLocal khi kết thúc Request để chống Memory Leak
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }
}

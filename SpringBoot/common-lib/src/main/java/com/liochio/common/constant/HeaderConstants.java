package com.liochio.common.constant;

/**
 * ==============================================================================
 * Hệ thống Hằng số HTTP Headers (Header Constants)
 * ==============================================================================
 * 
 * Mục đích:
 * - Tập trung hóa các tên HTTP Header tiêu chuẩn phục vụ cho:
 *   + Định danh khách thuê (Multi-Tenancy)
 *   + Đa ngôn ngữ (i18n)
 *   + Xác thực danh tính (Authentication / JWT)
 *   + Truy vết phân tán (Distributed Tracing / Idempotency)
 * 
 * Khi nào sử dụng:
 * - Được gọi trong API Gateway filters, Auth interceptors, Logging filters
 *   và các tầng Controller tiếp nhận request từ Client/Frontend.
 */
public final class HeaderConstants {

    private HeaderConstants() {
        // Chống khởi tạo instance cho Utility Class
    }

    /**
     * Header định danh khách thuê (Multi-tenancy isolation)
     */
    public static final String X_TENANT_ID = "X-Tenant-ID";

    /**
     * Header truyền Authorization Token (Bearer JWT)
     */
    public static final String AUTHORIZATION = "Authorization";

    /**
     * Header truyền ngôn ngữ client mong muốn (vi, en, zh)
     */
    public static final String ACCEPT_LANGUAGE = "Accept-Language";

    /**
     * Header định danh mã yêu cầu duy nhất phục vụ Distributed Tracing & Audit Log
     */
    public static final String X_REQUEST_ID = "X-Request-ID";

    /**
     * Header truyền Trace ID phân tán xuyên suốt các Microservices
     */
    public static final String X_TRACE_ID = "X-Trace-ID";

    /**
     * Header chống trùng lặp request phân tán (Idempotency Key)
     */
    public static final String IDEMPOTENCY_KEY = "Idempotency-Key";

    /**
     * Header lấy địa chỉ IP thật của client qua Proxy/Load Balancer
     */
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";

    /**
     * Header chuyển tiếp User ID từ Gateway tới các Microservices nội bộ
     */
    public static final String X_USER_ID = "X-User-ID";

    /**
     * Header chuyển tiếp Roles từ Gateway tới các Microservices nội bộ
     */
    public static final String X_USER_ROLES = "X-User-Roles";

    /**
     * Header chuyển tiếp Permissions từ Gateway tới các Microservices nội bộ
     */
    public static final String X_USER_PERMISSIONS = "X-User-Permissions";
}

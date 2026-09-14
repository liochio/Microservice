package com.liochio.common.constant;

/**
 * ==============================================================================
 * Hệ thống Hằng số Cấu hình Chung (Application Constants)
 * ==============================================================================
 * 
 * Mục đích:
 * - Tập trung hóa 100% các giá trị cấu hình mặc định, tên service, format thời gian,
 *   định dạng phân trang để loại bỏ hoàn toàn hardcoded string trong source code.
 * 
 * Khi nào sử dụng:
 * - Được gọi trong toàn bộ các layer (Controller, Service, Repository, DTO) khi cần
 *   thiết lập giá trị mặc định cho phân trang, định dạng múi giờ UTC, tên tiến trình.
 */
public final class AppConstants {

    private AppConstants() {
        // Chống khởi tạo instance cho Utility Class
    }

    /**
     * Múi giờ chuẩn hệ thống: Toàn bộ Backend và Database cố định UTC
     */
    public static final String UTC_TIMEZONE = "UTC";

    /**
     * Định dạng ngày giờ chuẩn ISO-8601 UTC
     */
    public static final String DATE_TIME_FORMAT_UTC = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /**
     * Tenant mặc định khi client không truyền header X-Tenant-ID
     */
    public static final String DEFAULT_TENANT_ID = "default";

    /**
     * Tên người dùng mặc định tạo/sửa dữ liệu khi hệ thống chạy ngầm
     */
    public static final String SYSTEM_USER = "SYSTEM";

    /**
     * Phân trang mặc định
     */
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final String DEFAULT_SORT_BY = "createdAt";
    public static final String DEFAULT_SORT_DIRECTION = "DESC";
    public static final int MAX_PAGE_SIZE = 100;

    /**
     * Tên các Microservices trong cụm kiến trúc
     */
    public static final String SERVICE_AUTH = "auth-service";
    public static final String SERVICE_ENTITY = "entity-service";
    public static final String SERVICE_NOTIFICATION = "notification-service";
    public static final String SERVICE_PAYMENT = "payment-service";
    public static final String SERVICE_GATEWAY = "api-gateway";
    public static final String SERVICE_REGISTRY = "service-registry";
    public static final String SERVICE_CONFIG = "config-server";
}

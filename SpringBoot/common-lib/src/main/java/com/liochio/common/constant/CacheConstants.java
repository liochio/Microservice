package com.liochio.common.constant;

/**
 * ==============================================================================
 * Hệ thống Hằng số Bộ nhớ đệm (Cache Constants)
 * ==============================================================================
 * 
 * Mục đích:
 * - Định nghĩa tên vùng Cache (Cache Names) cho L1 (Caffeine) và L2 (Redis),
 *   cùng với các hằng số cấu hình TTL (Time-To-Live).
 * 
 * Khi nào sử dụng:
 * - Được dùng trong annotation `@Cacheable`, `@CachePut`, `@CacheEvict` và trong CacheManager configs.
 */
public final class CacheConstants {

    private CacheConstants() {
        // Chống khởi tạo instance cho Utility Class
    }

    // Tên các vùng cache (Cache Names)
    public static final String CACHE_TENANT_RULES = "tenantSanitizeRules";
    public static final String CACHE_ROLES_PERMISSIONS = "rolesAndPermissions";
    public static final String CACHE_UI_CONFIGS = "uiConfigurations";
    public static final String CACHE_NAVIGATION_MENUS = "navigationMenus";
    public static final String CACHE_PORTFOLIO_ITEMS = "portfolioItems";
    public static final String CACHE_I18N_MESSAGES = "i18nMessages";

    // Tiền tố Redis Key
    public static final String REDIS_PREFIX_TOKEN_BLACKLIST = "blacklist:token:";
    public static final String REDIS_PREFIX_SESSION_BLACKLIST = "blacklist:session:";
    public static final String REDIS_PREFIX_USER_REVOCATION = "blacklist:user:";
    public static final String REDIS_PREFIX_RATE_LIMIT = "ratelimit:";
    public static final String REDIS_PREFIX_IDEMPOTENCY = "idempotency:";
    public static final String REDIS_PREFIX_LOCK = "lock:";

    // Thời gian hết hạn mặc định (Giây)
    public static final long TTL_L1_CAFFEINE_SECONDS = 300L;    // 5 phút
    public static final long TTL_L2_REDIS_SECONDS = 3600L;       // 1 giờ
    public static final long TTL_IDEMPOTENCY_SECONDS = 120L;     // 2 phút
}

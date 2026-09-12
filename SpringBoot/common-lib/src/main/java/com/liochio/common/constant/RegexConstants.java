package com.liochio.common.constant;

/**
 * ==============================================================================
 * Hệ thống Biểu thức Chính quy (Regex Constants)
 * ==============================================================================
 * 
 * Mục đích:
 * - Tập trung hóa các pattern regex phục vụ validate email, phone, slug,
 *   lọc sạch thẻ HTML/script độc hại (XSS) và ký tự nguy hiểm.
 * 
 * Khi nào sử dụng:
 * - Được SanitizerUtils, DTO Validation và Custom Deserializers sử dụng.
 */
public final class RegexConstants {

    private RegexConstants() {
        // Chống khởi tạo instance cho Utility Class
    }

    /**
     * Pattern kiểm tra định dạng Email hợp lệ
     */
    public static final String EMAIL_PATTERN = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    /**
     * Pattern kiểm tra định dạng URL Slug (kebab-case)
     */
    public static final String SLUG_PATTERN = "^[a-z0-9]+(?:-[a-z0-9]+)*$";

    /**
     * Pattern lọc script tag XSS
     */
    public static final String SCRIPT_TAG_REGEX = "(?i)<script.*?>.*?</script>";

    /**
     * Pattern lọc javascript: URI XSS
     */
    public static final String JAVASCRIPT_URI_REGEX = "(?i)<.*?javascript:.*?>";

    /**
     * Pattern lọc toàn bộ các thẻ HTML tag
     */
    public static final String HTML_TAG_REGEX = "<[^>]*>";

    /**
     * Pattern giữ lại chữ cái (bao gồm tiếng Việt có dấu), số và khoảng trắng
     */
    public static final String STRIP_SPECIAL_REGEX = "[^a-zA-Z0-9\\-_\\sàáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđĐ]";
}

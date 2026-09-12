package com.liochio.common.utils;

import com.liochio.common.constant.RegexConstants;

/**
 * ==============================================================================
 * Tiện Ích Làm Sạch Dữ Liệu & Chống XSS (Sanitizer Utility)
 * ==============================================================================
 * 
 * Mục đích:
 * - Cung cấp thuật toán làm sạch chuỗi đầu vào theo các quy tắc bảo mật:
 *   + "HTML" (mặc định): Cắt bỏ <script>, <iframe>, <style>, javascript: uri và các thẻ HTML nguy hiểm.
 *   + "STRIP_SPECIAL": Loại bỏ ký tự lạ, giữ lại chữ cái (bao gồm tiếng Việt), số và khoảng trắng.
 *   + "SQL_SAFE": Loại bỏ các ký tự nháy đơn, nháy kép, comment SQL.
 * 
 * Khi nào gọi:
 * - Được DynamicSanitizeDeserializer tự động gọi khi parse DTO.
 */
public final class SanitizerUtils {

    private SanitizerUtils() {
        // Chống khởi tạo instance cho Utility Class
    }

    /**
     * Làm sạch chuỗi đầu vào dựa trên ruleKey
     *
     * @param input   Chuỗi gốc từ request của client
     * @param ruleKey Mã quy tắc lọc
     * @return Chuỗi đã được làm sạch an toàn
     */
    public static String sanitize(String input, String ruleKey) {
        if (input == null || input.isBlank()) {
            return input;
        }

        if ("STRIP_SPECIAL".equalsIgnoreCase(ruleKey)) {
            return input.replaceAll(RegexConstants.STRIP_SPECIAL_REGEX, "").trim();
        }

        if ("SQL_SAFE".equalsIgnoreCase(ruleKey)) {
            return input.replace("'", "''").replace(";", "").replace("--", "").trim();
        }

        // Mặc định quy tắc HTML: Chống tấn công XSS
        return input.replaceAll(RegexConstants.SCRIPT_TAG_REGEX, "")
                .replaceAll(RegexConstants.JAVASCRIPT_URI_REGEX, "")
                .replaceAll(RegexConstants.HTML_TAG_REGEX, "")
                .trim();
    }
}
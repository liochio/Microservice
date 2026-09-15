package com.liochio.common.enums;

import lombok.Getter;

import java.util.Locale;

/**
 * ==============================================================================
 * Enum Ngôn ngữ Hệ thống hỗ trợ (Supported Languages)
 * ==============================================================================
 * 
 * Mục đích:
 * - Định nghĩa danh sách các ngôn ngữ được hỗ trợ trong hệ thống đa ngôn ngữ (i18n):
 *   + Tiếng Việt (vi - VIETNAMESE)
 *   + Tiếng Anh (en - ENGLISH)
 *   + Tiếng Trung (zh - CHINESE)
 * 
 * Khi nào gọi:
 * - Được I18nConfig, MessageService, và LocaleResolver sử dụng để ánh xạ Header 'Accept-Language'.
 */
@Getter
public enum LanguageEnum {
    VI("vi", "Tiếng Việt", Locale.forLanguageTag("vi")),
    EN("en", "English", Locale.ENGLISH),
    ZH("zh", "中文 (Chinese)", Locale.CHINESE);

    private final String code;
    private final String displayName;
    private final Locale locale;

    LanguageEnum(String code, String displayName, Locale locale) {
        this.code = code;
        this.displayName = displayName;
        this.locale = locale;
    }

    /**
     * Chuyển đổi mã chuỗi (String code) sang Enum Language tương ứng (mặc định là VI nếu không khớp)
     *
     * @param code Mã ngôn ngữ nhận từ HTTP header (vd: vi, en, zh, en-US)
     * @return LanguageEnum hợp lệ
     */
    public static LanguageEnum fromCode(String code) {
        if (code == null || code.isBlank()) {
            return VI;
        }
        String normalized = code.toLowerCase().trim();
        if (normalized.startsWith("en")) {
            return EN;
        } else if (normalized.startsWith("zh")) {
            return ZH;
        }
        return VI;
    }
}

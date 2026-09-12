package com.liochio.common.i18n;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * ==============================================================================
 * Dịch Vụ Thông Báo Đa Ngôn Ngữ (Message Service)
 * ==============================================================================
 * 
 * Mục đích:
 * - Cung cấp hàm tiện ích để lấy chuỗi thông báo đã được bản địa hóa (Localized Message)
 *   dựa trên mã Key (Message Key) và Locale hiện tại của Request.
 * 
 * Khi nào gọi:
 * - Được GlobalExceptionHandler, các tầng Service hoặc Controller gọi khi cần dịch
 *   thông báo thành công, thông báo lỗi sang tiếng Việt / Anh / Trung.
 */
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageSource messageSource;

    /**
     * Lấy thông báo theo mã khóa (Key) với Locale của request hiện tại
     *
     * @param code Mã khóa trong file properties (vd: api.response.success)
     * @return Chuỗi thông báo đã dịch
     */
    public String getMessage(String code) {
        return getMessage(code, null, LocaleContextHolder.getLocale());
    }

    /**
     * Lấy thông báo theo mã khóa (Key) kèm các tham số truyền vào (args)
     *
     * @param code Mã khóa trong file properties
     * @param args Mảng các tham số thế chỗ ({0}, {1}...)
     * @return Chuỗi thông báo đã định dạng
     */
    public String getMessage(String code, Object[] args) {
        return getMessage(code, args, LocaleContextHolder.getLocale());
    }

    /**
     * Lấy thông báo theo mã khóa (Key), tham số và chỉ định cụ thể Locale
     *
     * @param code   Mã khóa trong file properties
     * @param args   Mảng tham số thế chỗ
     * @param locale Locale mong muốn
     * @return Chuỗi thông báo
     */
    public String getMessage(String code, Object[] args, Locale locale) {
        try {
            return messageSource.getMessage(code, args, locale != null ? locale : LocaleContextHolder.getLocale());
        } catch (Exception e) {
            // Fallback trả về chính code nếu không tìm thấy key trong resource bundle
            return code;
        }
    }
}

package com.liochio.common.i18n;

import com.liochio.common.constant.HeaderConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

/**
 * ==============================================================================
 * Cấu Hình Đa Ngôn Ngữ Toàn Diện (Internationalization - i18n Configuration)
 * ==============================================================================
 * 
 * Mục đích:
 * - Tự động nhận diện ngôn ngữ yêu cầu từ Client thông qua HTTP Header `Accept-Language`.
 * - Hỗ trợ 3 ngôn ngữ chính: Tiếng Việt (vi - mặc định), Tiếng Anh (en), Tiếng Trung (zh).
 * - Nạp tập tin tài nguyên messages_{lang}.properties chuẩn UTF-8.
 * 
 * Khi nào gọi:
 * - Khởi tạo một lần khi Spring Boot Application Context nạp các Bean.
 */
@Configuration
public class I18nConfig {

    private static final List<Locale> SUPPORTED_LOCALES = List.of(
            Locale.forLanguageTag("vi"),
            Locale.ENGLISH,
            Locale.CHINESE
    );

    /**
     * Bean giải quyết Locale từ HTTP Header Accept-Language
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver() {
            @Override
            public Locale resolveLocale(HttpServletRequest request) {
                String headerLang = request.getHeader(HeaderConstants.ACCEPT_LANGUAGE);
                if (headerLang == null || headerLang.isBlank()) {
                    return Locale.forLanguageTag("vi");
                }
                List<Locale.LanguageRange> list = Locale.LanguageRange.parse(headerLang);
                return Locale.lookup(list, SUPPORTED_LOCALES) != null 
                        ? Locale.lookup(list, SUPPORTED_LOCALES) 
                        : Locale.forLanguageTag("vi");
            }
        };
        resolver.setDefaultLocale(Locale.forLanguageTag("vi"));
        resolver.setSupportedLocales(SUPPORTED_LOCALES);
        return resolver;
    }

    /**
     * Bean nạp file messages resource bundles từ classpath i18n/
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:i18n/messages");
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        messageSource.setDefaultLocale(Locale.forLanguageTag("vi"));
        messageSource.setCacheSeconds(3600); // Cache 1 giờ để tối ưu RAM
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }
}

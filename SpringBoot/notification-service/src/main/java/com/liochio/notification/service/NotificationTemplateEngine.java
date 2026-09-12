package com.liochio.notification.service;

import com.liochio.notification.entity.NotificationTemplateEntity;
import com.liochio.notification.repository.NotificationTemplateRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ==============================================================================
 * Động Cơ Xử Lý Template Động & Đa Ngôn Ngữ (Dynamic Template Engine)
 * ==============================================================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationTemplateEngine {

    private final NotificationTemplateRepository templateRepository;
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{\\s*([a-zA-Z0-9_.-]+)\\s*\\}\\}");

    @Data
    @Builder
    @AllArgsConstructor
    public static class CompiledTemplate {
        private String subject;
        private String content;
    }

    public CompiledTemplate compile(String tenantId, String templateCode, String channel, String locale, Map<String, Object> params) {
        if (templateCode == null || templateCode.isBlank()) {
            return null;
        }

        String effectiveTenant = (tenantId != null && !tenantId.isBlank()) ? tenantId : "SYSTEM";
        String effectiveLocale = (locale != null && !locale.isBlank()) ? locale.toLowerCase() : "vi";
        String effectiveChannel = (channel != null) ? channel.toUpperCase() : "EMAIL";

        // 1. Tìm template theo Tenant -> Locale
        Optional<NotificationTemplateEntity> templateOpt = templateRepository
                .findByTenantIdAndTemplateCodeAndChannelAndLocale(effectiveTenant, templateCode, effectiveChannel, effectiveLocale);

        // 2. Fallback sang SYSTEM tenant cùng locale
        if (templateOpt.isEmpty() && !effectiveTenant.equals("SYSTEM")) {
            templateOpt = templateRepository
                    .findByTenantIdAndTemplateCodeAndChannelAndLocale("SYSTEM", templateCode, effectiveChannel, effectiveLocale);
        }

        // 3. Fallback sang SYSTEM tenant locale mặc định 'vi'
        if (templateOpt.isEmpty() && !effectiveLocale.equals("vi")) {
            templateOpt = templateRepository
                    .findByTenantIdAndTemplateCodeAndChannelAndLocale("SYSTEM", templateCode, effectiveChannel, "vi");
        }

        if (templateOpt.isEmpty() || !templateOpt.get().getIsActive()) {
            log.warn("[NotificationTemplateEngine] Không tìm thấy mẫu template hoạt động: Code='{}', Channel='{}', Locale='{}'",
                    templateCode, effectiveChannel, effectiveLocale);
            return null;
        }

        NotificationTemplateEntity entity = templateOpt.get();
        String subject = interpolate(entity.getSubject(), params);
        String content = interpolate(entity.getBodyTemplate(), params);

        return CompiledTemplate.builder()
                .subject(subject)
                .content(content)
                .build();
    }

    public String interpolate(String template, Map<String, Object> params) {
        if (template == null || params == null || params.isEmpty()) {
            return template;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = params.get(key);
            String replacement = value != null ? Matcher.quoteReplacement(String.valueOf(value)) : "";
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}

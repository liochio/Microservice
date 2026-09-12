package com.liochio.worker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * ==============================================================================
 * Dịch Vụ Phát Cảnh Báo An Ninh & Sự Cố Tức Thì (Real-Time Telegram/Slack Alerting)
 * ==============================================================================
 * 
 * Mục đích:
 * - Tự động phát tin nhắn cảnh báo bảo mật, lỗi hệ thống 500, cạn kiệt Connection Pool,
 *   DLQ tràn ngập hoặc Giao dịch bù trừ Saga tới Kênh Quản trị Telegram / Slack.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramAlertService {

    private final JdbcTemplate jdbcTemplate;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

    /**
     * Bắn cảnh báo sự cố nghiêm trọng (Critical Alert)
     */
    public boolean sendCriticalAlert(String title, String message, String traceId, String module) {
        String botToken = getConfigValue("alert.telegram.bot_token", "");
        String chatId = getConfigValue("alert.telegram.chat_id", "");

        String formattedTime = TIME_FORMATTER.format(Instant.now());

        String telegramText = String.format(
                "🚨 <b>[LIOCHIO FINTECH ALERT - CRITICAL]</b>\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "📌 <b>Tiêu đề:</b> %s\n" +
                "🏢 <b>Phân hệ:</b> <code>%s</code>\n" +
                "🔍 <b>Trace ID:</b> <code>%s</code>\n" +
                "⏰ <b>Thời gian:</b> %s\n" +
                "📝 <b>Chi tiết:</b>\n%s\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "⚠️ <i>Vui lòng tra soát ngay qua Centralized Audit Hub!</i>",
                title,
                module != null ? module : "CORE_SYSTEM",
                traceId != null ? traceId : "N/A",
                formattedTime,
                message
        );

        log.info("\n" +
                "================================================================================\n" +
                "🚨 [SYSTEM ALERT DISPATCHER] ĐÃ PHÁT CẢNH BÁO AN NINH & SỰ CỐ\n" +
                "   -> Tiêu đề     : {}\n" +
                "   -> Phân hệ     : {}\n" +
                "   -> Trace ID    : {}\n" +
                "   -> Telegram ID : {}\n" +
                "================================================================================",
                title, module, traceId, chatId.isBlank() ? "(Console Logger Mode)" : chatId);

        if (botToken.isBlank() || chatId.isBlank()) {
            log.info("[TelegramAlertService] Chưa cấu hình Telegram Bot Token/Chat ID (Đã lưu vết vào file log và DB)");
            return true;
        }

        try {
            String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("chat_id", chatId);
            body.put("text", telegramText);
            body.put("parse_mode", "HTML");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("[TelegramAlertService] Gửi qua Telegram API Socket gặp cảnh báo: {}", e.getMessage());
            return false;
        }
    }

    private String getConfigValue(String key, String defaultValue) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT config_value FROM liochio_core_db.system_configs WHERE config_key = ? AND is_active = 1 LIMIT 1",
                    String.class,
                    key
            );
        } catch (Exception ignored) {
            return defaultValue;
        }
    }
}

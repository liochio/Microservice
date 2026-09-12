package com.liochio.notification.adapter;

import com.liochio.common.enums.NotificationChannel;
import com.liochio.common.pattern.strategy.NotificationStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * ==============================================================================
 * Bộ Chuyển Đổi Gửi Thông Báo Telegram Bot (Telegram Webhook Adapter)
 * ==============================================================================
 */
@Slf4j
@Component
public class TelegramNotificationAdapter implements NotificationStrategy {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${telegram.bot-token:123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11}")
    private String botToken;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.TELEGRAM;
    }

    @Override
    public boolean send(String recipient, String subject, String content, Map<String, Object> metadata) {
        try {
            String chatId = recipient; // Chat ID của Telegram Group/User
            String text = (subject != null ? "*" + subject + "*\n\n" : "") + content;
            String url = "https://api.telegram.org/bot" + botToken + "/sendMessage?chat_id=" + chatId + "&text=" + text + "&parse_mode=Markdown";

            // Nếu bot token là mock dummy thì log mô phỏng thành công
            if (botToken.contains("123456")) {
                log.info("[TelegramAdapter] [MOCK] Đã gửi tin nhắn tới Telegram Chat ID: {} - Nội dung: {}", chatId, content);
                return true;
            }

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("[TelegramAdapter] Gửi Telegram thất bại tới {}: {}", recipient, e.getMessage());
            return false;
        }
    }
}

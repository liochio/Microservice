package com.liochio.notification.adapter;

import com.liochio.common.enums.NotificationChannel;
import com.liochio.common.pattern.strategy.NotificationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * ==============================================================================
 * Bộ Chuyển Đổi Thông Báo Realtime WebSocket (WebSocket STOMP Adapter)
 * ==============================================================================
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketNotificationAdapter implements NotificationStrategy {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.WEBSOCKET;
    }

    @Override
    public boolean send(String recipient, String subject, String content, Map<String, Object> metadata) {
        try {
            String destination = recipient.startsWith("/") ? recipient : "/topic/" + recipient;
            messagingTemplate.convertAndSend(destination, Map.of(
                    "subject", subject != null ? subject : "",
                    "content", content,
                    "metadata", metadata != null ? metadata : Map.of()
            ));
            log.info("[WebSocketAdapter] Đã đẩy tin nhắn STOMP tới destination: {}", destination);
            return true;
        } catch (Exception e) {
            log.error("[WebSocketAdapter] Lỗi đẩy tin nhắn STOMP: ", e);
            return false;
        }
    }
}

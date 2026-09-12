package com.liochio.realtime.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ==============================================================================
 * Trình Lắng Nghe Sự Kiện Kết Nối WebSocket (Live Presence Tracker)
 * ==============================================================================
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final StringRedisTemplate redisTemplate;
    private final AtomicInteger activeConnections = new AtomicInteger(0);

    public static final String REDIS_KEY_ONLINE_USERS = "realtime:online_users";

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = accessor.getUser();
        int count = activeConnections.incrementAndGet();

        if (user != null) {
            String username = user.getName();
            try {
                redisTemplate.opsForSet().add(REDIS_KEY_ONLINE_USERS, username);
            } catch (Exception ignored) {}
            log.info("[WebSocket Presence] Người dùng '{}' đã kết nối. Tổng phiên trực tuyến: {}", username, count);
        } else {
            log.info("[WebSocket Presence] Khách vãng lai (Anonymous) đã kết nối. Tổng phiên: {}", count);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = accessor.getUser();
        int count = activeConnections.decrementAndGet();
        if (count < 0) activeConnections.set(0);

        if (user != null) {
            String username = user.getName();
            try {
                redisTemplate.opsForSet().remove(REDIS_KEY_ONLINE_USERS, username);
            } catch (Exception ignored) {}
            log.info("[WebSocket Presence] Người dùng '{}' đã ngắt kết nối. Còn lại: {}", username, activeConnections.get());
        } else {
            log.info("[WebSocket Presence] Khách vãng lai đã ngắt kết nối. Còn lại: {}", activeConnections.get());
        }
    }

    public int getActiveConnectionsCount() {
        return activeConnections.get();
    }
}
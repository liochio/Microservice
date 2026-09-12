package com.liochio.realtime.config;

import com.liochio.common.security.JwtUtils;
import com.liochio.common.security.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ==============================================================================
 * Bộ Lọc Xác Thực Bắt Tay WebSocket STOMP (Zero-Trust RS256 Interceptor)
 * ==============================================================================
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> authHeaders = accessor.getNativeHeader("Authorization");
            if (authHeaders == null || authHeaders.isEmpty()) {
                authHeaders = accessor.getNativeHeader("token");
            }

            if (authHeaders != null && !authHeaders.isEmpty()) {
                String rawToken = authHeaders.get(0);
                String token = rawToken.startsWith("Bearer ") ? rawToken.substring(7).trim() : rawToken.trim();

                try {
                    if (jwtUtils.validateToken(token)) {
                        Long userId = jwtUtils.extractUserId(token);
                        String username = jwtUtils.extractUsername(token);
                        String tenantId = jwtUtils.extractTenantId(token);
                        List<String> roles = jwtUtils.extractRoles(token);

                        // Kiểm tra blacklist Token / Session / User trên Redis
                        if (tokenBlacklistService.isBlacklisted(token)) {
                            log.warn("[WebSocketAuth] Kết nối bị từ chối do Token/User bị thu hồi: User='{}'", username);
                            throw new SecurityException("Token đã bị vô hiệu hóa hoặc thu hồi");
                        }

                        StompPrincipal principal = StompPrincipal.builder()
                                .name(userId != null ? String.valueOf(userId) : username)
                                .userId(userId)
                                .username(username)
                                .tenantId(tenantId)
                                .roles(roles)
                                .build();

                        accessor.setUser(principal);
                        log.info("[WebSocketAuth] Xác thực STOMP CONNECT thành công: User ID='{}', Username='{}', Tenant='{}'",
                                userId, username, tenantId);
                    } else {
                        log.warn("[WebSocketAuth] STOMP CONNECT mang Token không hợp lệ hoặc đã hết hạn");
                    }
                } catch (Exception e) {
                    log.warn("[WebSocketAuth] Lỗi giải mã JWT khi kết nối WebSocket: {}", e.getMessage());
                }
            } else {
                log.info("[WebSocketAuth] Kết nối STOMP CONNECT dưới danh nghĩa Anonymous (Public Channels only)");
            }
        }

        return message;
    }
}
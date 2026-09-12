package com.liochio.realtime.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * ==============================================================================
 * Cấu Hình Đồng Bộ Đa Cụm Realtime (Resilient Redis Pub/Sub Backplane)
 * ==============================================================================
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisRealtimeBrokerConfig {

    public static final String REDIS_TOPIC_BROADCAST = "realtime:broadcast";
    public static final String REDIS_TOPIC_USER_PUSH = "realtime:user_push";

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListener broadcastListener,
            MessageListener userPushListener) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer() {
            @Override
            public boolean isAutoStartup() {
                return false; // Không chặn context startup nếu Redis tạm thời offline
            }
        };
        container.setConnectionFactory(connectionFactory);
        container.setRecoveryInterval(5000);
        container.setErrorHandler(t -> log.warn("[RedisPubSub Backplane] Redis warning (auto-recovering): {}", t.getMessage()));
        container.addMessageListener(broadcastListener, new ChannelTopic(REDIS_TOPIC_BROADCAST));
        container.addMessageListener(userPushListener, new ChannelTopic(REDIS_TOPIC_USER_PUSH));
        return container;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startRedisListenerAsync(ApplicationReadyEvent event) {
        RedisMessageListenerContainer container = event.getApplicationContext().getBean(RedisMessageListenerContainer.class);
        CompletableFuture.runAsync(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    log.info("[RedisPubSub Backplane] Đang kết nối tới Redis Pub/Sub...");
                    container.start();
                    log.info("[RedisPubSub Backplane] Kết nối Redis Pub/Sub thành công!");
                    break;
                } catch (Exception e) {
                    log.warn("[RedisPubSub Backplane] Chưa kết nối được Redis ({}). Dịch vụ vẫn hoạt động và sẽ thử lại sau 5s...", e.getMessage());
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });
    }

    @Bean
    public MessageListener broadcastListener() {
        return (message, pattern) -> {
            try {
                String payloadStr = new String(message.getBody(), StandardCharsets.UTF_8);
                JsonNode root = objectMapper.readTree(payloadStr);
                String destination = root.has("destination") ? root.get("destination").asText() : "/topic/public";
                JsonNode data = root.has("data") ? root.get("data") : root;

                messagingTemplate.convertAndSend(destination, data);
                log.debug("[RedisPubSub Backplane] Broadcast -> {}", destination);
            } catch (Exception e) {
                log.warn("[RedisPubSub Backplane] Lỗi parse broadcast message: {}", e.getMessage());
            }
        };
    }

    @Bean
    public MessageListener userPushListener() {
        return (message, pattern) -> {
            try {
                String payloadStr = new String(message.getBody(), StandardCharsets.UTF_8);
                JsonNode root = objectMapper.readTree(payloadStr);
                String userId = root.has("userId") ? root.get("userId").asText() : null;
                String destination = root.has("destination") ? root.get("destination").asText() : "/queue/private-alerts";
                JsonNode data = root.has("data") ? root.get("data") : root;

                if (userId != null) {
                    messagingTemplate.convertAndSendToUser(userId, destination, data);
                    log.debug("[RedisPubSub Backplane] P2P User Push -> User: {}, Dest: {}", userId, destination);
                }
            } catch (Exception e) {
                log.warn("[RedisPubSub Backplane] Lỗi parse user push message: {}", e.getMessage());
            }
        };
    }
}
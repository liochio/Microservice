package com.liochio.realtime.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liochio.common.constant.MessageConstants;
import com.liochio.common.dto.ApiResponse;
import com.liochio.common.i18n.MessageService;
import com.liochio.realtime.config.RedisRealtimeBrokerConfig;
import com.liochio.realtime.config.WebSocketEventListener;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ==============================================================================
 * Controller Phát Tán Thông Điệp Thời Gian Thực (Realtime Push Controller)
 * ==============================================================================
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/realtime")
@RequiredArgsConstructor
@Tag(name = "Realtime Controller", description = "Các API đẩy thông báo thời gian thực qua WebSocket & Redis PubSub")
public class RealtimeMessageController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;
    private final WebSocketEventListener eventListener;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MessageMapping("/broadcast")
    @SendTo("/topic/public")
    public Map<String, Object> broadcast(Map<String, Object> message) {
        return message;
    }

    @PostMapping("/push")
    @Operation(summary = "Phát tán thông điệp quảng bá (Broadcast) qua kênh Topic")
    public ApiResponse<String> pushNotification(
            @RequestParam String channel,
            @RequestBody Map<String, Object> payload
    ) {
        String destination = "/topic/" + channel;
        messagingTemplate.convertAndSend(destination, payload);

        // Đồng bộ qua Redis Pub/Sub cho các node khác trong Cluster
        try {
            Map<String, Object> redisMsg = Map.of("destination", destination, "data", payload);
            redisTemplate.convertAndSend(RedisRealtimeBrokerConfig.REDIS_TOPIC_BROADCAST, objectMapper.writeValueAsString(redisMsg));
        } catch (Exception e) {
            log.debug("[RealtimeController] Bắn Redis PubSub: {}", e.getMessage());
        }

        return ApiResponse.success(channel, messageService.getMessage(MessageConstants.MSG_REALTIME_PUSH_SUCCESS));
    }

    @PostMapping("/push-user")
    @Operation(summary = "Đẩy thông điệp riêng tư (P2P Private Push) tới người dùng đích danh")
    public ApiResponse<String> pushToUser(
            @RequestParam String userId,
            @RequestParam(defaultValue = "/queue/private-alerts") String destination,
            @RequestBody Map<String, Object> payload
    ) {
        messagingTemplate.convertAndSendToUser(userId, destination, payload);

        // Đồng bộ qua Redis Pub/Sub cho các node khác
        try {
            Map<String, Object> redisMsg = Map.of("userId", userId, "destination", destination, "data", payload);
            redisTemplate.convertAndSend(RedisRealtimeBrokerConfig.REDIS_TOPIC_USER_PUSH, objectMapper.writeValueAsString(redisMsg));
        } catch (Exception e) {
            log.debug("[RealtimeController] Bắn Redis PubSub P2P: {}", e.getMessage());
        }

        return ApiResponse.success("Sent to user: " + userId, "Đã gửi thông điệp thời gian thực tới người dùng");
    }

    @GetMapping("/presence/online-count")
    @Operation(summary = "Xem tổng số lượng kết nối WebSocket đang hoạt động")
    public ApiResponse<Integer> getOnlineCount() {
        return ApiResponse.success(eventListener.getActiveConnectionsCount(), "Số lượng kết nối trực tuyến");
    }
}
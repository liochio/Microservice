package com.liochio.common.pattern.factory;

import com.liochio.common.enums.NotificationChannel;
import com.liochio.common.pattern.strategy.NotificationStrategy;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * ==============================================================================
 * Nhà Máy Khởi Tạo Chiến Lược Thông Báo (Notification Factory - GoF Factory)
 * ==============================================================================
 * 
 * Mục đích:
 * - Thu thập và điều phối các Strategy gửi thông báo dựa trên Enum 'NotificationChannel'.
 */
@Component
public class NotificationFactory {

    private final Map<NotificationChannel, NotificationStrategy> strategyMap = new EnumMap<>(NotificationChannel.class);

    public NotificationFactory(List<NotificationStrategy> strategies) {
        for (NotificationStrategy strategy : strategies) {
            strategyMap.put(strategy.getChannel(), strategy);
        }
    }

    public NotificationStrategy getStrategy(NotificationChannel channel) {
        NotificationStrategy strategy = strategyMap.get(channel);
        if (strategy == null) {
            throw new IllegalArgumentException("Không tìm thấy Notification Strategy cho channel: " + channel);
        }
        return strategy;
    }
}

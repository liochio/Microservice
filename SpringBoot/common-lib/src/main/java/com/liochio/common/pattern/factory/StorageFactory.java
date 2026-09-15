package com.liochio.common.pattern.factory;

import com.liochio.common.enums.StorageProvider;
import com.liochio.common.pattern.strategy.StorageStrategy;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * ==============================================================================
 * Nhà Máy Khởi Tạo Chiến Lược Lưu Trữ (Storage Strategy Factory - GoF Factory)
 * ==============================================================================
 * 
 * Mục đích:
 * - Tự động thu thập tất cả các Spring Beans thực thi interface 'StorageStrategy'
 *   và nạp vào bảng tra cứu EnumMap để truy xuất nhanh theo 'StorageProvider'.
 */
@Component
public class StorageFactory {

    private final Map<StorageProvider, StorageStrategy> strategyMap = new EnumMap<>(StorageProvider.class);

    public StorageFactory(List<StorageStrategy> strategies) {
        for (StorageStrategy strategy : strategies) {
            strategyMap.put(strategy.getProvider(), strategy);
        }
    }

    public StorageStrategy getStrategy(StorageProvider provider) {
        StorageStrategy strategy = strategyMap.get(provider);
        if (strategy == null) {
            throw new IllegalArgumentException("Không tìm thấy Storage Strategy cho provider: " + provider);
        }
        return strategy;
    }
}

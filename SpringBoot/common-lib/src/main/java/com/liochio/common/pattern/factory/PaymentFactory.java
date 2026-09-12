package com.liochio.common.pattern.factory;

import com.liochio.common.pattern.strategy.PaymentStrategy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ==============================================================================
 * Nhà Máy Khởi Tạo Chiến Lược Thanh Toán (Payment Factory - GoF Factory)
 * ==============================================================================
 * 
 * Mục đích:
 * - Tra cứu Strategy cổng thanh toán theo tên (vd: "VNPAY", "MOMO", "STRIPE").
 */
@Component
public class PaymentFactory {

    private final Map<String, PaymentStrategy> strategyMap = new HashMap<>();

    public PaymentFactory(List<PaymentStrategy> strategies) {
        for (PaymentStrategy strategy : strategies) {
            strategyMap.put(strategy.getGatewayName().toUpperCase(), strategy);
        }
    }

    public PaymentStrategy getStrategy(String gatewayName) {
        if (gatewayName == null) {
            throw new IllegalArgumentException("Gateway name không được để trống");
        }
        PaymentStrategy strategy = strategyMap.get(gatewayName.toUpperCase());
        if (strategy == null) {
            throw new IllegalArgumentException("Không hỗ trợ cổng thanh toán: " + gatewayName);
        }
        return strategy;
    }
}

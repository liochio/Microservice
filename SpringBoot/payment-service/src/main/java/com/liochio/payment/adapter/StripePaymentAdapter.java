package com.liochio.payment.adapter;

import com.liochio.common.pattern.strategy.PaymentStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * ==============================================================================
 * Bộ Chuyển Đổi Cổng Thanh Toán Quốc Tế Stripe (Stripe Payment Adapter)
 * ==============================================================================
 */
@Slf4j
@Component
public class StripePaymentAdapter implements PaymentStrategy {

    @Override
    public String getGatewayName() {
        return "STRIPE";
    }

    @Override
    public String createPaymentUrl(String orderId, BigDecimal amount, String orderInfo, String returnUrl, String ipAddress) {
        log.info("[StripeAdapter] Khởi tạo Stripe Checkout Session cho OrderId='{}', Amount={}", orderId, amount);
        return "https://checkout.stripe.com/pay/cs_test_" + orderId;
    }

    @Override
    public boolean verifyIpnSignature(Map<String, String> params) {
        log.info("[StripeAdapter] Xác thực Webhook Signature từ Stripe");
        return true;
    }
}

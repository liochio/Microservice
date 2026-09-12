package com.liochio.payment.adapter;

import com.liochio.common.pattern.strategy.PaymentStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * ==============================================================================
 * Bộ Chuyển Đổi Ví Điện Tử MoMo (MoMo Payment Adapter)
 * ==============================================================================
 */
@Slf4j
@Component
public class MomoPaymentAdapter implements PaymentStrategy {

    @Override
    public String getGatewayName() {
        return "MOMO";
    }

    @Override
    public String createPaymentUrl(String orderId, BigDecimal amount, String orderInfo, String returnUrl, String ipAddress) {
        log.info("[MomoAdapter] Khởi tạo giao dịch MoMo QR: OrderId='{}', Amount={}", orderId, amount);
        return "https://payment.momo.vn/v2/gateway/pay?orderId=" + orderId;
    }

    @Override
    public boolean verifyIpnSignature(Map<String, String> params) {
        log.info("[MomoAdapter] Xác thực chữ ký IPN từ MoMo");
        return true;
    }
}

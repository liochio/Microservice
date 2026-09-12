package com.liochio.common.pattern.template;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Map;

/**
 * ==============================================================================
 * Khung Xử Lý Thanh Toán Chuẩn (Abstract Payment Processor - Template Method)
 * ==============================================================================
 * 
 * Mục đích:
 * - Định nghĩa bộ khung xử lý (Skeleton) cho giao dịch thanh toán:
 *   1. Validate thông tin đơn hàng và số tiền (validatePaymentRequest)
 *   2. Tạo chữ ký bảo mật số (buildSecureSignature)
 *   3. Khởi tạo URL redirect sang cổng thanh toán (generateGatewayUrl)
 *   4. Ghi vết Audit Log bắt đầu giao dịch (auditLogPaymentInit)
 */
@Slf4j
public abstract class AbstractPaymentProcessor {

    public final String processPayment(String orderId, BigDecimal amount, String orderInfo, String returnUrl, String ipAddress) {
        log.info("[PaymentProcessor] Bắt đầu xử lý thanh toán: OrderId='{}', Amount={}", orderId, amount);

        validatePaymentRequest(orderId, amount);
        String paymentUrl = generateGatewayUrl(orderId, amount, orderInfo, returnUrl, ipAddress);
        auditLogPaymentInit(orderId, amount, paymentUrl);

        return paymentUrl;
    }

    protected void validatePaymentRequest(String orderId, BigDecimal amount) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Mã đơn hàng không được để trống");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0");
        }
    }

    protected abstract String generateGatewayUrl(String orderId, BigDecimal amount, String orderInfo, String returnUrl, String ipAddress);

    protected void auditLogPaymentInit(String orderId, BigDecimal amount, String paymentUrl) {
        log.debug("[PaymentProcessor] Hoàn tất sinh URL: OrderId='{}', URL='{}'", orderId, paymentUrl);
    }

    public final boolean processIpnCallback(Map<String, String> params) {
        log.info("[PaymentProcessor] Bắt đầu xử lý IPN Callback: params count={}", params.size());
        boolean isValidSignature = verifySignature(params);
        if (!isValidSignature) {
            log.warn("[PaymentProcessor] Chữ ký IPN không hợp lệ");
            return false;
        }
        return updateOrderStatus(params);
    }

    protected abstract boolean verifySignature(Map<String, String> params);

    protected abstract boolean updateOrderStatus(Map<String, String> params);
}

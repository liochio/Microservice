package com.liochio.common.pattern.strategy;

import java.math.BigDecimal;
import java.util.Map;

/**
 * ==============================================================================
 * Chiến Lược Cổng Thanh Toán (Payment Strategy Interface - GoF Strategy)
 * ==============================================================================
 * 
 * Mục đích:
 * - Định nghĩa giao diện chuẩn cho các cổng thanh toán (VNPay, MoMo, ZaloPay, Stripe, PayPal).
 * - Chuẩn hóa quy trình tạo URL thanh toán và xác minh phản hồi IPN Webhook.
 */
public interface PaymentStrategy {

    /**
     * Tên mã cổng thanh toán (vd: "VNPAY", "MOMO", "STRIPE", "PAYPAL")
     */
    String getGatewayName();

    /**
     * Khởi tạo liên kết thanh toán (Payment URL)
     *
     * @param orderId     Mã đơn hàng
     * @param amount      Số tiền thanh toán
     * @param orderInfo   Nội dung giao dịch
     * @param returnUrl   URL trả về cho Frontend sau khi thanh toán
     * @param ipAddress   IP của client thực hiện giao dịch
     * @return URL cổng thanh toán để điều hướng người dùng
     */
    String createPaymentUrl(String orderId, BigDecimal amount, String orderInfo, String returnUrl, String ipAddress);

    /**
     * Xác minh tính toàn vẹn của chữ ký số (HMAC SHA512) từ phản hồi IPN Webhook
     *
     * @param params Toàn bộ query params hoặc callback payload từ cổng thanh toán
     * @return true nếu chữ ký hợp lệ và giao dịch thành công
     */
    boolean verifyIpnSignature(Map<String, String> params);
}

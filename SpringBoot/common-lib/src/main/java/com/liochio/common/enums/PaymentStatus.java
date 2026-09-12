package com.liochio.common.enums;

import lombok.Getter;

/**
 * ==============================================================================
 * Enum Trạng thái Giao dịch Thanh toán (Payment Transaction Status)
 * ==============================================================================
 * 
 * Mục đích:
 * - Đồng bộ trạng thái đơn hàng/giao dịch giữa hệ thống Portfolio Engine và
 *   các cổng thanh toán (VNPay, MoMo, ZaloPay, Stripe, PayPal).
 * 
 * Khi nào gọi:
 * - Được PaymentService và IPN Webhook callback cập nhật sau khi xử lý chữ ký bảo mật.
 */
@Getter
public enum PaymentStatus {
    PENDING("Giao dịch mới khởi tạo, chờ thanh toán"),
    SUCCESS("Thanh toán thành công đã xác thực IPN"),
    FAILED("Thanh toán thất bại từ cổng thanh toán"),
    CANCELLED("Giao dịch đã bị hủy bởi người dùng"),
    REFUNDED("Giao dịch đã được hoàn tiền");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }
}

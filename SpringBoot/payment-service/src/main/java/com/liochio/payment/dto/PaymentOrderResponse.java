package com.liochio.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * ==============================================================================
 * DTO Trả Về Chi Tiết Đơn Hàng Thanh Toán (Payment Order Response DTO)
 * ==============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrderResponse {

    private Long id;
    private String tenantId;
    private String orderId;
    private Long userId;
    private String gateway;
    private BigDecimal amount;
    private String currency;
    private String orderInfo;
    private String status;
    private String transactionNo;
    private Instant createdAt;
    private Instant updatedAt;
}

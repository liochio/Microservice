package com.liochio.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * ==============================================================================
 * DTO Trả Về Đường Dẫn Thanh Toán (Payment URL Response DTO)
 * ==============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentUrlResponse {

    private String orderId;
    private String gateway;
    private BigDecimal amount;
    private String paymentUrl;
    private Instant createdAt;
}

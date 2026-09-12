package com.liochio.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * ==============================================================================
 * DTO Khởi Tạo Yêu Cầu Thanh Toán (Create Payment Request DTO)
 * ==============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {

    @NotBlank(message = "Mã đơn hàng không được để trống")
    private String orderId;

    @NotNull(message = "Số tiền thanh toán không được null")
    @DecimalMin(value = "1000.0", message = "Số tiền thanh toán tối thiểu là 1,000 VND")
    private BigDecimal amount;

    @NotBlank(message = "Cổng thanh toán không được để trống (VNPAY, MOMO, STRIPE)")
    private String gateway;

    private String orderInfo;

    @NotBlank(message = "Return URL không được để trống")
    private String returnUrl;
}

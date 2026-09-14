package com.liochio.ledger.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class M2MTransactionRequest {

    private String tenantId;

    @NotNull(message = "User ID không được để trống")
    private Long userId;

    @NotBlank(message = "Loại giao dịch không được để trống")
    private String transactionType; // TOPUP, WITHDRAW, TRANSFER, PIGGY_LOCK, PIGGY_UNLOCK, PARENT_BONUS

    @NotNull(message = "Số tiền không được để trống")
    @DecimalMin(value = "1000.00", message = "Số tiền giao dịch tối thiểu là 1,000 VNĐ")
    private BigDecimal amount;

    private String referenceId;

    @NotBlank(message = "Idempotency-Key không được để trống")
    private String idempotencyKey;

    @NotBlank(message = "Mô tả giao dịch không được để trống")
    private String description;

    private String sourceAccountType;
    private String targetAccountType;

    private Long targetUserId;

    private String actionToken;
}

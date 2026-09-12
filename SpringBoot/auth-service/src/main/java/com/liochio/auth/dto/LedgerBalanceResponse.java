package com.liochio.auth.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerBalanceResponse {

    private Long userId;
    private String tenantId;
    private BigDecimal availableBalance;
    private BigDecimal holdingBalance;
    private BigDecimal escrowBalance;
    private BigDecimal totalBalance;
    private String currency;
    private String ekycLevel;
    private BigDecimal dailyTransferLimit;
    private String status;
}

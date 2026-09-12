package com.liochio.worker.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationReportResponse {
    private Instant auditTimestamp;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;
    private BigDecimal discrepancy;
    private boolean isBalanced;
    private long totalLedgerAccounts;
    private long totalWalletsAudited;
    private String status;
    private String message;
}

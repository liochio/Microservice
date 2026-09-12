package com.liochio.common.dto.worker;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationReportResponse {

    private String auditBatchId;
    private Instant auditTimestamp;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;
    private boolean isBalanced;
    private long mismatchedRecordsCount;
    private String summary;
}

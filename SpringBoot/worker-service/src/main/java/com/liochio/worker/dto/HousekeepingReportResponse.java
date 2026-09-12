package com.liochio.worker.dto;

import lombok.*;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HousekeepingReportResponse {
    private Instant executionTimestamp;
    private int expiredOtpsDeleted;
    private int staleSessionsRevoked;
    private int expiredIdempotencyKeysDeleted;
    private int oldDlqCleaned;
    private String status;
}

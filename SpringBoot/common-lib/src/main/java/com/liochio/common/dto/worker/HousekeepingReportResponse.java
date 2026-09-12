package com.liochio.common.dto.worker;

import lombok.*;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HousekeepingReportResponse {

    private Instant executedAt;
    private long executionDurationMs;
    private Map<String, Integer> cleanedCounts;
    private String status;
}

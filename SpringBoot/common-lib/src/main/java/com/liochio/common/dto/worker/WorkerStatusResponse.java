package com.liochio.common.dto.worker;

import lombok.*;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerStatusResponse {

    private String status;
    private String workerInstanceId;
    private Instant serverTime;
    private Map<String, Long> queueMetrics;
    private Map<String, Object> schedulerStatus;
}

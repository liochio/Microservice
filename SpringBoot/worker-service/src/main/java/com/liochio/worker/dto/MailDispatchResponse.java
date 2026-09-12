package com.liochio.worker.dto;

import lombok.*;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailDispatchResponse {
    private Long id;
    private String traceId;
    private String recipient;
    private String channel;
    private String templateCode;
    private String subject;
    private String status;
    private Long executionTimeMs;
    private Instant createdAt;
}

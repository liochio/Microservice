package com.liochio.common.dto.worker;

import lombok.*;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailDispatchResponse {

    private Long logId;
    private String traceId;
    private String recipient;
    private String channel;
    private String status;
    private String message;
    private Instant dispatchedAt;
}

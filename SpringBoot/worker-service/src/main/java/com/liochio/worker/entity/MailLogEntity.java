package com.liochio.worker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "mail_logs", indexes = {
        @Index(name = "idx_mail_logs_trace_id", columnList = "trace_id"),
        @Index(name = "idx_mail_logs_recipient", columnList = "recipient"),
        @Index(name = "idx_mail_logs_status", columnList = "status")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trace_id", length = 64, nullable = false)
    private String traceId;

    @Column(name = "recipient", length = 150, nullable = false)
    private String recipient;

    @Column(name = "channel", length = 30, nullable = false)
    @Builder.Default
    private String channel = "EMAIL";

    @Column(name = "template_code", length = 100, nullable = false)
    private String templateCode;

    @Column(name = "language_code", length = 10, nullable = false)
    @Builder.Default
    private String languageCode = "vi";

    @Column(name = "subject", length = 255, nullable = false)
    private String subject;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private String status = "PENDING"; // PENDING, PROCESSING, SENT, FAILED

    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "execution_time_ms", nullable = false)
    @Builder.Default
    private Long executionTimeMs = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}

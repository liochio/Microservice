package com.liochio.worker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "dlq_messages", indexes = {
        @Index(name = "idx_dlq_event_id", columnList = "event_id"),
        @Index(name = "idx_dlq_status", columnList = "status")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DlqMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", length = 64, nullable = false)
    private String eventId;

    @Column(name = "topic", length = 100, nullable = false)
    private String topic;

    @Column(name = "trace_id", length = 64)
    private String traceId;

    @Column(name = "payload", columnDefinition = "JSON", nullable = false)
    private String payload;

    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "error_reason", columnDefinition = "TEXT")
    private String errorReason;

    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private String status = "PENDING_RETRY";

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();
}

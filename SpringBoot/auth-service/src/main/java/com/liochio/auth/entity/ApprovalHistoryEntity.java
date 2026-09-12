package com.liochio.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "approval_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalHistoryEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "approval_request_id", nullable = false)
    private Long approvalRequestId;

    @Column(name = "actor_user_id", nullable = false)
    private Long actorUserId;

    @Column(name = "action", length = 30, nullable = false)
    private String action; // SUBMITTED, APPROVED, REJECTED, CANCELLED, RESUBMITTED

    @Column(name = "action_note", columnDefinition = "TEXT")
    private String actionNote;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}

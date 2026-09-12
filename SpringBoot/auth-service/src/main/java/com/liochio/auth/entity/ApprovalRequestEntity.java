package com.liochio.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "approval_requests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequestEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", length = 50, nullable = false)
    private String tenantId;

    @Column(name = "request_code", length = 64, nullable = false, unique = true)
    private String requestCode;

    @Column(name = "request_type", length = 50, nullable = false)
    private String requestType; // CONFIG_CHANGE, FEE_CHANGE, LIMIT_OVERRIDE, REFUND_DISPUTE, ROLE_GRANT, GATE_EKYC, GATE_TIER, GATE_WALLET, GATE_IOT

    @Column(name = "entity_type", length = 50, nullable = false)
    private String entityType; // USER, CONFIG, WALLET, DEVICE, FEE

    @Column(name = "entity_id", length = 100)
    private String entityId;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "maker_user_id", nullable = false)
    private Long makerUserId;

    @Column(name = "maker_note", columnDefinition = "TEXT")
    private String makerNote;

    @Column(name = "payload_before", columnDefinition = "JSON")
    private String payloadBefore;

    @Column(name = "payload_after", columnDefinition = "JSON", nullable = false)
    private String payloadAfter;

    @Column(name = "checker_user_id")
    private Long checkerUserId;

    @Column(name = "checker_note", columnDefinition = "TEXT")
    private String checkerNote;

    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED, CANCELLED

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @OneToMany(mappedBy = "approvalRequestId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ApprovalHistoryEntity> historyList = new ArrayList<>();
}

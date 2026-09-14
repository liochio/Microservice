package com.liochio.ledger.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * ==============================================================================
 * Bút Toán Sổ Cái Kép Bất Biến (Double-Entry Journal Entry Entity)
 * ==============================================================================
 */
@Entity
@Table(name = "journal_entries", indexes = {
        @Index(name = "idx_jrn_tenant_type", columnList = "tenant_id, transaction_type"),
        @Index(name = "idx_jrn_idempotency", columnList = "idempotency_key", unique = true)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", length = 50, nullable = false)
    @Builder.Default
    private String tenantId = "SYSTEM";

    @Column(name = "entry_no", length = 64, nullable = false, unique = true)
    private String entryNo;

    @Column(name = "transaction_type", length = 40, nullable = false)
    private String transactionType; // TOPUP, WITHDRAW, TRANSFER, PIGGY_LOCK, PIGGY_UNLOCK, PARENT_BONUS, FEE

    @Column(name = "reference_id", length = 64)
    private String referenceId;

    @Column(name = "idempotency_key", length = 128, nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", length = 10, nullable = false)
    @Builder.Default
    private String currency = "VND";

    @Column(name = "description", length = 255, nullable = false)
    private String description;

    @Column(name = "posted_at", nullable = false)
    @Builder.Default
    private Instant postedAt = Instant.now();

    @Column(name = "prev_hash", length = 64, nullable = false)
    private String prevHash;

    @Column(name = "current_hash", length = 64, nullable = false)
    private String currentHash;

    @Column(name = "created_by", length = 50, nullable = false)
    @Builder.Default
    private String createdBy = "SYSTEM";

    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<JournalEntryDetailEntity> details = new ArrayList<>();
}

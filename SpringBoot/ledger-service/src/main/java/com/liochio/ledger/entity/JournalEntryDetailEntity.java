package com.liochio.ledger.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * ==============================================================================
 * Chi Tiết Bút Toán Sổ Cái Kép (Journal Entry Detail Entity)
 * ==============================================================================
 */
@Entity
@Table(name = "journal_entry_details", indexes = {
        @Index(name = "idx_jed_account", columnList = "account_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntryDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_entry_id", nullable = false)
    private JournalEntryEntity journalEntry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private LedgerAccountEntity account;

    @Column(name = "entry_type", length = 10, nullable = false)
    private String entryType; // DEBIT, CREDIT

    @Column(name = "amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "balance_after", precision = 18, scale = 2, nullable = false)
    private BigDecimal balanceAfter;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}

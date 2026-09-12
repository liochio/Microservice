package com.liochio.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * ==============================================================================
 * Tài Khoản Sổ Cái Kế Toán (Ledger Account Entity)
 * ==============================================================================
 * 
 * Phân tách 3 trạng thái số dư:
 * - USER_AVAILABLE: Số dư khả dụng thực tế
 * - USER_HOLDING: Số dư tạm giữ đang chờ quyết toán
 * - USER_ESCROW: Số dư khóa trong két Heo đất kỷ luật
 * - SYSTEM_SETTLEMENT: Tài khoản trung gian đối soát toàn hệ thống
 */
@Entity
@Table(name = "ledger_accounts", indexes = {
        @Index(name = "idx_ledger_tenant_user_type", columnList = "tenant_id, user_id, account_type")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", length = 50, nullable = false)
    @Builder.Default
    private String tenantId = "SYSTEM";

    @Column(name = "account_number", length = 50, nullable = false, unique = true)
    private String accountNumber;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "account_type", length = 40, nullable = false)
    private String accountType; // USER_AVAILABLE, USER_HOLDING, USER_ESCROW, SYSTEM_SETTLEMENT, SYSTEM_REVENUE

    @Column(name = "currency", length = 10, nullable = false)
    @Builder.Default
    private String currency = "VND";

    @Column(name = "balance", precision = 18, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, FROZEN, CLOSED

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    @Builder.Default
    private Instant updatedAt = Instant.now();
}

package com.liochio.worker.service;

import com.liochio.worker.dto.ReconciliationReportResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LedgerReconciliationService {

    private final JdbcTemplate jdbcTemplate;

    public ReconciliationReportResponse performAudit() {
        log.info("[LedgerReconciliationService] Bắt đầu phiên đối soát Sổ cái kép (EOD Audit)...");
        Instant now = Instant.now();

        try {
            // 1. Tính tổng Nợ và tổng Có trong liochio_core_db.journal_entry_details
            String sql = "SELECT " +
                    "COALESCE(SUM(CASE WHEN entry_type = 'DEBIT' THEN amount ELSE 0 END), 0) as total_debit, " +
                    "COALESCE(SUM(CASE WHEN entry_type = 'CREDIT' THEN amount ELSE 0 END), 0) as total_credit " +
                    "FROM liochio_core_db.journal_entry_details";

            Map<String, Object> sums = jdbcTemplate.queryForMap(sql);
            BigDecimal totalDebit = new BigDecimal(sums.get("total_debit").toString());
            BigDecimal totalCredit = new BigDecimal(sums.get("total_credit").toString());
            BigDecimal discrepancy = totalDebit.subtract(totalCredit).abs();

            boolean balanced = discrepancy.compareTo(BigDecimal.ZERO) == 0;

            // 2. Đếm số lượng tài khoản sổ cái và ví
            Long ledgerAccounts = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM liochio_core_db.ledger_accounts", Long.class);
            Long wallets = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM liochio_app_db.wallets", Long.class);

            String status = balanced ? "BALANCED" : "DISCREPANCY_DETECTED";
            String msg = balanced
                    ? "Sổ cái kép hoàn toàn cân bằng (Debit == Credit)"
                    : "CẢNH BÁO: Phát hiện chênh lệch Nợ/Có: " + discrepancy;

            log.info("[LedgerReconciliationService] Kết quả đối soát: Status={}, Debit={}, Credit={}, Lệch={}",
                    status, totalDebit, totalCredit, discrepancy);

            return ReconciliationReportResponse.builder()
                    .auditTimestamp(now)
                    .totalDebit(totalDebit)
                    .totalCredit(totalCredit)
                    .discrepancy(discrepancy)
                    .isBalanced(balanced)
                    .totalLedgerAccounts(ledgerAccounts != null ? ledgerAccounts : 0)
                    .totalWalletsAudited(wallets != null ? wallets : 0)
                    .status(status)
                    .message(msg)
                    .build();

        } catch (Exception e) {
            log.error("[LedgerReconciliationService] Lỗi khi thực thi đối soát sổ cái: {}", e.getMessage());
            return ReconciliationReportResponse.builder()
                    .auditTimestamp(now)
                    .totalDebit(BigDecimal.ZERO)
                    .totalCredit(BigDecimal.ZERO)
                    .discrepancy(BigDecimal.ZERO)
                    .isBalanced(true)
                    .status("ERROR")
                    .message("Lỗi kết nối đối soát: " + e.getMessage())
                    .build();
        }
    }
}

package com.liochio.auth.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntryResponse {

    private String entryNo;
    private String transactionType;
    private String referenceId;
    private String idempotencyKey;
    private BigDecimal amount;
    private String currency;
    private String description;
    private Instant postedAt;
    private String prevHash;
    private String currentHash;
    private List<JournalDetailDto> details;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JournalDetailDto {
        private String accountNumber;
        private String accountType;
        private String entryType; // DEBIT, CREDIT
        private BigDecimal amount;
        private BigDecimal balanceAfter;
    }
}

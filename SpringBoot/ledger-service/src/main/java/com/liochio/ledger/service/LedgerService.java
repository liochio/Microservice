package com.liochio.ledger.service;

import com.liochio.common.exception.AppException;
import com.liochio.common.exception.ErrorCode;
import com.liochio.common.outbox.OutboxPublisher;
import com.liochio.ledger.dto.JournalEntryResponse;
import com.liochio.ledger.dto.LedgerBalanceResponse;
import com.liochio.ledger.dto.M2MTransactionRequest;
import com.liochio.ledger.entity.JournalEntryDetailEntity;
import com.liochio.ledger.entity.JournalEntryEntity;
import com.liochio.ledger.entity.LedgerAccountEntity;
import com.liochio.ledger.repository.JournalEntryDetailRepository;
import com.liochio.ledger.repository.JournalEntryRepository;
import com.liochio.ledger.repository.LedgerAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerAccountRepository accountRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryDetailRepository detailRepository;
    private final OutboxPublisher outboxPublisher;

    public static final String ACC_SYS_SETTLEMENT = "ACC_SYS_SETTLEMENT";
    public static final String ACC_SYS_REVENUE = "ACC_SYS_REVENUE";

    @Transactional
    public LedgerAccountEntity getOrCreateAccount(String tenantId, Long userId, String accountType) {
        String safeTenant = (tenantId != null && !tenantId.isBlank()) ? tenantId : "SYSTEM";
        String accNumber;
        if (userId == null) {
            accNumber = "ACC_SYS_" + accountType;
        } else {
            String shortType = switch (accountType) {
                case "USER_HOLDING" -> "HOLD";
                case "USER_ESCROW" -> "ESCROW";
                default -> "AVAIL";
            };
            accNumber = "ACC_USR_" + userId + "_" + shortType;
        }

        return accountRepository.findByTenantIdAndAccountNumber(safeTenant, accNumber)
                .orElseGet(() -> {
                    LedgerAccountEntity newAcc = LedgerAccountEntity.builder()
                            .tenantId(safeTenant)
                            .accountNumber(accNumber)
                            .userId(userId)
                            .accountType(accountType)
                            .currency("VND")
                            .balance(BigDecimal.ZERO)
                            .status("ACTIVE")
                            .version(0L)
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build();
                    return accountRepository.save(newAcc);
                });
    }

    @Transactional
    public LedgerBalanceResponse getUserBalance(String tenantId, Long userId) {
        String safeTenant = (tenantId != null && !tenantId.isBlank()) ? tenantId : "SYSTEM";

        List<LedgerAccountEntity> userAccounts = accountRepository.findAllByTenantIdAndUserId(safeTenant, userId);
        if (userAccounts.isEmpty()) {
            userAccounts = accountRepository.findAllByTenantIdAndUserId("SYSTEM", userId);
        }
        if (userAccounts.isEmpty()) {
            userAccounts = accountRepository.findAllByTenantIdAndUserId("RETAIL", userId);
        }

        BigDecimal available = BigDecimal.ZERO;
        BigDecimal holding = BigDecimal.ZERO;
        BigDecimal escrow = BigDecimal.ZERO;

        if (!userAccounts.isEmpty()) {
            for (LedgerAccountEntity acc : userAccounts) {
                if ("USER_HOLDING".equalsIgnoreCase(acc.getAccountType()) || "HOLD".equalsIgnoreCase(acc.getAccountType())) {
                    holding = holding.add(acc.getBalance());
                } else if ("USER_ESCROW".equalsIgnoreCase(acc.getAccountType()) || "CORP_ESCROW".equalsIgnoreCase(acc.getAccountType()) || "ESCROW".equalsIgnoreCase(acc.getAccountType())) {
                    escrow = escrow.add(acc.getBalance());
                } else {
                    available = available.add(acc.getBalance());
                }
            }
        } else {
            LedgerAccountEntity availAcc = getOrCreateAccount(safeTenant, userId, "USER_AVAILABLE");
            LedgerAccountEntity holdAcc = getOrCreateAccount(safeTenant, userId, "USER_HOLDING");
            LedgerAccountEntity escrowAcc = getOrCreateAccount(safeTenant, userId, "USER_ESCROW");
            available = availAcc.getBalance();
            holding = holdAcc.getBalance();
            escrow = escrowAcc.getBalance();
        }

        BigDecimal total = available.add(holding).add(escrow);

        return LedgerBalanceResponse.builder()
                .userId(userId)
                .tenantId(safeTenant)
                .availableBalance(available)
                .holdingBalance(holding)
                .escrowBalance(escrow)
                .totalBalance(total)
                .currency("VND")
                .ekycLevel("TIER_2")
                .dailyTransferLimit(new BigDecimal("50000000.00"))
                .status("ACTIVE")
                .build();
    }

    @Transactional(readOnly = true)
    public LedgerBalanceResponse getAccountBalanceByNo(String accountNo) {
        String cleanNo = (accountNo != null) ? accountNo.trim() : "";
        LedgerAccountEntity account = accountRepository.findByAccountNumber(cleanNo)
                .or(() -> accountRepository.findByTenantIdAndAccountNumber("SYSTEM", cleanNo))
                .or(() -> accountRepository.findByTenantIdAndAccountNumber("TENANT_001", cleanNo))
                .or(() -> accountRepository.findByTenantIdAndAccountNumber("RETAIL", cleanNo))
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy tài khoản Sổ cái Core Banking: " + accountNo));

        return LedgerBalanceResponse.builder()
                .userId(account.getUserId())
                .tenantId(account.getTenantId())
                .availableBalance(account.getBalance())
                .totalBalance(account.getBalance())
                .holdingBalance(BigDecimal.ZERO)
                .escrowBalance(BigDecimal.ZERO)
                .currency(account.getCurrency())
                .status(account.getStatus())
                .build();
    }

    @Transactional
    public JournalEntryResponse processTransaction(M2MTransactionRequest request) {
        String safeTenant = (request.getTenantId() != null && !request.getTenantId().isBlank()) ? request.getTenantId() : "SYSTEM";
        BigDecimal amount = request.getAmount();

        // 1. Kiểm tra Idempotency Key
        Optional<JournalEntryEntity> existingOpt = journalEntryRepository.findByTenantIdAndIdempotencyKey(safeTenant, request.getIdempotencyKey());
        if (existingOpt.isPresent()) {
            log.info("[LedgerService] Trả về kết quả giao dịch đã thực hiện trước đó (Idempotent: {})", request.getIdempotencyKey());
            return mapToResponse(existingOpt.get());
        }

        // 2. Khóa bi quan các tài khoản liên quan (Pessimistic Lock)
        LedgerAccountEntity debitAcc;
        LedgerAccountEntity creditAcc;
        String txType = request.getTransactionType().toUpperCase();

        switch (txType) {
            case "TOPUP", "DEPOSIT" -> {
                debitAcc = getAccountForUpdate(safeTenant, null, "SYSTEM_SETTLEMENT");
                creditAcc = getAccountForUpdate(safeTenant, request.getUserId(), "USER_AVAILABLE");
            }
            case "WITHDRAW" -> {
                checkDailyLimit(request.getUserId(), amount);
                debitAcc = getAccountForUpdate(safeTenant, request.getUserId(), "USER_AVAILABLE");
                creditAcc = getAccountForUpdate(safeTenant, null, "SYSTEM_SETTLEMENT");
                validateSufficientBalance(debitAcc, amount);
            }
            case "TRANSFER" -> {
                if (request.getTargetUserId() == null) {
                    throw new AppException(ErrorCode.INVALID_REQUEST, "Giao dịch chuyển khoản yêu cầu targetUserId");
                }
                checkDailyLimit(request.getUserId(), amount);
                debitAcc = getAccountForUpdate(safeTenant, request.getUserId(), "USER_AVAILABLE");
                creditAcc = getAccountForUpdate(safeTenant, request.getTargetUserId(), "USER_AVAILABLE");
                validateSufficientBalance(debitAcc, amount);
            }
            case "PIGGY_LOCK" -> {
                debitAcc = getAccountForUpdate(safeTenant, request.getUserId(), "USER_AVAILABLE");
                creditAcc = getAccountForUpdate(safeTenant, request.getUserId(), "USER_ESCROW");
                validateSufficientBalance(debitAcc, amount);
            }
            case "PIGGY_UNLOCK" -> {
                debitAcc = getAccountForUpdate(safeTenant, request.getUserId(), "USER_ESCROW");
                creditAcc = getAccountForUpdate(safeTenant, request.getUserId(), "USER_AVAILABLE");
                validateSufficientBalance(debitAcc, amount);
            }
            default -> throw new AppException(ErrorCode.INVALID_REQUEST, "Loại giao dịch không hỗ trợ: " + txType);
        }

        // 3. Cập nhật số dư
        debitAcc.setBalance(debitAcc.getBalance().subtract(amount));
        debitAcc.setUpdatedAt(Instant.now());
        accountRepository.save(debitAcc);

        creditAcc.setBalance(creditAcc.getBalance().add(amount));
        creditAcc.setUpdatedAt(Instant.now());
        accountRepository.save(creditAcc);

        // 4. SHA-256 Hash Chaining
        String prevHash = journalEntryRepository.findTopByTenantIdOrderByIdDesc(safeTenant)
                .map(JournalEntryEntity::getCurrentHash)
                .orElse("0000000000000000000000000000000000000000000000000000000000000000");

        String entryNo = "JRN_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Instant postedAt = Instant.now();
        String currentHash = calculateSha256(entryNo + "|" + amount + "|" + postedAt.toEpochMilli() + "|" + prevHash + "|" + request.getIdempotencyKey());

        // 5. Lưu Journal Entry và Details
        JournalEntryEntity entry = JournalEntryEntity.builder()
                .tenantId(safeTenant)
                .entryNo(entryNo)
                .transactionType(txType)
                .referenceId(request.getReferenceId())
                .idempotencyKey(request.getIdempotencyKey())
                .amount(amount)
                .currency("VND")
                .description(request.getDescription())
                .postedAt(postedAt)
                .prevHash(prevHash)
                .currentHash(currentHash)
                .createdBy("M2M_FINTECH")
                .build();

        journalEntryRepository.save(entry);

        JournalEntryDetailEntity debitDetail = JournalEntryDetailEntity.builder()
                .journalEntry(entry)
                .account(debitAcc)
                .entryType("DEBIT")
                .amount(amount)
                .balanceAfter(debitAcc.getBalance())
                .createdAt(postedAt)
                .build();

        JournalEntryDetailEntity creditDetail = JournalEntryDetailEntity.builder()
                .journalEntry(entry)
                .account(creditAcc)
                .entryType("CREDIT")
                .amount(amount)
                .balanceAfter(creditAcc.getBalance())
                .createdAt(postedAt)
                .build();

        detailRepository.save(debitDetail);
        detailRepository.save(creditDetail);
        entry.setDetails(List.of(debitDetail, creditDetail));

        // 6. Transactional Outbox Event
        if (outboxPublisher != null) {
            try {
                Map<String, Object> eventPayload = new HashMap<>();
                eventPayload.put("entryNo", entryNo);
                eventPayload.put("userId", request.getUserId());
                eventPayload.put("transactionType", txType);
                eventPayload.put("amount", amount);
                eventPayload.put("currency", "VND");
                eventPayload.put("referenceId", request.getReferenceId());
                eventPayload.put("idempotencyKey", request.getIdempotencyKey());
                eventPayload.put("postedAt", postedAt.toString());
                outboxPublisher.publish("LEDGER_TRANSACTION", entryNo, "TRANSACTION_POSTED", eventPayload);
            } catch (Exception e) {
                log.warn("[LedgerService] Bỏ qua outbox event nếu outbox table chưa được cấu hình: {}", e.getMessage());
            }
        }

        log.info("[LedgerService] Ghi sổ kép thành công: EntryNo={}, Type={}, Amount={} VND, PrevHash={}, CurrentHash={}",
                entryNo, txType, amount, prevHash.substring(0, 8), currentHash.substring(0, 8));

        return mapToResponse(entry);
    }

    @Transactional(readOnly = true)
    public Page<JournalEntryResponse> getUserJournalHistory(String tenantId, Long userId, Pageable pageable) {
        String safeTenant = (tenantId != null && !tenantId.isBlank()) ? tenantId : "SYSTEM";
        return journalEntryRepository.findAllByTenantIdAndUserId(safeTenant, userId, pageable)
                .map(this::mapToResponse);
    }

    private LedgerAccountEntity getAccountForUpdate(String tenantId, Long userId, String accountType) {
        LedgerAccountEntity account = getOrCreateAccount(tenantId, userId, accountType);
        return accountRepository.findByTenantIdAndAccountNumberForUpdate(account.getTenantId(), account.getAccountNumber())
                .orElse(account);
    }

    private void validateSufficientBalance(LedgerAccountEntity account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new AppException(ErrorCode.LEDGER_INSUFFICIENT_BALANCE,
                    "Số dư không đủ. Hiện có: " + account.getBalance() + " VND, Yêu cầu: " + amount + " VND");
        }
    }

    private void checkDailyLimit(Long userId, BigDecimal amount) {
        if (userId == null) return;
        BigDecimal dailyLimit = new BigDecimal("50000000.00");
        if (amount.compareTo(dailyLimit) > 0) {
            throw new AppException(ErrorCode.EKYC_LIMIT_EXCEEDED,
                    "Số tiền giao dịch (" + amount + " VND) vượt quá hạn mức ngày (" + dailyLimit + " VND)");
        }
    }

    private String calculateSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return UUID.randomUUID().toString().replace("-", "");
        }
    }

    private JournalEntryResponse mapToResponse(JournalEntryEntity entry) {
        List<JournalEntryResponse.JournalDetailDto> detailDtos = new ArrayList<>();
        if (entry.getDetails() != null) {
            for (JournalEntryDetailEntity d : entry.getDetails()) {
                detailDtos.add(JournalEntryResponse.JournalDetailDto.builder()
                        .accountNumber(d.getAccount() != null ? d.getAccount().getAccountNumber() : "N/A")
                        .accountType(d.getAccount() != null ? d.getAccount().getAccountType() : "N/A")
                        .entryType(d.getEntryType())
                        .amount(d.getAmount())
                        .balanceAfter(d.getBalanceAfter())
                        .build());
            }
        }

        return JournalEntryResponse.builder()
                .entryNo(entry.getEntryNo())
                .transactionType(entry.getTransactionType())
                .referenceId(entry.getReferenceId())
                .idempotencyKey(entry.getIdempotencyKey())
                .amount(entry.getAmount())
                .currency(entry.getCurrency())
                .description(entry.getDescription())
                .postedAt(entry.getPostedAt())
                .prevHash(entry.getPrevHash())
                .currentHash(entry.getCurrentHash())
                .details(detailDtos)
                .build();
    }
}

package com.liochio.auth.service;

import com.liochio.auth.dto.JournalEntryResponse;
import com.liochio.auth.dto.LedgerBalanceResponse;
import com.liochio.auth.dto.M2MTransactionRequest;
import com.liochio.auth.entity.JournalEntryDetailEntity;
import com.liochio.auth.entity.JournalEntryEntity;
import com.liochio.auth.entity.LedgerAccountEntity;
import com.liochio.auth.entity.UserEntity;
import com.liochio.auth.repository.JournalEntryDetailRepository;
import com.liochio.auth.repository.JournalEntryRepository;
import com.liochio.auth.repository.LedgerAccountRepository;
import com.liochio.auth.repository.UserRepository;
import com.liochio.common.exception.AppException;
import com.liochio.common.exception.ErrorCode;
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

/**
 * ==============================================================================
 * Dịch Vụ Sổ Cái Kép Core Banking (Double-Entry Ledger Engine)
 * ==============================================================================
 * 
 * Nguyên tắc bất biến:
 * 1. Tổng DEBIT = Tổng CREDIT trong mọi giao dịch.
 * 2. Khóa bi quan (SELECT ... FOR UPDATE) chống Race Condition.
 * 3. Chuỗi băm SHA-256 bảo đảm tính bất biến (Hash Chaining).
 * 4. Idempotency Key triệt tiêu rủi ro gọi lặp (Duplicate Request).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerAccountRepository accountRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryDetailRepository detailRepository;
    private final UserRepository userRepository;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    public static final String ACC_SYS_SETTLEMENT = "ACC_SYS_SETTLEMENT";
    public static final String ACC_SYS_REVENUE = "ACC_SYS_REVENUE";

    /**
     * Lấy hoặc tự động khởi tạo tài khoản sổ cái
     */
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

    /**
     * Lấy thông tin số dư đa trạng thái của người dùng
     */
    @Transactional
    public LedgerBalanceResponse getUserBalance(String tenantId, Long userId) {
        String safeTenant = (tenantId != null && !tenantId.isBlank()) ? tenantId : "SYSTEM";
        UserEntity user = userRepository.findById(userId).orElse(null);

        // Check if accounts exist by user_id
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
                .ekycLevel(user != null ? user.getEkycLevel() : "TIER_2")
                .dailyTransferLimit(user != null ? user.getDailyTransferLimit() : new BigDecimal("50000000.00"))
                .status("ACTIVE")
                .build();
    }

    /**
     * Tra cứu số dư tài khoản sổ cái Core Banking theo mã tài khoản (Dành cho Corp & Retail)
     */
    @Transactional(readOnly = true)
    public LedgerBalanceResponse getAccountBalanceByNo(String accountNo) {
        String cleanNo = (accountNo != null) ? accountNo.trim() : "";
        LedgerAccountEntity account = accountRepository.findByAccountNumber(cleanNo)
                .or(() -> accountRepository.findByTenantIdAndAccountNumber("SYSTEM", cleanNo))
                .or(() -> accountRepository.findByTenantIdAndAccountNumber("TENANT_001", cleanNo))
                .or(() -> accountRepository.findByTenantIdAndAccountNumber("RETAIL", cleanNo))
                .orElse(null);

        if (account == null) {
            try {
                List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                        "SELECT id, tenant_id, account_number, user_id, balance, status, currency FROM liochio_admin_db.ledger_accounts WHERE account_number = ?",
                        cleanNo
                );
                if (!rows.isEmpty()) {
                    Map<String, Object> r = rows.get(0);
                    BigDecimal bal = (BigDecimal) r.get("balance");
                    return LedgerBalanceResponse.builder()
                            .userId(r.get("user_id") != null ? ((Number) r.get("user_id")).longValue() : 1L)
                            .tenantId((String) r.get("tenant_id"))
                            .availableBalance(bal)
                            .totalBalance(bal)
                            .holdingBalance(BigDecimal.ZERO)
                            .escrowBalance(BigDecimal.ZERO)
                            .currency((String) r.get("currency"))
                            .status((String) r.get("status"))
                            .build();
                }
            } catch (Exception ignored) {}
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy tài khoản Sổ cái Core Banking: " + accountNo);
        }

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

    /**
     * Thực thi giao dịch kế toán sổ kép (Double-Entry Posting Engine)
     */
    @Transactional
    public JournalEntryResponse processTransaction(M2MTransactionRequest request) {
        String safeTenant = (request.getTenantId() != null && !request.getTenantId().isBlank()) ? request.getTenantId() : "SYSTEM";
        BigDecimal amount = request.getAmount();

        // 1. Kiểm tra Idempotency Key (Tránh xử lý 2 lần)
        Optional<JournalEntryEntity> existingOpt = journalEntryRepository.findByTenantIdAndIdempotencyKey(safeTenant, request.getIdempotencyKey());
        if (existingOpt.isPresent()) {
            log.info("[LedgerService] Trả về kết quả giao dịch đã thực hiện trước đó (Idempotent: {})", request.getIdempotencyKey());
            return mapToResponse(existingOpt.get());
        }

        // 2. Xác định các tài khoản liên quan và khóa bi quan (SELECT FOR UPDATE)
        LedgerAccountEntity debitAcc;
        LedgerAccountEntity creditAcc;
        String txType = request.getTransactionType().toUpperCase();

        switch (txType) {
            case "TOPUP", "DEPOSIT" -> {
                // Nạp tiền: DEBIT System Settlement -> CREDIT User Available
                debitAcc = getAccountForUpdate(safeTenant, null, "SYSTEM_SETTLEMENT");
                creditAcc = getAccountForUpdate(safeTenant, request.getUserId(), "USER_AVAILABLE");
            }
            case "WITHDRAW" -> {
                // Rút tiền: DEBIT User Available -> CREDIT System Settlement
                checkDailyLimit(request.getUserId(), amount);
                debitAcc = getAccountForUpdate(safeTenant, request.getUserId(), "USER_AVAILABLE");
                creditAcc = getAccountForUpdate(safeTenant, null, "SYSTEM_SETTLEMENT");
                validateSufficientBalance(debitAcc, amount);
            }
            case "TRANSFER" -> {
                // Chuyển khoản P2P: DEBIT Source User -> CREDIT Target User
                if (request.getTargetUserId() == null) {
                    throw new AppException(ErrorCode.INVALID_REQUEST, "Giao dịch chuyển khoản yêu cầu targetUserId");
                }
                checkDailyLimit(request.getUserId(), amount);
                debitAcc = getAccountForUpdate(safeTenant, request.getUserId(), "USER_AVAILABLE");
                creditAcc = getAccountForUpdate(safeTenant, request.getTargetUserId(), "USER_AVAILABLE");
                validateSufficientBalance(debitAcc, amount);
            }
            case "PIGGY_LOCK" -> {
                // Khóa Heo đất kỷ luật: DEBIT User Available -> CREDIT User Escrow
                debitAcc = getAccountForUpdate(safeTenant, request.getUserId(), "USER_AVAILABLE");
                creditAcc = getAccountForUpdate(safeTenant, request.getUserId(), "USER_ESCROW");
                validateSufficientBalance(debitAcc, amount);
            }
            case "PIGGY_UNLOCK" -> {
                // Mở khóa Heo đất: DEBIT User Escrow -> CREDIT User Available
                debitAcc = getAccountForUpdate(safeTenant, request.getUserId(), "USER_ESCROW");
                creditAcc = getAccountForUpdate(safeTenant, request.getUserId(), "USER_AVAILABLE");
                validateSufficientBalance(debitAcc, amount);
            }
            case "PARENT_BONUS" -> {
                // Cha mẹ duyệt thưởng: DEBIT Parent Available -> CREDIT Child Escrow
                if (request.getTargetUserId() == null) {
                    throw new AppException(ErrorCode.INVALID_REQUEST, "Thưởng phụ huynh yêu cầu targetUserId (ID của con)");
                }
                debitAcc = getAccountForUpdate(safeTenant, request.getUserId(), "USER_AVAILABLE");
                creditAcc = getAccountForUpdate(safeTenant, request.getTargetUserId(), "USER_ESCROW");
                validateSufficientBalance(debitAcc, amount);
            }
            default -> throw new AppException(ErrorCode.INVALID_REQUEST, "Loại giao dịch không hỗ trợ: " + txType);
        }

        // 3. Cập nhật số dư tài khoản
        debitAcc.setBalance(debitAcc.getBalance().subtract(amount));
        debitAcc.setUpdatedAt(Instant.now());
        accountRepository.save(debitAcc);

        creditAcc.setBalance(creditAcc.getBalance().add(amount));
        creditAcc.setUpdatedAt(Instant.now());
        accountRepository.save(creditAcc);

        // 4. Tính toán Chuỗi Băm SHA-256 Bất Biến (Hash Chaining)
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

        log.info("[LedgerService] Ghi sổ kép thành công: EntryNo={}, Type={}, Amount={} VND, PrevHash={}, CurrentHash={}",
                entryNo, txType, amount, prevHash.substring(0, 8), currentHash.substring(0, 8));

        return mapToResponse(entry);
    }

    /**
     * Tra cứu lịch sử sổ cái theo User ID
     */
    @Transactional(readOnly = true)
    public Page<JournalEntryResponse> getUserJournalHistory(String tenantId, Long userId, Pageable pageable) {
        String safeTenant = (tenantId != null && !tenantId.isBlank()) ? tenantId : "SYSTEM";
        return journalEntryRepository.findAllByTenantIdAndUserId(safeTenant, userId, pageable)
                .map(this::mapToResponse);
    }

    private LedgerAccountEntity getAccountForUpdate(String tenantId, Long userId, String accountType) {
        LedgerAccountEntity account = getOrCreateAccount(tenantId, userId, accountType); // Đảm bảo đã có bản ghi
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
        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user != null && amount.compareTo(user.getDailyTransferLimit()) > 0) {
            throw new AppException(ErrorCode.EKYC_LIMIT_EXCEEDED,
                    "Số tiền giao dịch (" + amount + " VND) vượt quá hạn mức ngày (" +
                            user.getDailyTransferLimit() + " VND) của cấp độ " + user.getEkycLevel());
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

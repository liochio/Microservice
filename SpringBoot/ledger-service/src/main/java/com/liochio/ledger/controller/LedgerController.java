package com.liochio.ledger.controller;

import com.liochio.common.context.TenantContext;
import com.liochio.common.context.UserContext;
import com.liochio.common.dto.ApiResponse;
import com.liochio.common.exception.AppException;
import com.liochio.common.exception.ErrorCode;
import com.liochio.common.utils.HmacUtils;
import com.liochio.ledger.dto.JournalEntryResponse;
import com.liochio.ledger.dto.LedgerBalanceResponse;
import com.liochio.ledger.dto.M2MTransactionRequest;
import com.liochio.ledger.service.LedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping({"/api/v1/ledger", "/api/ledger"})
@RequiredArgsConstructor
@Tag(name = "Core Banking & Ledger Controller", description = "Các API Sổ cái kép, tra cứu số dư và M2M API xử lý dòng tiền")
public class LedgerController {

    private final LedgerService ledgerService;

    @Value("${m2m.secret-key:liochio-fintech-m2m-secret-key-2026}")
    private String m2mSecretKey;

    @GetMapping("/balance")
    @Operation(summary = "Tra cứu số dư tài khoản đa trạng thái (Available, Holding, Escrow) của chính mình")
    public ApiResponse<LedgerBalanceResponse> getMyBalance(@RequestParam(required = false) String tenantId) {
        String effectiveTenant = (tenantId != null && !tenantId.isBlank()) ? tenantId : TenantContext.getTenantId();
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Vui lòng đăng nhập để tra cứu số dư tài khoản");
        }
        LedgerBalanceResponse response = ledgerService.getUserBalance(effectiveTenant, userId);
        return ApiResponse.success(response);
    }

    @GetMapping("/balance/{userId}")
    @Operation(summary = "Tra cứu số dư tài khoản của một người dùng theo User ID (Dành cho Admin/Core)")
    public ApiResponse<LedgerBalanceResponse> getUserBalance(
            @PathVariable Long userId,
            @RequestParam(required = false) String tenantId
    ) {
        String effectiveTenant = (tenantId != null && !tenantId.isBlank()) ? tenantId : TenantContext.getTenantId();
        LedgerBalanceResponse response = ledgerService.getUserBalance(effectiveTenant, userId);
        return ApiResponse.success(response);
    }

    @GetMapping("/account/{accountNo}")
    @Operation(summary = "Tra cứu số dư tài khoản sổ cái Core Banking theo mã tài khoản (Dành cho Corp và Retail)")
    public ApiResponse<LedgerBalanceResponse> getAccountBalance(@PathVariable String accountNo) {
        LedgerBalanceResponse response = ledgerService.getAccountBalanceByNo(accountNo);
        return ApiResponse.success(response);
    }

    @GetMapping("/history/{userId}")
    @Operation(summary = "Tra cứu sao kê lịch sử sổ cái kép của một người dùng")
    public ApiResponse<Page<JournalEntryResponse>> getUserJournalHistory(
            @PathVariable Long userId,
            @RequestParam(required = false) String tenantId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        String effectiveTenant = (tenantId != null && !tenantId.isBlank()) ? tenantId : TenantContext.getTenantId();
        Page<JournalEntryResponse> page = ledgerService.getUserJournalHistory(effectiveTenant, userId, pageable);
        return ApiResponse.success(page);
    }

    @PostMapping("/m2m/transaction")
    @Operation(summary = "Cổng Machine-to-Machine (M2M) nhận lệnh hạch toán từ Python FinTech có kiểm tra chữ ký HMAC-SHA256")
    public ApiResponse<JournalEntryResponse> executeM2MTransaction(
            @RequestHeader(value = "X-M2M-Signature", required = false) String signature,
            @RequestHeader(value = "X-M2M-Timestamp", required = false) String timestamp,
            @Valid @RequestBody M2MTransactionRequest request
    ) {
        if (signature != null && !signature.isBlank() && timestamp != null) {
            long reqTime = Long.parseLong(timestamp);
            long currentTime = System.currentTimeMillis();
            if (Math.abs(currentTime - reqTime) > 300_000) {
                throw new AppException(ErrorCode.M2M_TIMESTAMP_EXPIRED, "Yêu cầu M2M đã quá hạn hiệu lực (Timestamp lệch quá 5 phút)");
            }

            String payload = request.getUserId() + "|" + request.getTransactionType() + "|" + request.getAmount() + "|" + request.getIdempotencyKey() + "|" + timestamp;
            boolean isValid = HmacUtils.verify(payload, m2mSecretKey, signature);
            if (!isValid) {
                log.warn("[LedgerController] Chữ ký HMAC không hợp lệ cho IdempotencyKey: {}", request.getIdempotencyKey());
                throw new AppException(ErrorCode.M2M_HMAC_INVALID, "Chữ ký số M2M HMAC-SHA256 không hợp lệ");
            }
        }

        if (request.getTenantId() == null || request.getTenantId().isBlank()) {
            request.setTenantId(TenantContext.getTenantId());
        }

        JournalEntryResponse response = ledgerService.processTransaction(request);
        return ApiResponse.created(response, "Hạch toán giao dịch vào sổ cái kép thành công");
    }
}

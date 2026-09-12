package com.liochio.auth.controller;

import com.liochio.auth.dto.CustomerGateDto;
import com.liochio.auth.service.CustomerGateService;
import com.liochio.common.context.TenantContext;
import com.liochio.common.context.UserContext;
import com.liochio.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class CustomerGateController {

    private final CustomerGateService customerGateService;

    // --- CORP ADMIN ENDPOINTS ---

    @GetMapping({"/api/v1/corp/customer-gates", "/api/corp/customer-gates"})
    @PreAuthorize("hasAnyAuthority('CORP_CUSTOMER_MGMT:READ', 'ROLE_SUPER_ADMIN', 'ROLE_TENANT_ADMIN', 'ROLE_MAKER', 'ROLE_CHECKER')")
    public ResponseEntity<ApiResponse<Page<CustomerGateDto>>> listCustomerGates(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        String tenantId = TenantContext.getTenantId();
        Page<CustomerGateDto> result = customerGateService.listCustomerGates(tenantId, status, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy danh sách 4 Cửa ải Khách hàng thành công!"));
    }

    @GetMapping({"/api/v1/corp/customer-gates/{userId}", "/api/corp/customer-gates/{userId}"})
    @PreAuthorize("hasAnyAuthority('CORP_CUSTOMER_MGMT:READ', 'ROLE_SUPER_ADMIN', 'ROLE_TENANT_ADMIN', 'ROLE_MAKER', 'ROLE_CHECKER')")
    public ResponseEntity<ApiResponse<CustomerGateDto>> getCustomerGate(@PathVariable("userId") Long userId) {
        String tenantId = TenantContext.getTenantId();
        CustomerGateDto result = customerGateService.getOrCreateGate(userId, tenantId);
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy thông tin 4 Cửa ải Khách hàng thành công!"));
    }

    @PostMapping({"/api/v1/corp/customer-gates/{userId}/gate1-review", "/api/corp/customer-gates/{userId}/gate1-review"})
    @PreAuthorize("hasAnyAuthority('CORP_CUST_GATE1_EKYC:APPROVE', 'ROLE_SUPER_ADMIN', 'ROLE_TENANT_ADMIN', 'ROLE_CHECKER')")
    public ResponseEntity<ApiResponse<CustomerGateDto>> reviewGate1(
            @PathVariable("userId") Long userId,
            @RequestBody CustomerGateDto.Gate1ReviewRequest request
    ) {
        Long reviewerId = UserContext.getUserId() != null ? UserContext.getUserId() : 1L;
        CustomerGateDto result = customerGateService.reviewGate1Ekyc(userId, reviewerId, request);
        return ResponseEntity.ok(ApiResponse.success(result, "Thẩm định Gate 1 (eKYC) thành công!"));
    }

    @PostMapping({"/api/v1/corp/customer-gates/{userId}/gate2-tier", "/api/corp/customer-gates/{userId}/gate2-tier"})
    @PreAuthorize("hasAnyAuthority('CORP_CUST_GATE2_ROLE_TIER:APPROVE', 'ROLE_SUPER_ADMIN', 'ROLE_TENANT_ADMIN', 'ROLE_CHECKER')")
    public ResponseEntity<ApiResponse<CustomerGateDto>> assignGate2(
            @PathVariable("userId") Long userId,
            @RequestBody CustomerGateDto.Gate2TierAssignRequest request
    ) {
        Long reviewerId = UserContext.getUserId() != null ? UserContext.getUserId() : 1L;
        CustomerGateDto result = customerGateService.assignGate2Tier(userId, reviewerId, request);
        return ResponseEntity.ok(ApiResponse.success(result, "Cấp phát Vai trò & Xếp hạng Gate 2 thành công!"));
    }

    @PostMapping({"/api/v1/corp/customer-gates/{userId}/gate3-wallets", "/api/corp/customer-gates/{userId}/gate3-wallets"})
    @PreAuthorize("hasAnyAuthority('CORP_CUST_GATE3_WALLETS:APPROVE', 'ROLE_SUPER_ADMIN', 'ROLE_TENANT_ADMIN', 'ROLE_MAKER', 'ROLE_CHECKER')")
    public ResponseEntity<ApiResponse<CustomerGateDto>> provisionGate3(@PathVariable("userId") Long userId) {
        CustomerGateDto result = customerGateService.provisionGate3Wallets(userId);
        return ResponseEntity.ok(ApiResponse.success(result, "Kích hoạt Cặp Tài khoản Sổ cái Gate 3 thành công!"));
    }

    @PostMapping({"/api/v1/corp/customer-gates/{userId}/gate4-pair", "/api/corp/customer-gates/{userId}/gate4-pair"})
    @PreAuthorize("hasAnyAuthority('CORP_CUST_GATE4_IOT_PAIRING:APPROVE', 'ROLE_SUPER_ADMIN', 'ROLE_TENANT_ADMIN', 'ROLE_MAKER', 'ROLE_CHECKER')")
    public ResponseEntity<ApiResponse<CustomerGateDto>> pairGate4(
            @PathVariable("userId") Long userId,
            @RequestBody CustomerGateDto.Gate4PairingRequest request
    ) {
        CustomerGateDto result = customerGateService.pairGate4Device(userId, request);
        return ResponseEntity.ok(ApiResponse.success(result, "Phê chuẩn Ghép đôi Heo Đất Gate 4 thành công!"));
    }

    // --- CONSUMER APP ENDPOINT ---

    @GetMapping({"/api/v1/app/onboarding-status", "/api/app/onboarding-status"})
    public ResponseEntity<ApiResponse<CustomerGateDto>> getMyOnboardingStatus() {
        Long userId = UserContext.getUserId() != null ? UserContext.getUserId() : 1L;
        String tenantId = TenantContext.getTenantId();
        CustomerGateDto result = customerGateService.getOrCreateGate(userId, tenantId);
        return ResponseEntity.ok(ApiResponse.success(result, "Tiến trình 4 Cửa ải của bạn!"));
    }
}

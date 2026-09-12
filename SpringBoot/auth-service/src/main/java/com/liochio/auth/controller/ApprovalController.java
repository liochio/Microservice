package com.liochio.auth.controller;

import com.liochio.auth.dto.ApprovalDto;
import com.liochio.auth.service.MakerCheckerService;
import com.liochio.common.context.TenantContext;
import com.liochio.common.context.UserContext;
import com.liochio.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping({"/api/v1/corp/approvals", "/api/corp/approvals"})
@RequiredArgsConstructor
public class ApprovalController {

    private final MakerCheckerService makerCheckerService;

    @PostMapping("/submit")
    @PreAuthorize("hasAnyAuthority('CORP_APPROVAL_CENTER:WRITE', 'ROLE_SUPER_ADMIN', 'ROLE_TENANT_ADMIN', 'ROLE_MAKER')")
    public ResponseEntity<ApiResponse<ApprovalDto>> submitRequest(
            @Valid @RequestBody ApprovalDto.SubmitRequest request,
            HttpServletRequest httpRequest
    ) {
        String tenantId = TenantContext.getTenantId();
        Long userId = UserContext.getUserId() != null ? UserContext.getUserId() : 1L;
        String ipAddress = httpRequest.getRemoteAddr();

        ApprovalDto result = makerCheckerService.submitRequest(tenantId, userId, request, ipAddress);
        return ResponseEntity.ok(ApiResponse.success(result, "Tạo yêu cầu phê duyệt thành công!"));
    }

    @PostMapping("/{id}/action")
    @PreAuthorize("hasAnyAuthority('CORP_APPROVAL_CENTER:APPROVE', 'ROLE_SUPER_ADMIN', 'ROLE_TENANT_ADMIN', 'ROLE_CHECKER')")
    public ResponseEntity<ApiResponse<ApprovalDto>> actionRequest(
            @PathVariable("id") Long id,
            @Valid @RequestBody ApprovalDto.ActionRequest request,
            HttpServletRequest httpRequest
    ) {
        Long checkerUserId = UserContext.getUserId() != null ? UserContext.getUserId() : 1L;
        String ipAddress = httpRequest.getRemoteAddr();

        ApprovalDto result = makerCheckerService.actionRequest(id, checkerUserId, request, ipAddress);
        return ResponseEntity.ok(ApiResponse.success(result, "Xử lý phê duyệt thành công!"));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('CORP_APPROVAL_CENTER:READ', 'ROLE_SUPER_ADMIN', 'ROLE_TENANT_ADMIN', 'ROLE_MAKER', 'ROLE_CHECKER')")
    public ResponseEntity<ApiResponse<Page<ApprovalDto>>> listRequests(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        String tenantId = TenantContext.getTenantId();
        Page<ApprovalDto> result = makerCheckerService.listRequests(tenantId, status, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy danh sách yêu cầu phê duyệt thành công!"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CORP_APPROVAL_CENTER:READ', 'ROLE_SUPER_ADMIN', 'ROLE_TENANT_ADMIN', 'ROLE_MAKER', 'ROLE_CHECKER')")
    public ResponseEntity<ApiResponse<ApprovalDto>> getRequestById(@PathVariable("id") Long id) {
        ApprovalDto result = makerCheckerService.getRequestById(id);
        return ResponseEntity.ok(ApiResponse.success(result, "Chi tiết yêu cầu phê duyệt!"));
    }
}

package com.liochio.auth.controller;

import com.liochio.auth.entity.UserDeviceEntity;
import com.liochio.auth.entity.UserSessionEntity;
import com.liochio.auth.repository.UserDeviceRepository;
import com.liochio.auth.repository.UserSessionRepository;
import com.liochio.common.context.TenantContext;
import com.liochio.common.context.UserContext;
import com.liochio.common.dto.ApiResponse;
import com.liochio.common.constant.MessageConstants;
import com.liochio.common.i18n.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ==============================================================================
 * Controller Bảo Mật & Quản Lý Thiết Bị, Phiên Đăng Nhập (Security Controller)
 * ==============================================================================
 */
@RestController
@RequestMapping("/api/auth/security")
@RequiredArgsConstructor
@Tag(name = "Security & Zero Trust", description = "Các API quản lý thiết bị tin cậy, SmartOTP 2FA và thu hồi phiên JWT")
public class SecurityController {

    private final UserDeviceRepository userDeviceRepository;
    private final UserSessionRepository userSessionRepository;
    private final MessageService messageService;

    @GetMapping("/devices")
    @Operation(summary = "Lấy danh sách các thiết bị đã đăng nhập của người dùng")
    public ApiResponse<List<UserDeviceEntity>> getUserDevices() {
        String tenantId = TenantContext.getTenantId();
        Long userId = UserContext.getUserId();
        return ApiResponse.success(userDeviceRepository.findByTenantIdAndUserId(tenantId, userId));
    }

    @GetMapping("/sessions")
    @Operation(summary = "Lấy danh sách các phiên làm việc (Sessions) đang hoạt động")
    public ApiResponse<List<UserSessionEntity>> getActiveSessions() {
        String tenantId = TenantContext.getTenantId();
        Long userId = UserContext.getUserId();
        return ApiResponse.success(userSessionRepository.findByTenantIdAndUserIdAndIsRevokedFalse(tenantId, userId));
    }

    @PostMapping("/sessions/{sessionId}/revoke")
    @Operation(summary = "Thu hồi từ xa một phiên làm việc (Revoke Session)")
    public ApiResponse<Void> revokeSession(@PathVariable String sessionId) {
        userSessionRepository.findByIdAndIsRevokedFalse(sessionId).ifPresent(session -> {
            session.setIsRevoked(true);
            session.setRevokedReason("User requested remote logout");
            userSessionRepository.save(session);
        });
        return ApiResponse.noContent(messageService.getMessage(MessageConstants.MSG_AUTH_SESSION_REVOKED));
    }
}

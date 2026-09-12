package com.liochio.otp.controller;

import com.liochio.common.constant.MessageConstants;
import com.liochio.common.context.TenantContext;
import com.liochio.common.dto.ApiResponse;
import com.liochio.common.dto.otp.OtpServiceConfigDto;
import com.liochio.common.i18n.MessageService;
import com.liochio.otp.service.OtpConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/otp/config", "/api/otp/config"})
@RequiredArgsConstructor
@Tag(name = "OTP Service Config Controller", description = "Các API kiểm tra và Bật/Tắt chế độ Bypass OTP cho môi trường Dev")
public class OtpConfigController {

    private final OtpConfigService configService;
    private final MessageService messageService;

    @GetMapping
    @Operation(summary = "Xem trạng thái cấu hình và cờ Bật/Tắt OTP Service")
    public ApiResponse<OtpServiceConfigDto> getConfig(@RequestParam(required = false) String tenantId) {
        String effectiveTenantId = (tenantId != null && !tenantId.isBlank()) ? tenantId : TenantContext.getTenantId();
        OtpServiceConfigDto dto = configService.getConfigDto(effectiveTenantId);
        return ApiResponse.success(dto);
    }

    @PutMapping
    @Operation(summary = "Cập nhật cờ Bật/Tắt OTP Service (isEnabled, bypassInDev, devBypassCode)")
    public ApiResponse<OtpServiceConfigDto> updateConfig(
            @RequestParam(required = false) String tenantId,
            @RequestBody OtpServiceConfigDto request
    ) {
        String effectiveTenantId = (tenantId != null && !tenantId.isBlank()) ? tenantId : TenantContext.getTenantId();
        OtpServiceConfigDto updated = configService.updateConfig(effectiveTenantId, request);
        return ApiResponse.success(updated, messageService.getMessage(MessageConstants.MSG_OTP_CONFIG_UPDATED));
    }
}

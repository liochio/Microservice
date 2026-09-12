package com.liochio.otp.controller;

import com.liochio.common.constant.MessageConstants;
import com.liochio.common.context.TenantContext;
import com.liochio.common.dto.ApiResponse;
import com.liochio.common.dto.otp.*;
import com.liochio.common.i18n.MessageService;
import com.liochio.otp.service.DedicatedOtpService;
import com.liochio.otp.service.DedicatedSmartOtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/otp", "/api/otp"})
@RequiredArgsConstructor
@Tag(name = "Dedicated OTP & SmartOTP Controller", description = "Các API sinh, xác thực OTP 6 số và SmartOTP TOTP RFC 6238")
public class OtpController {

    private final DedicatedOtpService otpService;
    private final DedicatedSmartOtpService smartOtpService;
    private final MessageService messageService;

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Sinh mã xác thực OTP 6 số (Tự động hỗ trợ cờ bypass)")
    public ApiResponse<OtpGenerateResponse> generateOtp(@Valid @RequestBody OtpGenerateRequest request) {
        if (request.getTenantId() == null || request.getTenantId().isBlank()) {
            request.setTenantId(TenantContext.getTenantId());
        }
        OtpGenerateResponse response = otpService.generateOtp(request);
        return ApiResponse.created(response, messageService.getMessage(MessageConstants.MSG_OTP_GENERATED));
    }

    @PostMapping("/verify")
    @Operation(summary = "Xác thực mã OTP 6 số")
    public ApiResponse<OtpVerifyResponse> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        if (request.getTenantId() == null || request.getTenantId().isBlank()) {
            request.setTenantId(TenantContext.getTenantId());
        }
        OtpVerifyResponse response = otpService.verifyOtp(request);
        return ApiResponse.success(response, messageService.getMessage(
                Boolean.TRUE.equals(response.getIsBypassed()) ? MessageConstants.MSG_OTP_BYPASSED : MessageConstants.MSG_OTP_VERIFIED
        ));
    }

    @PostMapping("/smart-otp/setup")
    @Operation(summary = "Khởi tạo Base32 Secret & Barcode URI chuẩn TOTP RFC 6238")
    public ApiResponse<SmartOtpSetupResponse> setupSmartOtp(@Valid @RequestBody SmartOtpSetupRequest request) {
        if (request.getTenantId() == null || request.getTenantId().isBlank()) {
            request.setTenantId(TenantContext.getTenantId());
        }
        SmartOtpSetupResponse response = smartOtpService.setupSmartOtp(request);
        return ApiResponse.success(response, messageService.getMessage(MessageConstants.MSG_AUTH_SMART_OTP_SETUP_SUCCESS));
    }

    @PostMapping("/smart-otp/verify")
    @Operation(summary = "Xác thực và kích hoạt SmartOTP với mã TOTP 6 số & mã PIN")
    public ApiResponse<OtpVerifyResponse> verifySmartOtp(@Valid @RequestBody SmartOtpVerifyRequest request) {
        if (request.getTenantId() == null || request.getTenantId().isBlank()) {
            request.setTenantId(TenantContext.getTenantId());
        }
        OtpVerifyResponse response = smartOtpService.verifyAndEnrollSmartOtp(request);
        return ApiResponse.success(response, messageService.getMessage(
                Boolean.TRUE.equals(response.getIsBypassed()) ? MessageConstants.MSG_OTP_BYPASSED : MessageConstants.MSG_AUTH_SMART_OTP_VERIFY_SUCCESS
        ));
    }
}

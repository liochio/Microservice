package com.liochio.auth.client;

import com.liochio.common.dto.ApiResponse;
import com.liochio.common.dto.otp.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "otp-service", url = "${otp-service.url:http://localhost:8094}")
public interface OtpServiceClient {

    @PostMapping("/api/v1/otp/generate")
    ApiResponse<OtpGenerateResponse> generateOtp(@RequestBody OtpGenerateRequest request);

    @PostMapping("/api/v1/otp/verify")
    ApiResponse<OtpVerifyResponse> verifyOtp(@RequestBody OtpVerifyRequest request);

    @PostMapping("/api/v1/otp/smart-otp/setup")
    ApiResponse<SmartOtpSetupResponse> setupSmartOtp(@RequestBody SmartOtpSetupRequest request);

    @PostMapping("/api/v1/otp/smart-otp/verify")
    ApiResponse<OtpVerifyResponse> verifySmartOtp(@RequestBody SmartOtpVerifyRequest request);
}

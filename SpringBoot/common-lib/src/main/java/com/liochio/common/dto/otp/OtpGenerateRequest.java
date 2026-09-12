package com.liochio.common.dto.otp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpGenerateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String tenantId;

    @NotNull(message = "User ID không được để trống")
    private Long userId;

    @NotBlank(message = "Mục đích OTP không được để trống")
    private String purpose; // REGISTRATION, DEVICE_TRUST, RESET_PASSWORD, TRANSACTION_SIGN

    private String destination; // Email or Phone Number

    @Builder.Default
    private String otpType = "EMAIL"; // EMAIL, SMS, SMART_OTP

    private String txContextHash; // Ràng buộc ngữ cảnh giao dịch tài chính

    private String clientIp; // Địa chỉ IP người yêu cầu
}
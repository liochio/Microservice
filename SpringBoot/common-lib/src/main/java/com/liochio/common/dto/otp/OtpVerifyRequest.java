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
public class OtpVerifyRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String tenantId;

    @NotNull(message = "User ID không được để trống")
    private Long userId;

    @NotBlank(message = "Mục đích OTP không được để trống")
    private String purpose;

    @NotBlank(message = "Mã OTP không được để trống")
    private String otpCode;

    private String txContextHash; // Chữ ký xác thực ngữ cảnh giao dịch

    private String clientIp;
}
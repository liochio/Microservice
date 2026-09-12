package com.liochio.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifySmartOtpRequest {

    @NotBlank(message = "Mã SmartOTP không được để trống")
    private String otpCode;

    private String pin;

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }
}

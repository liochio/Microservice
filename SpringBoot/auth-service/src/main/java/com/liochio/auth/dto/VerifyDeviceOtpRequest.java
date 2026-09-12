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
public class VerifyDeviceOtpRequest {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    private String username;

    @NotBlank(message = "Mã thiết bị (deviceId) không được để trống")
    private String deviceId;

    @NotBlank(message = "Mã xác thực OTP không được để trống")
    @com.fasterxml.jackson.annotation.JsonAlias({"otp", "code", "otp_code"})
    private String otpCode;

    private String referenceId;

    @Builder.Default
    private Boolean rememberDevice = true;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
    public Boolean getRememberDevice() { return rememberDevice != null ? rememberDevice : true; }
    public void setRememberDevice(Boolean rememberDevice) { this.rememberDevice = rememberDevice; }
}

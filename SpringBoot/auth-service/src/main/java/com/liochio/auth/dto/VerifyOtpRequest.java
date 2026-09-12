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
public class VerifyOtpRequest {

    private String email;
    private String username;
    private Long userId;

    @NotBlank(message = "Mã OTP không được để trống")
    @com.fasterxml.jackson.annotation.JsonAlias({"otp", "code", "otp_code"})
    private String otpCode;

    private String otpPurpose;
    private String referenceId;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
    public String getOtpPurpose() { return otpPurpose; }
    public void setOtpPurpose(String otpPurpose) { this.otpPurpose = otpPurpose; }
    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
}

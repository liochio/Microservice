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
public class QrConfirmRequest {

    @NotBlank(message = "Session ID không được để trống")
    private String sessionId;

    private String mobileDeviceId;
    private String confirmPin;

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getMobileDeviceId() { return mobileDeviceId; }
    public void setMobileDeviceId(String mobileDeviceId) { this.mobileDeviceId = mobileDeviceId; }
    public String getConfirmPin() { return confirmPin; }
    public void setConfirmPin(String confirmPin) { this.confirmPin = confirmPin; }
}

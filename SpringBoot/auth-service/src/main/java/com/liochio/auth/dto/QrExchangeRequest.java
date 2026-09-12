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
public class QrExchangeRequest {

    @NotBlank(message = "Session ID không được để trống")
    private String sessionId;

    @NotBlank(message = "Exchange Auth Code không được để trống")
    private String exchangeAuthCode;

    @NotBlank(message = "Mã thiết bị Web không được để trống")
    private String webDeviceId;

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getExchangeAuthCode() { return exchangeAuthCode; }
    public void setExchangeAuthCode(String exchangeAuthCode) { this.exchangeAuthCode = exchangeAuthCode; }
    public String getWebDeviceId() { return webDeviceId; }
    public void setWebDeviceId(String webDeviceId) { this.webDeviceId = webDeviceId; }
}

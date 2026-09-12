package com.liochio.common.dto.otp;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmartOtpSetupResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String secret;
    private String qrBarcodeUri;
    private String issuer;
    private String accountName;
}

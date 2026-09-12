package com.liochio.common.dto.otp;

import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpGenerateResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String referenceId;
    private String destination;
    private Integer expiresInSeconds;
    private Boolean isBypassed;
    private String devBypassCode;
    private Instant expiresAt;
}

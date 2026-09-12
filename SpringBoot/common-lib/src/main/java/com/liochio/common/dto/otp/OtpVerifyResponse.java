package com.liochio.common.dto.otp;

import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerifyResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Boolean success;
    private Boolean isBypassed;
    private String message;
    private Instant verifiedAt;
}

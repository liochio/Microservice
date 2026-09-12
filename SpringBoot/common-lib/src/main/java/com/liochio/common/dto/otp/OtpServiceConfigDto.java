package com.liochio.common.dto.otp;

import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpServiceConfigDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String tenantId;
    private Boolean isEnabled;
    private Boolean bypassInDev;
    private String devBypassCode;
    private String environment;
    private Instant updatedAt;
}

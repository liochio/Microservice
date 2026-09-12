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
public class SmartOtpSetupRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String tenantId;

    @NotNull(message = "User ID không được để trống")
    private Long userId;

    @NotBlank(message = "Username không được để trống")
    private String username;
}

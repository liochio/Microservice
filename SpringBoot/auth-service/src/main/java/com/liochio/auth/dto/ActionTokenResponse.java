package com.liochio.auth.dto;

import lombok.*;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionTokenResponse {

    private String actionToken;
    private String purpose;
    private Long userId;
    private int expiresInSeconds;
    private Instant expiresAt;
}

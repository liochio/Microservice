package com.liochio.realtime.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.Principal;
import java.util.List;

/**
 * ==============================================================================
 * Định Danh Phiên WebSocket STOMP Người Dùng (Stomp Principal)
 * ==============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StompPrincipal implements Principal {

    private String name; // Trả về userId hoặc username
    private Long userId;
    private String username;
    private String tenantId;
    private List<String> roles;

    @Override
    public String getName() {
        return (name != null && !name.isBlank()) ? name : (userId != null ? String.valueOf(userId) : "anonymous");
    }
}
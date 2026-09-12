package com.liochio.entityservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * ==============================================================================
 * DTO Trả Về Cấu Hình Menu (Navigation Menu Response DTO)
 * ==============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NavigationMenuResponse {

    private Long id;
    private String tenantId;
    private String menuCode;
    private String title;
    private List<Map<String, Object>> items;
    private Instant createdAt;
    private Instant updatedAt;
}

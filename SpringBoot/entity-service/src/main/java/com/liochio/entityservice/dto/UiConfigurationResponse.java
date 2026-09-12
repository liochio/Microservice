package com.liochio.entityservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * ==============================================================================
 * DTO Trả Về Cấu Hình Giao Diện UI (UI Configuration Response DTO)
 * ==============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UiConfigurationResponse {

    private Long id;
    private String tenantId;
    private String pageCode;
    private String themeName;
    private Map<String, Object> layoutSchema;
    private Instant createdAt;
    private Instant updatedAt;
}

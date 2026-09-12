package com.liochio.entityservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * ==============================================================================
 * DTO Trả Về Định Nghĩa Biểu Mẫu (Form Definition Response DTO)
 * ==============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormDefinitionResponse {

    private Long id;
    private String tenantId;
    private String formCode;
    private String title;
    private Map<String, Object> fieldDefinitions;
    private Instant createdAt;
    private Instant updatedAt;
}

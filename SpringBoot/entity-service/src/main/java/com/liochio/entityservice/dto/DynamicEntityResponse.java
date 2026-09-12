package com.liochio.entityservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * ==============================================================================
 * DTO Trả Về Chi Tiết Thực Thể Động (Dynamic Entity Response DTO)
 * ==============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DynamicEntityResponse {

    private Long id;
    private String tenantId;
    private String entityType;
    private String slug;
    private String title;
    private Map<String, Object> attributes;
    private String status;
    private Long viewCount;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
}

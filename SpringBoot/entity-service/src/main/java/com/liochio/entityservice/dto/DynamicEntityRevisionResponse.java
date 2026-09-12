package com.liochio.entityservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DynamicEntityRevisionResponse {
    private Long id;
    private Long entityId;
    private Integer revisionNumber;
    private String entityType;
    private String slug;
    private String title;
    private Map<String, Object> attributesSnapshot;
    private String status;
    private String modifiedBy;
    private String changeSummary;
    private Instant createdAt;
}
package com.liochio.entityservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityStatusUpdateRequest {

    @NotBlank(message = "Trạng thái mới không được để trống")
    private String status; // DRAFT, PENDING_REVIEW, PUBLISHED, ARCHIVED, REJECTED

    private String reason;
}
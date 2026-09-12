package com.liochio.entityservice.dto;

import com.liochio.common.annotation.DynamicSanitize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * ==============================================================================
 * DTO Yêu Cầu Tạo/Sửa Thực Thể Động (Dynamic Entity Request DTO)
 * ==============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DynamicEntityRequest {

    @NotBlank(message = "Loại thực thể (entity_type) không được để trống")
    @DynamicSanitize(ruleKey = "STRIP_SPECIAL")
    private String entityType;

    @NotBlank(message = "Slug đường dẫn không được để trống")
    @DynamicSanitize(ruleKey = "STRIP_SPECIAL")
    private String slug;

    @NotBlank(message = "Tiêu đề không được để trống")
    @DynamicSanitize(ruleKey = "HTML")
    private String title;

    @NotNull(message = "Thuộc tính động (attributes) không được null")
    private Map<String, Object> attributes;

    @Builder.Default
    private String status = "PUBLISHED";
}

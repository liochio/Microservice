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
 * DTO Định Nghĩa Biểu Mẫu Động (Form Definition Request DTO)
 * ==============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormDefinitionRequest {

    @NotBlank(message = "Mã biểu mẫu (form_code) không được để trống")
    @DynamicSanitize(ruleKey = "STRIP_SPECIAL")
    private String formCode;

    @NotBlank(message = "Tiêu đề biểu mẫu không được để trống")
    @DynamicSanitize(ruleKey = "HTML")
    private String title;

    @NotNull(message = "Danh sách trường (field_definitions) không được null")
    private Map<String, Object> fieldDefinitions;
}

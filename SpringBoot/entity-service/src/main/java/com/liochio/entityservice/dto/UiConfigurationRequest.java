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
 * DTO Cấu Hình Giao Diện Server-Driven UI (UI Configuration Request DTO)
 * ==============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UiConfigurationRequest {

    @NotBlank(message = "Mã trang (page_code) không được để trống")
    @DynamicSanitize(ruleKey = "STRIP_SPECIAL")
    private String pageCode;

    @NotBlank(message = "Tên chủ đề (theme_name) không được để trống")
    @DynamicSanitize(ruleKey = "STRIP_SPECIAL")
    private String themeName;

    @NotNull(message = "Cấu trúc layout (layout_schema) không được null")
    private Map<String, Object> layoutSchema;
}

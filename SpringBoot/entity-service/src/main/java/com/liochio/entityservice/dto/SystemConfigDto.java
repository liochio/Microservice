package com.liochio.entityservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigDto {

    private String configKey;
    private String category;
    private String configName;
    private String dataType;
    private String effectiveValue;
    private String globalValue;
    private String overrideValue;
    private Boolean isOverridden;
    private Boolean isTenantOverridable;
    private String minValue;
    private String maxValue;
    private String description;
    private Instant updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GlobalConfigRequest {
        @NotBlank(message = "Config key không được để trống")
        private String configKey;
        @NotBlank(message = "Category không được để trống")
        private String category;
        @NotBlank(message = "Config name không được để trống")
        private String configName;
        @NotBlank(message = "Value không được để trống")
        private String value;
        private String dataType;
        private String description;
        private Boolean isTenantOverridable;
        private String minValue;
        private String maxValue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverrideRequest {
        @NotBlank(message = "Override value không được để trống")
        private String overrideValue;
    }
}

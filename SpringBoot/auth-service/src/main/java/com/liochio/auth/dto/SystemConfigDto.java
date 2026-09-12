package com.liochio.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
    private BigDecimal minValue;
    private BigDecimal maxValue;
    private String description;
    private Instant updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverrideRequest {
        private String overrideValue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GlobalConfigRequest {
        private String configKey;
        private String category;
        private String configName;
        private String dataType;
        private String value;
        private BigDecimal minValue;
        private BigDecimal maxValue;
        private Boolean isTenantOverridable;
        private String description;
    }
}

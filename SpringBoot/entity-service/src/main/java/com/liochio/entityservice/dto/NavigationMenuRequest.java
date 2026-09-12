package com.liochio.entityservice.dto;

import com.liochio.common.annotation.DynamicSanitize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * ==============================================================================
 * DTO Yêu Cầu Cấu Hình Menu Điều Hướng (Navigation Menu Request DTO)
 * ==============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NavigationMenuRequest {

    @NotBlank(message = "Mã menu (menu_code) không được để trống")
    @DynamicSanitize(ruleKey = "STRIP_SPECIAL")
    private String menuCode;

    @NotBlank(message = "Tiêu đề menu không được để trống")
    @DynamicSanitize(ruleKey = "HTML")
    private String title;

    @NotNull(message = "Danh sách mục menu (items) không được null")
    private List<Map<String, Object>> items;
}

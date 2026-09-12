package com.liochio.entityservice.controller;

import com.liochio.common.annotation.RequirePermission;
import com.liochio.common.constant.MessageConstants;
import com.liochio.common.dto.ApiResponse;
import com.liochio.common.i18n.MessageService;
import com.liochio.entityservice.dto.UiConfigurationRequest;
import com.liochio.entityservice.dto.UiConfigurationResponse;
import com.liochio.entityservice.service.UiConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * ==============================================================================
 * Controller Cấu Hình Giao Diện Server-Driven UI (UI Configuration Controller)
 * ==============================================================================
 */
@RestController
@RequestMapping("/api/ui-configs")
@RequiredArgsConstructor
@Tag(name = "UI Configuration Controller", description = "Các API trả về layout JSON cho Server-Driven UI")
public class UiConfigController {

    private final UiConfigService uiConfigService;
    private final MessageService messageService;

    @GetMapping("/{pageCode}")
    @Operation(summary = "Lấy cấu trúc layout JSON của một trang theo pageCode")
    public ApiResponse<UiConfigurationResponse> getByPageCode(@PathVariable String pageCode) {
        UiConfigurationResponse response = uiConfigService.getByPageCode(pageCode);
        return ApiResponse.success(response);
    }

    @PostMapping
    @RequirePermission("system:admin")
    @Operation(summary = "Lưu hoặc cập nhật cấu trúc layout UI của trang")
    public ApiResponse<UiConfigurationResponse> saveOrUpdate(@Valid @RequestBody UiConfigurationRequest request) {
        UiConfigurationResponse response = uiConfigService.saveOrUpdate(request);
        return ApiResponse.success(response, messageService.getMessage(MessageConstants.MSG_UI_CONFIG_SAVED));
    }
}

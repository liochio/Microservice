package com.liochio.entityservice.controller;

import com.liochio.common.annotation.RequirePermission;
import com.liochio.common.constant.MessageConstants;
import com.liochio.common.dto.ApiResponse;
import com.liochio.common.i18n.MessageService;
import com.liochio.entityservice.dto.NavigationMenuRequest;
import com.liochio.entityservice.dto.NavigationMenuResponse;
import com.liochio.entityservice.service.NavigationMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * ==============================================================================
 * Controller Cấu Hình Menu & Điều Hướng Động (Navigation Menu Controller)
 * ==============================================================================
 */
@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
@Tag(name = "Navigation Menu Controller", description = "Các API trả về menu điều hướng động")
public class NavigationMenuController {

    private final NavigationMenuService navigationMenuService;
    private final MessageService messageService;

    @GetMapping("/{menuCode}")
    @Operation(summary = "Lấy cấu trúc menu theo menuCode")
    public ApiResponse<NavigationMenuResponse> getByMenuCode(@PathVariable String menuCode) {
        NavigationMenuResponse response = navigationMenuService.getByMenuCode(menuCode);
        return ApiResponse.success(response);
    }

    @PostMapping
    @RequirePermission("system:admin")
    @Operation(summary = "Lưu hoặc cập nhật cấu trúc menu")
    public ApiResponse<NavigationMenuResponse> saveOrUpdate(@Valid @RequestBody NavigationMenuRequest request) {
        NavigationMenuResponse response = navigationMenuService.saveOrUpdate(request);
        return ApiResponse.success(response, messageService.getMessage(MessageConstants.MSG_MENU_SAVED));
    }
}

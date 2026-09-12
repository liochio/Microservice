package com.liochio.entityservice.controller;

import com.liochio.common.dto.ApiResponse;
import com.liochio.entityservice.entity.EntityTypeEntity;
import com.liochio.entityservice.entity.WebTemplateEntity;
import com.liochio.entityservice.repository.EntityTypeRepository;
import com.liochio.entityservice.repository.WebTemplateRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ==============================================================================
 * Controller Danh Mục Mẫu Giao Diện & Loại Thực Thể (Template & Schema Controller)
 * ==============================================================================
 */
@RestController
@RequestMapping("/api/entities/templates")
@RequiredArgsConstructor
@Tag(name = "Templates & Entity Types", description = "Các API truy vấn danh mục Web Templates và Loại thực thể động")
public class TemplateController {

    private final WebTemplateRepository templateRepository;
    private final EntityTypeRepository entityTypeRepository;

    @GetMapping
    @Operation(summary = "Lấy danh sách các mẫu giao diện Website có sẵn (Web Templates)")
    public ApiResponse<List<WebTemplateEntity>> getAllTemplates() {
        return ApiResponse.success(templateRepository.findAll());
    }

    @GetMapping("/types")
    @Operation(summary = "Lấy danh sách tất cả các loại thực thể động (Entity Types)")
    public ApiResponse<List<EntityTypeEntity>> getEntityTypes() {
        return ApiResponse.success(entityTypeRepository.findByIsActiveTrue());
    }
}

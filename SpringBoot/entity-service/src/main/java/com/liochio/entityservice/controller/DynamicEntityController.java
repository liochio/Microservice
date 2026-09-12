package com.liochio.entityservice.controller;

import com.liochio.common.annotation.RequirePermission;
import com.liochio.common.constant.MessageConstants;
import com.liochio.common.dto.ApiResponse;
import com.liochio.common.dto.PageResponse;
import com.liochio.common.i18n.MessageService;
import com.liochio.entityservice.dto.DynamicEntityRequest;
import com.liochio.entityservice.dto.DynamicEntityResponse;
import com.liochio.entityservice.dto.DynamicEntityRevisionResponse;
import com.liochio.entityservice.dto.EntityStatusUpdateRequest;
import com.liochio.entityservice.service.DynamicEntityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ==============================================================================
 * Controller Thực Thể Động Hybrid EAV + JSONB (Dynamic Entity Controller)
 * ==============================================================================
 */
@RestController
@RequestMapping({"/api/entities", "/api/v1/entities"})
@RequiredArgsConstructor
@Tag(name = "Dynamic Entity Controller", description = "Các API thao tác với Dynamic Engine Hybrid EAV + JSONB")
public class DynamicEntityController {

    private final DynamicEntityService dynamicEntityService;
    private final MessageService messageService;

    @GetMapping
    @Operation(summary = "Tìm kiếm danh sách dynamic entities có phân trang")
    public ApiResponse<PageResponse<DynamicEntityResponse>> search(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<DynamicEntityResponse> result = dynamicEntityService.search(entityType, status, keyword, pageable);
        return ApiResponse.success(result);
    }

    @GetMapping("/{entityType}/{slug}")
    @Operation(summary = "Lấy chi tiết dynamic entity theo entityType và slug")
    public ApiResponse<DynamicEntityResponse> getBySlug(
            @PathVariable String entityType,
            @PathVariable String slug
    ) {
        DynamicEntityResponse response = dynamicEntityService.getBySlug(entityType, slug);
        return ApiResponse.success(response);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequirePermission("entity:write")
    @Operation(summary = "Tạo mới dynamic entity kèm schema validation & versioning")
    public ApiResponse<DynamicEntityResponse> create(@Valid @RequestBody DynamicEntityRequest request) {
        DynamicEntityResponse response = dynamicEntityService.create(request);
        return ApiResponse.created(response, messageService.getMessage(MessageConstants.MSG_ENTITY_CREATED));
    }

    @PutMapping("/{id}")
    @RequirePermission("entity:write")
    @Operation(summary = "Cập nhật dynamic entity kèm snapshot revision mới")
    public ApiResponse<DynamicEntityResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody DynamicEntityRequest request
    ) {
        DynamicEntityResponse response = dynamicEntityService.update(id, request);
        return ApiResponse.success(response, messageService.getMessage(MessageConstants.MSG_ENTITY_UPDATED));
    }

    @PatchMapping("/{id}/status")
    @RequirePermission("entity:write")
    @Operation(summary = "Chuyển đổi trạng thái workflow của thực thể (DRAFT -> PENDING_REVIEW -> PUBLISHED -> ARCHIVED)")
    public ApiResponse<DynamicEntityResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody EntityStatusUpdateRequest request
    ) {
        DynamicEntityResponse response = dynamicEntityService.updateStatus(id, request);
        return ApiResponse.success(response, "Cập nhật trạng thái quy trình thành công");
    }

    @GetMapping("/{id}/revisions")
    @RequirePermission("entity:read")
    @Operation(summary = "Xem lịch sử các phiên bản revision của thực thể")
    public ApiResponse<List<DynamicEntityRevisionResponse>> getRevisions(@PathVariable Long id) {
        List<DynamicEntityRevisionResponse> revisions = dynamicEntityService.getRevisions(id);
        return ApiResponse.success(revisions);
    }

    @PostMapping("/{id}/rollback/{revisionNumber}")
    @RequirePermission("entity:write")
    @Operation(summary = "Hoàn tác (Rollback) thực thể về một phiên bản revision cụ thể trong quá khứ")
    public ApiResponse<DynamicEntityResponse> rollback(
            @PathVariable Long id,
            @PathVariable Integer revisionNumber
    ) {
        DynamicEntityResponse response = dynamicEntityService.rollback(id, revisionNumber);
        return ApiResponse.success(response, "Hoàn tác về phiên bản " + revisionNumber + " thành công");
    }

    @DeleteMapping("/{id}")
    @RequirePermission("entity:delete")
    @Operation(summary = "Xóa mềm dynamic entity")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        dynamicEntityService.delete(id);
        return ApiResponse.noContent(messageService.getMessage(MessageConstants.MSG_ENTITY_DELETED));
    }
}
package com.liochio.tour.controller;

import com.liochio.common.annotation.RequirePermission;
import com.liochio.common.constant.MessageConstants;
import com.liochio.common.context.TenantContext;
import com.liochio.common.dto.ApiResponse;
import com.liochio.common.dto.PageResponse;
import com.liochio.common.exception.AppException;
import com.liochio.common.exception.ErrorCode;
import com.liochio.common.i18n.MessageService;
import com.liochio.tour.entity.TourEntity;
import com.liochio.tour.repository.TourRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/tours", "/api/tours"})
@RequiredArgsConstructor
@Tag(name = "Tour Controller", description = "Dịch vụ độc lập quản lý tour du lịch, lịch trình và đặt chỗ")
public class TourController {

    private final TourRepository tourRepository;
    private final MessageService messageService;

    @GetMapping
    @Operation(summary = "Lấy danh sách các tour du lịch (phân trang)")
    public ApiResponse<PageResponse<TourEntity>> getAllTours(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String tenantId = TenantContext.getTenantId();
        Page<TourEntity> result = tourRepository.findByTenantIdAndStatus(
                tenantId, "PUBLISHED", PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ApiResponse.success(PageResponse.of(result));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Lấy chi tiết tour du lịch theo slug")
    public ApiResponse<TourEntity> getTourBySlug(@PathVariable String slug) {
        String tenantId = TenantContext.getTenantId();
        TourEntity tour = tourRepository.findByTenantIdAndSlug(tenantId, slug)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        return ApiResponse.success(tour);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequirePermission("entity:write")
    @Operation(summary = "Tạo mới tour du lịch")
    public ApiResponse<TourEntity> createTour(@RequestBody TourEntity request) {
        request.setTenantId(TenantContext.getTenantId());
        TourEntity saved = tourRepository.save(request);
        return ApiResponse.created(saved, messageService.getMessage(MessageConstants.MSG_TOUR_CREATED));
    }
}

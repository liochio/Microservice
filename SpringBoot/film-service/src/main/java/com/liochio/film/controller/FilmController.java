package com.liochio.film.controller;

import com.liochio.common.annotation.RequirePermission;
import com.liochio.common.constant.MessageConstants;
import com.liochio.common.context.TenantContext;
import com.liochio.common.dto.ApiResponse;
import com.liochio.common.dto.PageResponse;
import com.liochio.common.exception.AppException;
import com.liochio.common.exception.ErrorCode;
import com.liochio.common.i18n.MessageService;
import com.liochio.film.entity.MovieEntity;
import com.liochio.film.repository.MovieRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/films", "/api/films"})
@RequiredArgsConstructor
@Tag(name = "Film Controller", description = "Dịch vụ độc lập quản lý phim điện ảnh, tập phim và streaming CDN")
public class FilmController {

    private final MovieRepository movieRepository;
    private final MessageService messageService;

    @GetMapping
    @Operation(summary = "Lấy danh sách các bộ phim (phân trang)")
    public ApiResponse<PageResponse<MovieEntity>> getAllMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String tenantId = TenantContext.getTenantId();
        Page<MovieEntity> result = movieRepository.findByTenantId(
                tenantId, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ApiResponse.success(PageResponse.of(result));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Lấy thông tin chi tiết phim theo slug")
    public ApiResponse<MovieEntity> getMovieBySlug(@PathVariable String slug) {
        String tenantId = TenantContext.getTenantId();
        MovieEntity movie = movieRepository.findByTenantIdAndSlug(tenantId, slug)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        return ApiResponse.success(movie);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequirePermission("entity:write")
    @Operation(summary = "Thêm bộ phim mới vào kho dữ liệu")
    public ApiResponse<MovieEntity> createMovie(@RequestBody MovieEntity request) {
        request.setTenantId(TenantContext.getTenantId());
        MovieEntity saved = movieRepository.save(request);
        return ApiResponse.created(saved, messageService.getMessage(MessageConstants.MSG_FILM_MOVIE_CREATED));
    }
}

package com.liochio.music.controller;

import com.liochio.common.annotation.RequirePermission;
import com.liochio.common.constant.MessageConstants;
import com.liochio.common.context.TenantContext;
import com.liochio.common.dto.ApiResponse;
import com.liochio.common.dto.PageResponse;
import com.liochio.common.exception.AppException;
import com.liochio.common.exception.ErrorCode;
import com.liochio.common.i18n.MessageService;
import com.liochio.music.entity.SongEntity;
import com.liochio.music.repository.SongRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/music", "/api/music"})
@RequiredArgsConstructor
@Tag(name = "Music Controller", description = "Dịch vụ độc lập quản lý bài hát, album và stream nhạc số")
public class MusicController {

    private final SongRepository songRepository;
    private final MessageService messageService;

    @GetMapping("/songs")
    @Operation(summary = "Lấy danh sách các bài hát (phân trang)")
    public ApiResponse<PageResponse<SongEntity>> getAllSongs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String tenantId = TenantContext.getTenantId();
        Page<SongEntity> result = songRepository.findByTenantId(
                tenantId, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ApiResponse.success(PageResponse.of(result));
    }

    @GetMapping("/songs/{slug}")
    @Operation(summary = "Lấy chi tiết bài hát theo slug kèm audio streaming URL")
    public ApiResponse<SongEntity> getSongBySlug(@PathVariable String slug) {
        String tenantId = TenantContext.getTenantId();
        SongEntity song = songRepository.findByTenantIdAndSlug(tenantId, slug)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        return ApiResponse.success(song);
    }

    @PostMapping("/songs")
    @ResponseStatus(HttpStatus.CREATED)
    @RequirePermission("entity:write")
    @Operation(summary = "Thêm bài hát mới vào thư viện")
    public ApiResponse<SongEntity> createSong(@RequestBody SongEntity request) {
        request.setTenantId(TenantContext.getTenantId());
        SongEntity saved = songRepository.save(request);
        return ApiResponse.created(saved, messageService.getMessage(MessageConstants.MSG_MUSIC_SONG_CREATED));
    }
}

package com.liochio.media.controller;

import com.liochio.common.annotation.RequirePermission;
import com.liochio.common.constant.MessageConstants;
import com.liochio.common.dto.ApiResponse;
import com.liochio.common.enums.StorageProvider;
import com.liochio.common.i18n.MessageService;
import com.liochio.media.dto.MediaUploadResponse;
import com.liochio.media.service.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * ==============================================================================
 * Controller Tải Lên & Quản Lý Đa Phương Tiện (Media Controller)
 * ==============================================================================
 */
@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
@Tag(name = "Media Controller", description = "Các API tải lên tệp tin, hình ảnh và xử lý Chunk Upload")
public class MediaController {

    private final MediaService mediaService;
    private final MessageService messageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @RequirePermission("media:upload")
    @Operation(summary = "Tải lên một tệp tin (ảnh / video / tài liệu)")
    public ApiResponse<MediaUploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "provider", required = false, defaultValue = "LOCAL") StorageProvider provider,
            @RequestParam(value = "folder", required = false, defaultValue = "portfolio") String folder
    ) {
        MediaUploadResponse response = mediaService.upload(file, provider, folder);
        return ApiResponse.created(response, messageService.getMessage(MessageConstants.MSG_MEDIA_UPLOAD_SUCCESS));
    }

    @PostMapping(value = "/chunk-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequirePermission("media:upload")
    @Operation(summary = "Tải lên tệp tin dung lượng lớn theo từng phần (Chunk Upload)")
    public ApiResponse<String> uploadChunk(
            @RequestParam("chunk") MultipartFile chunk,
            @RequestParam("fileName") String fileName,
            @RequestParam("chunkIndex") int chunkIndex,
            @RequestParam("totalChunks") int totalChunks
    ) throws IOException {
        String result = mediaService.uploadChunk(chunk.getInputStream(), fileName, chunkIndex, totalChunks);
        return ApiResponse.success(result, messageService.getMessage(MessageConstants.MSG_MEDIA_CHUNK_SUCCESS));
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả các tệp tin media của tenant")
    public ApiResponse<List<MediaUploadResponse>> getAll() {
        List<MediaUploadResponse> result = mediaService.getAllMedia();
        return ApiResponse.success(result);
    }
}

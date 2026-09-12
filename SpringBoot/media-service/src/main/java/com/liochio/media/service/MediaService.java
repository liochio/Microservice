package com.liochio.media.service;

import com.liochio.common.context.TenantContext;
import com.liochio.common.enums.StorageProvider;
import com.liochio.common.pattern.factory.StorageFactory;
import com.liochio.common.pattern.strategy.StorageStrategy;
import com.liochio.media.dto.MediaUploadResponse;
import com.liochio.media.entity.MediaFileEntity;
import com.liochio.media.repository.MediaFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ==============================================================================
 * Dịch Vụ Quản Lý Tệp Tin Media (Media Management Service)
 * ==============================================================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MediaService {

    private final MediaFileRepository mediaFileRepository;
    private final StorageFactory storageFactory;

    @Transactional
    public MediaUploadResponse upload(MultipartFile file, StorageProvider provider, String folderPath) {
        String tenantId = TenantContext.getTenantId();
        StorageProvider targetProvider = provider != null ? provider : StorageProvider.LOCAL;
        StorageStrategy strategy = storageFactory.getStrategy(targetProvider);

        String fileUrl = strategy.uploadFile(file, folderPath);

        MediaFileEntity entity = MediaFileEntity.builder()
                .fileName(file.getOriginalFilename())
                .fileUrl(fileUrl)
                .storageProvider(targetProvider)
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .build();
        entity.setTenantId(tenantId);

        MediaFileEntity saved = mediaFileRepository.save(entity);
        log.info("[MediaService] Lưu tệp tin thành công ID: {}, URL: {}", saved.getId(), saved.getFileUrl());

        return mapToResponse(saved);
    }

    @Transactional
    public String uploadChunk(InputStream inputStream, String fileName, int chunkIndex, int totalChunks) {
        StorageStrategy strategy = storageFactory.getStrategy(StorageProvider.LOCAL);
        return strategy.uploadChunk(inputStream, fileName, chunkIndex, totalChunks);
    }

    @Transactional(readOnly = true)
    public List<MediaUploadResponse> getAllMedia() {
        String tenantId = TenantContext.getTenantId();
        return mediaFileRepository.findByTenantId(tenantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private MediaUploadResponse mapToResponse(MediaFileEntity entity) {
        return MediaUploadResponse.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .fileName(entity.getFileName())
                .fileUrl(entity.getFileUrl())
                .storageProvider(entity.getStorageProvider().name())
                .fileSize(entity.getFileSize())
                .contentType(entity.getContentType())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

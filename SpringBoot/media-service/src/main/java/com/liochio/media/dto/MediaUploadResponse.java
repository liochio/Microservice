package com.liochio.media.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * ==============================================================================
 * DTO Trả Về Kết Quả Tải Lên Tệp Tin (Media Upload Response DTO)
 * ==============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaUploadResponse {

    private Long id;
    private String tenantId;
    private String fileName;
    private String fileUrl;
    private String storageProvider;
    private Long fileSize;
    private String contentType;
    private Instant createdAt;
}

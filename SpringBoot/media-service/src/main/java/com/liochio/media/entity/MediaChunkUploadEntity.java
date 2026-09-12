package com.liochio.media.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

/**
 * ==============================================================================
 * Thực Thể Tiến Độ Tải Lên Từng Phần (Media Chunk Upload Entity - Bảng 23)
 * ==============================================================================
 */
@Entity
@Table(name = "media_chunk_uploads")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaChunkUploadEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "upload_id", length = 64, nullable = false)
    private String uploadId;

    @Column(name = "tenant_id", length = 50, nullable = false)
    private String tenantId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    @Column(name = "total_size", nullable = false)
    private Long totalSize;

    @Column(name = "total_chunks", nullable = false)
    private Integer totalChunks;

    @Column(name = "uploaded_chunks", nullable = false)
    @Builder.Default
    private Integer uploadedChunks = 0;

    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private String status = "UPLOADING"; // UPLOADING, MERGING, COMPLETED, FAILED

    @Column(name = "temp_storage_path", length = 500, nullable = false)
    private String tempStoragePath;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}

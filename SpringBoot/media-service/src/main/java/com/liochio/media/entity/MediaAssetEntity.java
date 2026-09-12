package com.liochio.media.entity;

import com.liochio.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * ==============================================================================
 * Thực Thể Tài Sản Đa Phương Tiện (Media Asset Entity - Bảng 22)
 * ==============================================================================
 */
@Entity
@Table(name = "media_assets", indexes = {
        @Index(name = "idx_media_lookup", columnList = "tenant_id, file_type, is_deleted"),
        @Index(name = "idx_media_hash", columnList = "tenant_id, file_hash")
})
@SQLDelete(sql = "UPDATE media_assets SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaAssetEntity extends BaseEntity {

    @Column(name = "uploaded_by")
    private Long uploadedBy;

    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    @Column(name = "storage_key", length = 500, nullable = false)
    private String storageKey;

    @Column(name = "cdn_url", length = 500, nullable = false)
    private String cdnUrl;

    @Column(name = "file_type", length = 30, nullable = false)
    private String fileType; // IMAGE, VIDEO, AUDIO, DOCUMENT, ARCHIVE, OTHER

    @Column(name = "mime_type", length = 100, nullable = false)
    private String mimeType;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @Column(name = "file_hash", length = 64)
    private String fileHash;

    @Column(name = "media_metadata", columnDefinition = "JSON")
    private String mediaMetadata;

    @Column(name = "storage_provider", length = 50, nullable = false)
    @Builder.Default
    private String storageProvider = "CLOUDFLARE_R2";

    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean isPublic = true;
}

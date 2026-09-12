package com.liochio.media.entity;

import com.liochio.common.entity.BaseEntity;
import com.liochio.common.enums.StorageProvider;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * ==============================================================================
 * Thực Thể Quản Lý Tệp Tin Đa Phương Tiện (Media File Entity)
 * ==============================================================================
 */
@Entity
@Table(name = "media_files")
@SQLDelete(sql = "UPDATE media_files SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaFileEntity extends BaseEntity {

    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    @Column(name = "file_url", length = 500, nullable = false)
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_provider", length = 50, nullable = false)
    @Builder.Default
    private StorageProvider storageProvider = StorageProvider.LOCAL;

    @Column(name = "file_size", nullable = false)
    @Builder.Default
    private Long fileSize = 0L;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "checksum", length = 100)
    private String checksum;
}

package com.liochio.media.repository;

import com.liochio.media.entity.MediaChunkUploadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MediaChunkUploadRepository extends JpaRepository<MediaChunkUploadEntity, String> {
    Optional<MediaChunkUploadEntity> findByUploadIdAndTenantId(String uploadId, String tenantId);
}

package com.liochio.media.repository;

import com.liochio.media.entity.MediaAssetEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaAssetRepository extends JpaRepository<MediaAssetEntity, Long> {
    Page<MediaAssetEntity> findByTenantIdAndFileType(String tenantId, String fileType, Pageable pageable);
    List<MediaAssetEntity> findByTenantId(String tenantId);
}

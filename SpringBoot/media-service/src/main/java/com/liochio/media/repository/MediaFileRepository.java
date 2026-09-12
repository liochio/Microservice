package com.liochio.media.repository;

import com.liochio.media.entity.MediaFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ==============================================================================
 * Repository Quản Lý Tệp Tin Media (Media File Repository)
 * ==============================================================================
 */
@Repository
public interface MediaFileRepository extends JpaRepository<MediaFileEntity, Long> {

    List<MediaFileEntity> findByTenantId(String tenantId);
}

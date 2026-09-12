package com.liochio.music.repository;

import com.liochio.music.entity.SongEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SongRepository extends JpaRepository<SongEntity, Long> {
    Page<SongEntity> findByTenantId(String tenantId, Pageable pageable);
    Optional<SongEntity> findByTenantIdAndSlug(String tenantId, String slug);
}

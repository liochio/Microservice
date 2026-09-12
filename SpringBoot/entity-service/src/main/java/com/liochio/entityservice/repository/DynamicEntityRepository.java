package com.liochio.entityservice.repository;

import com.liochio.entityservice.entity.DynamicEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ==============================================================================
 * Repository Quản Lý Thực Thể Động (Dynamic Entity Repository)
 * ==============================================================================
 */
@Repository
public interface DynamicEntityRepository extends JpaRepository<DynamicEntity, Long>, JpaSpecificationExecutor<DynamicEntity> {

    Optional<DynamicEntity> findByEntityTypeAndSlug(String entityType, String slug);

    Optional<DynamicEntity> findByTenantIdAndEntityTypeAndSlug(String tenantId, String entityType, String slug);
}

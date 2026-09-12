package com.liochio.entityservice.repository;

import com.liochio.entityservice.entity.DynamicEntityRevisionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DynamicEntityRevisionRepository extends JpaRepository<DynamicEntityRevisionEntity, Long> {
    List<DynamicEntityRevisionEntity> findByTenantIdAndEntityIdOrderByRevisionNumberDesc(String tenantId, Long entityId);
    Optional<DynamicEntityRevisionEntity> findByTenantIdAndEntityIdAndRevisionNumber(String tenantId, Long entityId, Integer revisionNumber);
    Optional<DynamicEntityRevisionEntity> findFirstByTenantIdAndEntityIdOrderByRevisionNumberDesc(String tenantId, Long entityId);
}
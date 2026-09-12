package com.liochio.auth.repository;

import com.liochio.auth.entity.TenantFeatureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantFeatureRepository extends JpaRepository<TenantFeatureEntity, Long> {
    List<TenantFeatureEntity> findByTenantIdAndIsEnabledTrue(String tenantId);
    Optional<TenantFeatureEntity> findByTenantIdAndFeatureCode(String tenantId, String featureCode);
}

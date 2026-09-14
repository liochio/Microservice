package com.liochio.entityservice.repository;

import com.liochio.entityservice.entity.TenantConfigOverrideEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantConfigOverrideRepository extends JpaRepository<TenantConfigOverrideEntity, Long> {

    List<TenantConfigOverrideEntity> findByTenantIdAndIsActiveTrue(String tenantId);

    Optional<TenantConfigOverrideEntity> findByTenantIdAndConfigKey(String tenantId, String configKey);
}

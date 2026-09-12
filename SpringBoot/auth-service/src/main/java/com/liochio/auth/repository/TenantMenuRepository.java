package com.liochio.auth.repository;

import com.liochio.auth.entity.TenantMenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantMenuRepository extends JpaRepository<TenantMenuEntity, Long> {
    List<TenantMenuEntity> findByTenantIdAndIsEnabledTrue(String tenantId);
    Optional<TenantMenuEntity> findByTenantIdAndMenuCode(String tenantId, String menuCode);
}

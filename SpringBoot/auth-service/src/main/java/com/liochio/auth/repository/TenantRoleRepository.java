package com.liochio.auth.repository;

import com.liochio.auth.entity.TenantRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantRoleRepository extends JpaRepository<TenantRoleEntity, Long> {
    List<TenantRoleEntity> findByTenantIdAndIsActiveTrue(String tenantId);
    Optional<TenantRoleEntity> findByTenantIdAndRoleCode(String tenantId, String roleCode);
}

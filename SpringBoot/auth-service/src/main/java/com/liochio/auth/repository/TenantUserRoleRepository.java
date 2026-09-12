package com.liochio.auth.repository;

import com.liochio.auth.entity.TenantUserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantUserRoleRepository extends JpaRepository<TenantUserRoleEntity, Long> {
    List<TenantUserRoleEntity> findByTenantIdAndUserId(String tenantId, Long userId);
    List<TenantUserRoleEntity> findByUserId(Long userId);
    Optional<TenantUserRoleEntity> findByTenantIdAndUserIdAndTenantRoleId(String tenantId, Long userId, Long tenantRoleId);
}

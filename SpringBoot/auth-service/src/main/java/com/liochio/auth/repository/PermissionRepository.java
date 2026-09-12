package com.liochio.auth.repository;

import com.liochio.auth.entity.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ==============================================================================
 * Repository Quản Lý Quyền Hạn (Permission Repository)
 * ==============================================================================
 */
@Repository
public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {

    Optional<PermissionEntity> findByPermissionCode(String permissionCode);

    boolean existsByPermissionCode(String permissionCode);
}

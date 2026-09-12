package com.liochio.auth.repository;

import com.liochio.auth.entity.MasterPermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasterPermissionRepository extends JpaRepository<MasterPermissionEntity, Long> {
    List<MasterPermissionEntity> findByMenuCode(String menuCode);
    Optional<MasterPermissionEntity> findByPermissionCode(String permissionCode);
}

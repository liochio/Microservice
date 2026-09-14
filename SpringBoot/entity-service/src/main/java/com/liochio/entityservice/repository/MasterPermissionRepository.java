package com.liochio.entityservice.repository;

import com.liochio.entityservice.entity.MasterPermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasterPermissionRepository extends JpaRepository<MasterPermissionEntity, Long> {

    List<MasterPermissionEntity> findByMenuCode(String menuCode);
}

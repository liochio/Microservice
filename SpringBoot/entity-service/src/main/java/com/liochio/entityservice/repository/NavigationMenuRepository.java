package com.liochio.entityservice.repository;

import com.liochio.entityservice.entity.NavigationMenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ==============================================================================
 * Repository Quản Lý Menu Điều Hướng (Navigation Menu Repository)
 * ==============================================================================
 */
@Repository
public interface NavigationMenuRepository extends JpaRepository<NavigationMenuEntity, Long> {

    Optional<NavigationMenuEntity> findByMenuCode(String menuCode);

    Optional<NavigationMenuEntity> findByTenantIdAndMenuCode(String tenantId, String menuCode);
}

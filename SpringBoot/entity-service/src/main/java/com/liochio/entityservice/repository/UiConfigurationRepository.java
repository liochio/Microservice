package com.liochio.entityservice.repository;

import com.liochio.entityservice.entity.UiConfigurationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ==============================================================================
 * Repository Quản Lý Cấu Hình Giao Diện UI (UI Configuration Repository)
 * ==============================================================================
 */
@Repository
public interface UiConfigurationRepository extends JpaRepository<UiConfigurationEntity, Long> {

    Optional<UiConfigurationEntity> findByPageCode(String pageCode);

    Optional<UiConfigurationEntity> findByTenantIdAndPageCode(String tenantId, String pageCode);
}

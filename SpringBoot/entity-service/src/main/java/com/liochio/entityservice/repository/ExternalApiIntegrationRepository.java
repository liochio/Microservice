package com.liochio.entityservice.repository;

import com.liochio.entityservice.entity.ExternalApiIntegrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExternalApiIntegrationRepository extends JpaRepository<ExternalApiIntegrationEntity, Long> {
    Optional<ExternalApiIntegrationEntity> findByTenantIdAndServiceCode(String tenantId, String serviceCode);
    List<ExternalApiIntegrationEntity> findByTenantIdAndIsActiveTrue(String tenantId);
}

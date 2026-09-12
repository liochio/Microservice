package com.liochio.auth.repository;

import com.liochio.auth.entity.CustomerOnboardingGateEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerOnboardingGateRepository extends JpaRepository<CustomerOnboardingGateEntity, Long> {
    Optional<CustomerOnboardingGateEntity> findByUserId(Long userId);
    Optional<CustomerOnboardingGateEntity> findByTenantIdAndUserId(String tenantId, Long userId);
    Page<CustomerOnboardingGateEntity> findByTenantIdOrderByCreatedAtDesc(String tenantId, Pageable pageable);
    Page<CustomerOnboardingGateEntity> findByTenantIdAndOverallStatusOrderByCreatedAtDesc(String tenantId, String overallStatus, Pageable pageable);
}

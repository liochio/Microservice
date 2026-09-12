package com.liochio.auth.repository;

import com.liochio.auth.entity.SecurityLoginHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityLoginHistoryRepository extends JpaRepository<SecurityLoginHistoryEntity, Long> {
    Page<SecurityLoginHistoryEntity> findByTenantIdAndUserId(String tenantId, Long userId, Pageable pageable);
}

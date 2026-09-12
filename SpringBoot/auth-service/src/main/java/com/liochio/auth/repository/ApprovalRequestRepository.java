package com.liochio.auth.repository;

import com.liochio.auth.entity.ApprovalRequestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequestEntity, Long> {
    Optional<ApprovalRequestEntity> findByRequestCode(String requestCode);
    Page<ApprovalRequestEntity> findByTenantIdOrderByCreatedAtDesc(String tenantId, Pageable pageable);
    Page<ApprovalRequestEntity> findByTenantIdAndStatusOrderByCreatedAtDesc(String tenantId, String status, Pageable pageable);
    List<ApprovalRequestEntity> findByTenantIdAndStatus(String tenantId, String status);
}

package com.liochio.common.repository;

import com.liochio.common.entity.AuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {
    Page<AuditLogEntity> findByTenantId(String tenantId, Pageable pageable);
    List<AuditLogEntity> findByTraceId(String traceId);
}

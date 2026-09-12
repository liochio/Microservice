package com.liochio.entityservice.repository;

import com.liochio.entityservice.entity.FormDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ==============================================================================
 * Repository Quản Lý Định Nghĩa Biểu Mẫu (Form Definition Repository)
 * ==============================================================================
 */
@Repository
public interface FormDefinitionRepository extends JpaRepository<FormDefinitionEntity, Long> {

    Optional<FormDefinitionEntity> findByFormCode(String formCode);

    Optional<FormDefinitionEntity> findByTenantIdAndFormCode(String tenantId, String formCode);
}

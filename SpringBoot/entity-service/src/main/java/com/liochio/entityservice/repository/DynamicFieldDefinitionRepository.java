package com.liochio.entityservice.repository;

import com.liochio.entityservice.entity.DynamicFieldDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DynamicFieldDefinitionRepository extends JpaRepository<DynamicFieldDefinitionEntity, Long> {
    List<DynamicFieldDefinitionEntity> findByEntityTypeCodeOrderByDisplayOrderAsc(String entityTypeCode);
}

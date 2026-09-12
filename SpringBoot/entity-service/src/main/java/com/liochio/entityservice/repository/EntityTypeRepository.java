package com.liochio.entityservice.repository;

import com.liochio.entityservice.entity.EntityTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntityTypeRepository extends JpaRepository<EntityTypeEntity, String> {
    List<EntityTypeEntity> findByIsActiveTrue();
}

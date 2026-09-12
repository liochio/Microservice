package com.liochio.auth.repository;

import com.liochio.auth.entity.SystemFeatureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemFeatureRepository extends JpaRepository<SystemFeatureEntity, String> {
    List<SystemFeatureEntity> findByIsActiveTrue();
}

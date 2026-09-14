package com.liochio.entityservice.repository;

import com.liochio.entityservice.entity.MasterMenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasterMenuRepository extends JpaRepository<MasterMenuEntity, String> {

    List<MasterMenuEntity> findByIsActiveOrderBySortOrderAsc(Boolean isActive);

    List<MasterMenuEntity> findByPortalTypeAndIsActiveOrderBySortOrderAsc(String portalType, Boolean isActive);

    Optional<MasterMenuEntity> findByCode(String code);
}

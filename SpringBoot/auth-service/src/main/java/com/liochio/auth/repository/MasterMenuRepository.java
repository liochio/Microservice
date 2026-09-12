package com.liochio.auth.repository;

import com.liochio.auth.entity.MasterMenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasterMenuRepository extends JpaRepository<MasterMenuEntity, String> {
    List<MasterMenuEntity> findByPortalTypeAndIsActiveOrderBySortOrderAsc(String portalType, Boolean isActive);
    List<MasterMenuEntity> findByParentCodeAndIsActiveOrderBySortOrderAsc(String parentCode, Boolean isActive);
    List<MasterMenuEntity> findByIsActiveOrderBySortOrderAsc(Boolean isActive);
}

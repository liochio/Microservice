package com.liochio.tour.repository;

import com.liochio.tour.entity.TourEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TourRepository extends JpaRepository<TourEntity, Long> {
    Page<TourEntity> findByTenantIdAndStatus(String tenantId, String status, Pageable pageable);
    Optional<TourEntity> findByTenantIdAndSlug(String tenantId, String slug);
}

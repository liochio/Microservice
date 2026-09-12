package com.liochio.film.repository;

import com.liochio.film.entity.MovieEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<MovieEntity, Long> {
    Page<MovieEntity> findByTenantId(String tenantId, Pageable pageable);
    Optional<MovieEntity> findByTenantIdAndSlug(String tenantId, String slug);
}

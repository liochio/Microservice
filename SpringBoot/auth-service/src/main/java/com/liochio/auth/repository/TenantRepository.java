package com.liochio.auth.repository;

import com.liochio.auth.entity.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<TenantEntity, String> {
    Optional<TenantEntity> findBySubdomain(String subdomain);
    Optional<TenantEntity> findByDomain(String domain);
}

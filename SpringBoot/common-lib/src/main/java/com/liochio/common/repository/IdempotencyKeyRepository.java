package com.liochio.common.repository;

import com.liochio.common.entity.IdempotencyKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKeyEntity, String> {
    Optional<IdempotencyKeyEntity> findByIdempotencyKeyAndTenantId(String idempotencyKey, String tenantId);
    void deleteByExpiresAtBefore(Instant now);
}

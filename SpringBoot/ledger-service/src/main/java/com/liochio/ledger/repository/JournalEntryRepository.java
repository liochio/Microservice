package com.liochio.ledger.repository;

import com.liochio.ledger.entity.JournalEntryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntryEntity, Long> {

    Optional<JournalEntryEntity> findByTenantIdAndIdempotencyKey(String tenantId, String idempotencyKey);

    Optional<JournalEntryEntity> findByTenantIdAndEntryNo(String tenantId, String entryNo);

    Optional<JournalEntryEntity> findTopByTenantIdOrderByIdDesc(String tenantId);

    Page<JournalEntryEntity> findAllByTenantIdOrderByPostedAtDesc(String tenantId, Pageable pageable);

    @Query("SELECT j FROM JournalEntryEntity j JOIN j.details d WHERE j.tenantId = :tenantId AND d.account.userId = :userId ORDER BY j.postedAt DESC")
    Page<JournalEntryEntity> findAllByTenantIdAndUserId(@Param("tenantId") String tenantId, @Param("userId") Long userId, Pageable pageable);
}

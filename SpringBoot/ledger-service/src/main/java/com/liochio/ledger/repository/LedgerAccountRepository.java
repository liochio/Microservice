package com.liochio.ledger.repository;

import com.liochio.ledger.entity.LedgerAccountEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LedgerAccountRepository extends JpaRepository<LedgerAccountEntity, Long> {

    Optional<LedgerAccountEntity> findByTenantIdAndUserIdAndAccountType(String tenantId, Long userId, String accountType);

    Optional<LedgerAccountEntity> findByTenantIdAndAccountNumber(String tenantId, String accountNumber);

    Optional<LedgerAccountEntity> findByAccountNumber(String accountNumber);

    List<LedgerAccountEntity> findAllByTenantIdAndUserId(String tenantId, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM LedgerAccountEntity a WHERE a.id = :id")
    Optional<LedgerAccountEntity> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM LedgerAccountEntity a WHERE a.tenantId = :tenantId AND ((:userId IS NULL AND a.userId IS NULL) OR a.userId = :userId) AND a.accountType = :accountType")
    Optional<LedgerAccountEntity> findByTenantIdAndUserIdAndAccountTypeForUpdate(
            @Param("tenantId") String tenantId,
            @Param("userId") Long userId,
            @Param("accountType") String accountType
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM LedgerAccountEntity a WHERE a.tenantId = :tenantId AND a.accountNumber = :accountNumber")
    Optional<LedgerAccountEntity> findByTenantIdAndAccountNumberForUpdate(
            @Param("tenantId") String tenantId,
            @Param("accountNumber") String accountNumber
    );
}

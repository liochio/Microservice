package com.liochio.auth.repository;

import com.liochio.auth.entity.QrLoginSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QrLoginSessionRepository extends JpaRepository<QrLoginSessionEntity, String> {

    Optional<QrLoginSessionEntity> findByIdAndTenantId(String id, String tenantId);

    Optional<QrLoginSessionEntity> findByExchangeAuthCodeAndTenantId(String exchangeAuthCode, String tenantId);

    Optional<QrLoginSessionEntity> findByExchangeAuthCode(String exchangeAuthCode);
}

package com.liochio.payment.repository;

import com.liochio.payment.entity.TenantPaymentConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantPaymentConfigRepository extends JpaRepository<TenantPaymentConfigEntity, Long> {
    Optional<TenantPaymentConfigEntity> findByTenantIdAndGatewayName(String tenantId, String gatewayName);
    List<TenantPaymentConfigEntity> findByTenantIdAndIsActiveTrue(String tenantId);
}

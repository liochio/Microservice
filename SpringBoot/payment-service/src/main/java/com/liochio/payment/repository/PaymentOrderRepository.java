package com.liochio.payment.repository;

import com.liochio.payment.entity.PaymentOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ==============================================================================
 * Repository Quản Lý Giao Dịch Thanh Toán (Payment Order Repository)
 * ==============================================================================
 */
@Repository
public interface PaymentOrderRepository extends JpaRepository<PaymentOrderEntity, Long> {

    Optional<PaymentOrderEntity> findByOrderId(String orderId);

    List<PaymentOrderEntity> findByTenantId(String tenantId);
}

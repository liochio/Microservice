package com.liochio.payment.repository;

import com.liochio.payment.entity.PaymentTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransactionEntity, Long> {
    Optional<PaymentTransactionEntity> findByTenantIdAndTransactionCode(String tenantId, String transactionCode);
    List<PaymentTransactionEntity> findByBookingId(Long bookingId);
}

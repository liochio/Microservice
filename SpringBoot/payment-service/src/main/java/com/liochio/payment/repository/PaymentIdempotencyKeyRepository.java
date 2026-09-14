package com.liochio.payment.repository;

import com.liochio.payment.entity.IdempotencyKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("paymentIdempotencyKeyRepository")
public interface PaymentIdempotencyKeyRepository extends JpaRepository<IdempotencyKeyEntity, Long> {

    Optional<IdempotencyKeyEntity> findByIdempotencyKey(String idempotencyKey);
}
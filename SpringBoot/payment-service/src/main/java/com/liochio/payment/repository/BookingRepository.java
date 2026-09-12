package com.liochio.payment.repository;

import com.liochio.payment.entity.BookingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, Long> {
    Optional<BookingEntity> findByTenantIdAndBookingCode(String tenantId, String bookingCode);
    Page<BookingEntity> findByTenantIdAndCustomerId(String tenantId, Long customerId, Pageable pageable);
}

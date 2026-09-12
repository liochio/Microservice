package com.liochio.otp.repository;

import com.liochio.otp.entity.OtpVerificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerificationEntity, Long> {

    Optional<OtpVerificationEntity> findTopByTenantIdAndUserIdAndOtpPurposeAndStatusOrderByCreatedAtDesc(
            String tenantId, Long userId, String otpPurpose, String status
    );

    Optional<OtpVerificationEntity> findTopByReferenceId(String referenceId);

    long countByTargetDestinationAndCreatedAtAfter(String targetDestination, Instant after);

    long countByUserIdAndCreatedAtAfter(Long userId, Instant after);

    Optional<OtpVerificationEntity> findTopByTargetDestinationOrderByCreatedAtDesc(String targetDestination);

    Optional<OtpVerificationEntity> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}
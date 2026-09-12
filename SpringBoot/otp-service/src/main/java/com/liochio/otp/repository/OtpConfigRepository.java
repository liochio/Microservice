package com.liochio.otp.repository;

import com.liochio.otp.entity.OtpConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpConfigRepository extends JpaRepository<OtpConfigEntity, Long> {
    Optional<OtpConfigEntity> findByTenantId(String tenantId);
}

package com.liochio.auth.repository;

import com.liochio.auth.entity.UserDeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDeviceEntity, Long> {
    Optional<UserDeviceEntity> findByTenantIdAndUserIdAndDeviceId(String tenantId, Long userId, String deviceId);
    List<UserDeviceEntity> findByTenantIdAndUserId(String tenantId, Long userId);
}

package com.liochio.notification.repository;

import com.liochio.notification.entity.TenantNotificationConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantNotificationConfigRepository extends JpaRepository<TenantNotificationConfigEntity, Long> {
    Optional<TenantNotificationConfigEntity> findByTenantIdAndChannel(String tenantId, String channel);
}

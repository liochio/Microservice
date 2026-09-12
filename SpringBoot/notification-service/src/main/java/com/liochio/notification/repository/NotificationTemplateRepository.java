package com.liochio.notification.repository;

import com.liochio.notification.entity.NotificationTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplateEntity, Long> {
    Optional<NotificationTemplateEntity> findByTenantIdAndTemplateCodeAndChannelAndLocale(
            String tenantId, String templateCode, String channel, String locale);
}

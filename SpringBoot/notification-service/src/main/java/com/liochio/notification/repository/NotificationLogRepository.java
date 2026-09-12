package com.liochio.notification.repository;

import com.liochio.notification.entity.NotificationLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * ==============================================================================
 * Repository Nhật Ký Thông Báo (Notification Log Repository)
 * ==============================================================================
 */
@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLogEntity, Long> {

    List<NotificationLogEntity> findByTenantId(String tenantId);

    Optional<NotificationLogEntity> findByIdempotencyKey(String idempotencyKey);

    List<NotificationLogEntity> findByTenantIdAndRecipientOrderByCreatedAtDesc(String tenantId, String recipient);

    List<NotificationLogEntity> findByRecipientOrderByCreatedAtDesc(String recipient);

    List<NotificationLogEntity> findByRecipientAndIsReadFalse(String recipient);

    @Modifying
    @Query("UPDATE NotificationLogEntity n SET n.isRead = true, n.readAt = :readAt WHERE n.recipient = :recipient AND n.isRead = false")
    int markAllAsRead(@Param("recipient") String recipient, @Param("readAt") Instant readAt);
}

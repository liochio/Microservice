package com.liochio.worker.repository;

import com.liochio.worker.entity.NotificationRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRuleRepository extends JpaRepository<NotificationRuleEntity, Long> {
    List<NotificationRuleEntity> findByActionTriggerAndIsEnabledTrue(String actionTrigger);
}

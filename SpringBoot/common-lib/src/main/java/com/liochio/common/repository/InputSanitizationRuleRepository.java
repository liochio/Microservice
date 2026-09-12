package com.liochio.common.repository;

import com.liochio.common.entity.InputSanitizationRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InputSanitizationRuleRepository extends JpaRepository<InputSanitizationRuleEntity, Long> {
    Optional<InputSanitizationRuleEntity> findByRuleKey(String ruleKey);
    List<InputSanitizationRuleEntity> findByIsActiveTrue();
}

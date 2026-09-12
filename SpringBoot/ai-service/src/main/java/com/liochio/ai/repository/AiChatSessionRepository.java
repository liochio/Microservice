package com.liochio.ai.repository;

import com.liochio.ai.entity.AiChatMessageEntity;
import com.liochio.ai.entity.AiChatSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiChatSessionRepository extends JpaRepository<AiChatSessionEntity, Long> {
    Optional<AiChatSessionEntity> findByTenantIdAndSessionToken(String tenantId, String sessionToken);
}

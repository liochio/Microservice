package com.liochio.worker.repository;

import com.liochio.worker.entity.MessageTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessageTemplateRepository extends JpaRepository<MessageTemplateEntity, Long> {
    Optional<MessageTemplateEntity> findByTemplateCodeAndLanguageCodeAndIsActiveTrue(String templateCode, String languageCode);
    Optional<MessageTemplateEntity> findByTemplateCodeAndIsActiveTrue(String templateCode);
}

package com.liochio.entityservice.repository;

import com.liochio.entityservice.entity.I18nDictionaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface I18nDictionaryRepository extends JpaRepository<I18nDictionaryEntity, Long> {
    List<I18nDictionaryEntity> findByTenantIdAndLocale(String tenantId, String locale);
    Optional<I18nDictionaryEntity> findByTenantIdAndLocaleAndTextKey(String tenantId, String locale, String textKey);
}

package com.liochio.entityservice.repository;

import com.liochio.entityservice.entity.WebTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebTemplateRepository extends JpaRepository<WebTemplateEntity, String> {
}

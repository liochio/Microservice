package com.liochio.entityservice.repository;

import com.liochio.entityservice.entity.GlobalSystemConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GlobalSystemConfigRepository extends JpaRepository<GlobalSystemConfigEntity, String> {

    List<GlobalSystemConfigEntity> findByCategory(String category);
}

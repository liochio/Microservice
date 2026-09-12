package com.liochio.auth.repository;

import com.liochio.auth.entity.GlobalSystemConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GlobalSystemConfigRepository extends JpaRepository<GlobalSystemConfigEntity, String> {
    List<GlobalSystemConfigEntity> findByCategory(String category);
}

package com.liochio.common.repository;

import com.liochio.common.entity.SystemErrorCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemErrorCodeRepository extends JpaRepository<SystemErrorCodeEntity, Integer> {
    Optional<SystemErrorCodeEntity> findByErrorKey(String errorKey);
}

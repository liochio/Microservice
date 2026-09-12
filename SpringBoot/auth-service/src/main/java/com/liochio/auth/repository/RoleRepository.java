package com.liochio.auth.repository;

import com.liochio.auth.entity.RoleEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ==============================================================================
 * Repository Quản Lý Vai Trò (Role Repository)
 * ==============================================================================
 */
@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    @EntityGraph(attributePaths = {"permissions"})
    Optional<RoleEntity> findByRoleName(String roleName);

    boolean existsByRoleName(String roleName);
}

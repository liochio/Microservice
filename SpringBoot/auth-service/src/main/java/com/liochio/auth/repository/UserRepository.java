package com.liochio.auth.repository;

import com.liochio.auth.entity.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ==============================================================================
 * Repository Quản Lý Người Dùng (User Repository)
 * ==============================================================================
 * 
 * Mục đích:
 * - Truy vấn thông tin người dùng kèm theo Roles và Permissions bằng '@EntityGraph'
 *   để triệt tiêu lỗi N+1 Query theo nguyên tắc Tối ưu hóa hiệu năng.
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<UserEntity> findByUsername(String username);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<UserEntity> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    @Query("SELECT u FROM UserEntity u WHERE u.username = :username AND u.tenantId = :tenantId")
    Optional<UserEntity> findByUsernameAndTenantId(@Param("username") String username, @Param("tenantId") String tenantId);
}

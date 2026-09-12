package com.liochio.auth.repository;

import com.liochio.auth.entity.UserSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSessionEntity, String> {

    List<UserSessionEntity> findByTenantIdAndUserIdAndIsRevokedFalse(String tenantId, Long userId);

    List<UserSessionEntity> findByUserIdAndIsRevokedFalse(Long userId);

    List<UserSessionEntity> findByTenantIdAndUserId(String tenantId, Long userId);

    Optional<UserSessionEntity> findByIdAndIsRevokedFalse(String id);

    Optional<UserSessionEntity> findByRefreshTokenHash(String refreshTokenHash);

    Optional<UserSessionEntity> findFirstByRefreshTokenHashOrderByCreatedAtDesc(String refreshTokenHash);

    Optional<UserSessionEntity> findByRefreshTokenHashAndIsRevokedFalse(String refreshTokenHash);

    @Modifying
    @Query("UPDATE UserSessionEntity s SET s.isRevoked = true, s.revokedReason = :reason WHERE s.userId = :userId AND s.isRevoked = false")
    int revokeAllByUserId(@Param("userId") Long userId, @Param("reason") String reason);

    @Modifying
    @Query("UPDATE UserSessionEntity s SET s.isRevoked = true, s.revokedReason = :reason WHERE s.tenantId = :tenantId AND s.userId = :userId AND s.isRevoked = false")
    int revokeAllByTenantIdAndUserId(@Param("tenantId") String tenantId, @Param("userId") Long userId, @Param("reason") String reason);
}

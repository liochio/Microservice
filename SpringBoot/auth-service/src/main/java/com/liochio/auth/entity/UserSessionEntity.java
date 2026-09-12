package com.liochio.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

/**
 * ==============================================================================
 * Thực Thể Phiên Đăng Nhập & Refresh Token (User Session Entity - Bảng 33)
 * ==============================================================================
 */
@Entity
@Table(name = "user_sessions", indexes = {
        @Index(name = "idx_session_user", columnList = "tenant_id, user_id, is_revoked"),
        @Index(name = "idx_session_expiry", columnList = "expires_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSessionEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Column(name = "tenant_id", length = 50, nullable = false)
    private String tenantId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "device_id", length = 150, nullable = false)
    private String deviceId;

    @Column(name = "refresh_token_hash", length = 255, nullable = false)
    private String refreshTokenHash;

    @Column(name = "ip_address", length = 50, nullable = false)
    private String ipAddress;

    @Column(name = "user_agent", length = 500, nullable = false)
    private String userAgent;

    @Column(name = "is_revoked", nullable = false)
    @Builder.Default
    private Boolean isRevoked = false;

    @Column(name = "revoked_reason", length = 100)
    private String revokedReason;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "last_accessed_at", nullable = false)
    @Builder.Default
    private Instant lastAccessedAt = Instant.now();

    public static UserSessionEntityBuilder builder() { return new UserSessionEntityBuilder(); }
    public static class UserSessionEntityBuilder {
        private String id;
        private String tenantId;
        private Long userId;
        private String deviceId;
        private String refreshTokenHash;
        private String ipAddress;
        private String userAgent;
        private Boolean isRevoked = false;
        private String revokedReason;
        private Instant expiresAt;
        private Instant createdAt = Instant.now();
        private Instant lastAccessedAt = Instant.now();

        public UserSessionEntityBuilder id(String id) { this.id = id; return this; }
        public UserSessionEntityBuilder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
        public UserSessionEntityBuilder userId(Long userId) { this.userId = userId; return this; }
        public UserSessionEntityBuilder deviceId(String deviceId) { this.deviceId = deviceId; return this; }
        public UserSessionEntityBuilder refreshTokenHash(String refreshTokenHash) { this.refreshTokenHash = refreshTokenHash; return this; }
        public UserSessionEntityBuilder ipAddress(String ipAddress) { this.ipAddress = ipAddress; return this; }
        public UserSessionEntityBuilder userAgent(String userAgent) { this.userAgent = userAgent; return this; }
        public UserSessionEntityBuilder isRevoked(Boolean isRevoked) { this.isRevoked = isRevoked; return this; }
        public UserSessionEntityBuilder revokedReason(String revokedReason) { this.revokedReason = revokedReason; return this; }
        public UserSessionEntityBuilder expiresAt(Instant expiresAt) { this.expiresAt = expiresAt; return this; }
        public UserSessionEntityBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public UserSessionEntityBuilder lastAccessedAt(Instant lastAccessedAt) { this.lastAccessedAt = lastAccessedAt; return this; }
        public UserSessionEntity build() {
            UserSessionEntity e = new UserSessionEntity();
            e.setId(id);
            e.setTenantId(tenantId);
            e.setUserId(userId);
            e.setDeviceId(deviceId);
            e.setRefreshTokenHash(refreshTokenHash);
            e.setIpAddress(ipAddress);
            e.setUserAgent(userAgent);
            e.setIsRevoked(isRevoked);
            e.setRevokedReason(revokedReason);
            e.setExpiresAt(expiresAt);
            e.setCreatedAt(createdAt);
            e.setLastAccessedAt(lastAccessedAt);
            return e;
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getRefreshTokenHash() { return refreshTokenHash; }
    public void setRefreshTokenHash(String refreshTokenHash) { this.refreshTokenHash = refreshTokenHash; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public Boolean getIsRevoked() { return isRevoked; }
    public void setIsRevoked(Boolean isRevoked) { this.isRevoked = isRevoked; }
    public String getRevokedReason() { return revokedReason; }
    public void setRevokedReason(String revokedReason) { this.revokedReason = revokedReason; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getLastAccessedAt() { return lastAccessedAt; }
    public void setLastAccessedAt(Instant lastAccessedAt) { this.lastAccessedAt = lastAccessedAt; }
}

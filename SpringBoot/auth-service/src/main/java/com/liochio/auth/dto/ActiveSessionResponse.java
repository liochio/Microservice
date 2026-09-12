package com.liochio.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveSessionResponse {

    private String id;
    private String deviceId;
    private String ipAddress;
    private String userAgent;
    private Boolean isCurrentSession;
    private Boolean isRevoked;
    private String revokedReason;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant lastAccessedAt;

    public static ActiveSessionResponseBuilder builder() { return new ActiveSessionResponseBuilder(); }
    public static class ActiveSessionResponseBuilder {
        private String id;
        private String deviceId;
        private String ipAddress;
        private String userAgent;
        private Boolean isCurrentSession;
        private Boolean isRevoked;
        private String revokedReason;
        private Instant expiresAt;
        private Instant createdAt;
        private Instant lastAccessedAt;

        public ActiveSessionResponseBuilder id(String id) { this.id = id; return this; }
        public ActiveSessionResponseBuilder deviceId(String deviceId) { this.deviceId = deviceId; return this; }
        public ActiveSessionResponseBuilder ipAddress(String ipAddress) { this.ipAddress = ipAddress; return this; }
        public ActiveSessionResponseBuilder userAgent(String userAgent) { this.userAgent = userAgent; return this; }
        public ActiveSessionResponseBuilder isCurrentSession(Boolean isCurrentSession) { this.isCurrentSession = isCurrentSession; return this; }
        public ActiveSessionResponseBuilder isRevoked(Boolean isRevoked) { this.isRevoked = isRevoked; return this; }
        public ActiveSessionResponseBuilder revokedReason(String revokedReason) { this.revokedReason = revokedReason; return this; }
        public ActiveSessionResponseBuilder expiresAt(Instant expiresAt) { this.expiresAt = expiresAt; return this; }
        public ActiveSessionResponseBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public ActiveSessionResponseBuilder lastAccessedAt(Instant lastAccessedAt) { this.lastAccessedAt = lastAccessedAt; return this; }
        public ActiveSessionResponse build() {
            ActiveSessionResponse r = new ActiveSessionResponse();
            r.setId(id);
            r.setDeviceId(deviceId);
            r.setIpAddress(ipAddress);
            r.setUserAgent(userAgent);
            r.setIsCurrentSession(isCurrentSession);
            r.setIsRevoked(isRevoked);
            r.setRevokedReason(revokedReason);
            r.setExpiresAt(expiresAt);
            r.setCreatedAt(createdAt);
            r.setLastAccessedAt(lastAccessedAt);
            return r;
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public Boolean getIsCurrentSession() { return isCurrentSession; }
    public void setIsCurrentSession(Boolean isCurrentSession) { this.isCurrentSession = isCurrentSession; }
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

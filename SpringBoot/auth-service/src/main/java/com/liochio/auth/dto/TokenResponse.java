package com.liochio.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

/**
 * ==============================================================================
 * DTO Trả Về Cặp Mã Xác Thực JWT (Token Response DTO)
 * ==============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private Long userId;
    private String username;
    private String tenantId;
    private Set<String> roles;
    private Set<String> permissions;
    private Boolean requires2Fa;
    private String challengeToken;
    private Boolean deviceTrusted;
    private String deviceId;
    private String sessionId;
    private Instant issuedAt;
    private String fullName;
    private String coreAccountRef;

    public static TokenResponseBuilder builder() { return new TokenResponseBuilder(); }
    public static class TokenResponseBuilder {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private long expiresIn;
        private Long userId;
        private String username;
        private String tenantId;
        private Set<String> roles;
        private Set<String> permissions;
        private Boolean requires2Fa;
        private String challengeToken;
        private Boolean deviceTrusted;
        private String deviceId;
        private String sessionId;
        private Instant issuedAt;
        private String fullName;
        private String coreAccountRef;

        public TokenResponseBuilder accessToken(String accessToken) { this.accessToken = accessToken; return this; }
        public TokenResponseBuilder refreshToken(String refreshToken) { this.refreshToken = refreshToken; return this; }
        public TokenResponseBuilder tokenType(String tokenType) { this.tokenType = tokenType; return this; }
        public TokenResponseBuilder expiresIn(long expiresIn) { this.expiresIn = expiresIn; return this; }
        public TokenResponseBuilder userId(Long userId) { this.userId = userId; return this; }
        public TokenResponseBuilder username(String username) { this.username = username; return this; }
        public TokenResponseBuilder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
        public TokenResponseBuilder roles(Set<String> roles) { this.roles = roles; return this; }
        public TokenResponseBuilder permissions(Set<String> permissions) { this.permissions = permissions; return this; }
        public TokenResponseBuilder requires2Fa(Boolean requires2Fa) { this.requires2Fa = requires2Fa; return this; }
        public TokenResponseBuilder challengeToken(String challengeToken) { this.challengeToken = challengeToken; return this; }
        public TokenResponseBuilder deviceTrusted(Boolean deviceTrusted) { this.deviceTrusted = deviceTrusted; return this; }
        public TokenResponseBuilder deviceId(String deviceId) { this.deviceId = deviceId; return this; }
        public TokenResponseBuilder sessionId(String sessionId) { this.sessionId = sessionId; return this; }
        public TokenResponseBuilder issuedAt(Instant issuedAt) { this.issuedAt = issuedAt; return this; }
        public TokenResponseBuilder fullName(String fullName) { this.fullName = fullName; return this; }
        public TokenResponseBuilder coreAccountRef(String coreAccountRef) { this.coreAccountRef = coreAccountRef; return this; }
        public TokenResponse build() {
            TokenResponse r = new TokenResponse();
            r.setAccessToken(accessToken);
            r.setRefreshToken(refreshToken);
            r.setTokenType(tokenType);
            r.setExpiresIn(expiresIn);
            r.setUserId(userId);
            r.setUsername(username);
            r.setTenantId(tenantId);
            r.setRoles(roles);
            r.setPermissions(permissions);
            r.setRequires2Fa(requires2Fa);
            r.setChallengeToken(challengeToken);
            r.setDeviceTrusted(deviceTrusted);
            r.setDeviceId(deviceId);
            r.setSessionId(sessionId);
            r.setIssuedAt(issuedAt);
            r.setFullName(fullName);
            r.setCoreAccountRef(coreAccountRef);
            return r;
        }
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
    public Set<String> getPermissions() { return permissions; }
    public void setPermissions(Set<String> permissions) { this.permissions = permissions; }
    public Boolean getRequires2Fa() { return requires2Fa; }
    public void setRequires2Fa(Boolean requires2Fa) { this.requires2Fa = requires2Fa; }
    public String getChallengeToken() { return challengeToken; }
    public void setChallengeToken(String challengeToken) { this.challengeToken = challengeToken; }
    public Boolean getDeviceTrusted() { return deviceTrusted; }
    public void setDeviceTrusted(Boolean deviceTrusted) { this.deviceTrusted = deviceTrusted; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public Instant getIssuedAt() { return issuedAt; }
    public void setIssuedAt(Instant issuedAt) { this.issuedAt = issuedAt; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getCoreAccountRef() { return coreAccountRef; }
    public void setCoreAccountRef(String coreAccountRef) { this.coreAccountRef = coreAccountRef; }
}

package com.liochio.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

/**
 * ==============================================================================
 * Thực Thể Theo Vết Đăng Nhập & Phân Tích Rủi Ro (Login History - Bảng 34)
 * ==============================================================================
 */
@Entity
@Table(name = "security_login_histories", indexes = {
        @Index(name = "idx_login_history_ip", columnList = "ip_address, created_at"),
        @Index(name = "idx_login_history_user", columnList = "tenant_id, user_id, created_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityLoginHistoryEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", length = 50, nullable = false)
    private String tenantId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "attempted_username", length = 100, nullable = false)
    private String attemptedUsername;

    @Column(name = "ip_address", length = 50, nullable = false)
    private String ipAddress;

    @Column(name = "user_agent", length = 500, nullable = false)
    private String userAgent;

    @Column(name = "country_code", length = 10)
    private String countryCode;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "isp", length = 100)
    private String isp;

    @Column(name = "device_fingerprint", length = 150)
    private String deviceFingerprint;

    @Column(name = "login_status", length = 50, nullable = false)
    private String loginStatus; // SUCCESS, WRONG_PASSWORD, LOCKED, UNTRUSTED_DEVICE_CHALLENGE, INVALID_2FA

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "risk_score")
    @Builder.Default
    private Integer riskScore = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    public static SecurityLoginHistoryEntityBuilder builder() { return new SecurityLoginHistoryEntityBuilder(); }
    public static class SecurityLoginHistoryEntityBuilder {
        private Long id;
        private String tenantId;
        private Long userId;
        private String attemptedUsername;
        private String ipAddress;
        private String userAgent;
        private String countryCode;
        private String city;
        private String isp;
        private String deviceFingerprint;
        private String loginStatus;
        private String failureReason;
        private Integer riskScore = 0;
        private Instant createdAt = Instant.now();

        public SecurityLoginHistoryEntityBuilder id(Long id) { this.id = id; return this; }
        public SecurityLoginHistoryEntityBuilder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
        public SecurityLoginHistoryEntityBuilder userId(Long userId) { this.userId = userId; return this; }
        public SecurityLoginHistoryEntityBuilder attemptedUsername(String attemptedUsername) { this.attemptedUsername = attemptedUsername; return this; }
        public SecurityLoginHistoryEntityBuilder ipAddress(String ipAddress) { this.ipAddress = ipAddress; return this; }
        public SecurityLoginHistoryEntityBuilder userAgent(String userAgent) { this.userAgent = userAgent; return this; }
        public SecurityLoginHistoryEntityBuilder countryCode(String countryCode) { this.countryCode = countryCode; return this; }
        public SecurityLoginHistoryEntityBuilder city(String city) { this.city = city; return this; }
        public SecurityLoginHistoryEntityBuilder isp(String isp) { this.isp = isp; return this; }
        public SecurityLoginHistoryEntityBuilder deviceFingerprint(String deviceFingerprint) { this.deviceFingerprint = deviceFingerprint; return this; }
        public SecurityLoginHistoryEntityBuilder loginStatus(String loginStatus) { this.loginStatus = loginStatus; return this; }
        public SecurityLoginHistoryEntityBuilder failureReason(String failureReason) { this.failureReason = failureReason; return this; }
        public SecurityLoginHistoryEntityBuilder riskScore(Integer riskScore) { this.riskScore = riskScore; return this; }
        public SecurityLoginHistoryEntityBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public SecurityLoginHistoryEntity build() {
            SecurityLoginHistoryEntity e = new SecurityLoginHistoryEntity();
            e.setId(id);
            e.setTenantId(tenantId);
            e.setUserId(userId);
            e.setAttemptedUsername(attemptedUsername);
            e.setIpAddress(ipAddress);
            e.setUserAgent(userAgent);
            e.setCountryCode(countryCode);
            e.setCity(city);
            e.setIsp(isp);
            e.setDeviceFingerprint(deviceFingerprint);
            e.setLoginStatus(loginStatus);
            e.setFailureReason(failureReason);
            e.setRiskScore(riskScore);
            e.setCreatedAt(createdAt);
            return e;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getAttemptedUsername() { return attemptedUsername; }
    public void setAttemptedUsername(String attemptedUsername) { this.attemptedUsername = attemptedUsername; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getIsp() { return isp; }
    public void setIsp(String isp) { this.isp = isp; }
    public String getDeviceFingerprint() { return deviceFingerprint; }
    public void setDeviceFingerprint(String deviceFingerprint) { this.deviceFingerprint = deviceFingerprint; }
    public String getLoginStatus() { return loginStatus; }
    public void setLoginStatus(String loginStatus) { this.loginStatus = loginStatus; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public Integer getRiskScore() { return riskScore != null ? riskScore : 0; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

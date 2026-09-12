package com.liochio.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

/**
 * ==============================================================================
 * Thực Thể Thiết Bị Tin Cậy (User Device Entity - Bảng 32)
 * ==============================================================================
 */
@Entity
@Table(name = "user_devices", indexes = {
        @Index(name = "uk_user_device", columnList = "tenant_id, user_id, device_id", unique = true),
        @Index(name = "idx_device_status", columnList = "user_id, status")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDeviceEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", length = 50, nullable = false)
    private String tenantId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "device_id", length = 150, nullable = false)
    private String deviceId;

    @Column(name = "device_name", length = 150, nullable = false)
    private String deviceName;

    @Column(name = "platform", length = 30, nullable = false)
    private String platform; // WEB, IOS, ANDROID, MACOS, WINDOWS, LINUX

    @Column(name = "os_version", length = 50)
    private String osVersion;

    @Column(name = "app_version", length = 50)
    private String appVersion;

    @Column(name = "browser_name", length = 50)
    private String browserName;

    @Column(name = "is_trusted", nullable = false)
    @Builder.Default
    private Boolean isTrusted = false;

    @Column(name = "is_smart_otp_enrolled", nullable = false)
    @Builder.Default
    private Boolean isSmartOtpEnrolled = false;

    @Column(name = "fcm_push_token", length = 500)
    private String fcmPushToken;

    @Column(name = "last_ip_address", length = 50)
    private String lastIpAddress;

    @Column(name = "last_location", length = 150)
    private String lastLocation;

    @Column(name = "last_active_at", nullable = false)
    @Builder.Default
    private Instant lastActiveAt = Instant.now();

    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, BLOCKED, REVOKED

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    public static UserDeviceEntityBuilder builder() { return new UserDeviceEntityBuilder(); }
    public static class UserDeviceEntityBuilder {
        private Long id;
        private String tenantId;
        private Long userId;
        private String deviceId;
        private String deviceName;
        private String platform;
        private String osVersion;
        private String appVersion;
        private String browserName;
        private Boolean isTrusted = false;
        private Boolean isSmartOtpEnrolled = false;
        private String fcmPushToken;
        private String lastIpAddress;
        private String lastLocation;
        private Instant lastActiveAt = Instant.now();
        private String status = "ACTIVE";
        private Instant createdAt = Instant.now();

        public UserDeviceEntityBuilder id(Long id) { this.id = id; return this; }
        public UserDeviceEntityBuilder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
        public UserDeviceEntityBuilder userId(Long userId) { this.userId = userId; return this; }
        public UserDeviceEntityBuilder deviceId(String deviceId) { this.deviceId = deviceId; return this; }
        public UserDeviceEntityBuilder deviceName(String deviceName) { this.deviceName = deviceName; return this; }
        public UserDeviceEntityBuilder platform(String platform) { this.platform = platform; return this; }
        public UserDeviceEntityBuilder osVersion(String osVersion) { this.osVersion = osVersion; return this; }
        public UserDeviceEntityBuilder appVersion(String appVersion) { this.appVersion = appVersion; return this; }
        public UserDeviceEntityBuilder browserName(String browserName) { this.browserName = browserName; return this; }
        public UserDeviceEntityBuilder isTrusted(Boolean isTrusted) { this.isTrusted = isTrusted; return this; }
        public UserDeviceEntityBuilder isSmartOtpEnrolled(Boolean isSmartOtpEnrolled) { this.isSmartOtpEnrolled = isSmartOtpEnrolled; return this; }
        public UserDeviceEntityBuilder fcmPushToken(String fcmPushToken) { this.fcmPushToken = fcmPushToken; return this; }
        public UserDeviceEntityBuilder lastIpAddress(String lastIpAddress) { this.lastIpAddress = lastIpAddress; return this; }
        public UserDeviceEntityBuilder lastLocation(String lastLocation) { this.lastLocation = lastLocation; return this; }
        public UserDeviceEntityBuilder lastActiveAt(Instant lastActiveAt) { this.lastActiveAt = lastActiveAt; return this; }
        public UserDeviceEntityBuilder status(String status) { this.status = status; return this; }
        public UserDeviceEntityBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public UserDeviceEntity build() {
            UserDeviceEntity e = new UserDeviceEntity();
            e.setId(id);
            e.setTenantId(tenantId);
            e.setUserId(userId);
            e.setDeviceId(deviceId);
            e.setDeviceName(deviceName);
            e.setPlatform(platform);
            e.setOsVersion(osVersion);
            e.setAppVersion(appVersion);
            e.setBrowserName(browserName);
            if (isTrusted != null) e.setIsTrusted(isTrusted);
            if (isSmartOtpEnrolled != null) e.setIsSmartOtpEnrolled(isSmartOtpEnrolled);
            e.setFcmPushToken(fcmPushToken);
            e.setLastIpAddress(lastIpAddress);
            e.setLastLocation(lastLocation);
            if (lastActiveAt != null) e.setLastActiveAt(lastActiveAt);
            if (status != null) e.setStatus(status);
            if (createdAt != null) e.setCreatedAt(createdAt);
            return e;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getOsVersion() { return osVersion; }
    public void setOsVersion(String osVersion) { this.osVersion = osVersion; }
    public String getAppVersion() { return appVersion; }
    public void setAppVersion(String appVersion) { this.appVersion = appVersion; }
    public String getBrowserName() { return browserName; }
    public void setBrowserName(String browserName) { this.browserName = browserName; }
    public Boolean getIsTrusted() { return isTrusted; }
    public void setIsTrusted(Boolean isTrusted) { this.isTrusted = isTrusted; }
    public Boolean getIsSmartOtpEnrolled() { return isSmartOtpEnrolled; }
    public void setIsSmartOtpEnrolled(Boolean isSmartOtpEnrolled) { this.isSmartOtpEnrolled = isSmartOtpEnrolled; }
    public String getFcmPushToken() { return fcmPushToken; }
    public void setFcmPushToken(String fcmPushToken) { this.fcmPushToken = fcmPushToken; }
    public String getLastIpAddress() { return lastIpAddress; }
    public void setLastIpAddress(String lastIpAddress) { this.lastIpAddress = lastIpAddress; }
    public String getLastLocation() { return lastLocation; }
    public void setLastLocation(String lastLocation) { this.lastLocation = lastLocation; }
    public Instant getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(Instant lastActiveAt) { this.lastActiveAt = lastActiveAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

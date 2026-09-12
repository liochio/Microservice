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
public class DeviceResponse {

    private Long id;
    private String deviceId;
    private String deviceName;
    private String platform;
    private String osVersion;
    private String appVersion;
    private String browserName;
    private Boolean isTrusted;
    private Boolean isSmartOtpEnrolled;
    private String lastIpAddress;
    private String lastLocation;
    private Instant lastActiveAt;
    private String status;
    private Instant createdAt;

    public static DeviceResponseBuilder builder() { return new DeviceResponseBuilder(); }
    public static class DeviceResponseBuilder {
        private Long id;
        private String deviceId;
        private String deviceName;
        private String platform;
        private String osVersion;
        private String appVersion;
        private String browserName;
        private Boolean isTrusted;
        private Boolean isSmartOtpEnrolled;
        private String lastIpAddress;
        private String lastLocation;
        private Instant lastActiveAt;
        private String status;
        private Instant createdAt;

        public DeviceResponseBuilder id(Long id) { this.id = id; return this; }
        public DeviceResponseBuilder deviceId(String deviceId) { this.deviceId = deviceId; return this; }
        public DeviceResponseBuilder deviceName(String deviceName) { this.deviceName = deviceName; return this; }
        public DeviceResponseBuilder platform(String platform) { this.platform = platform; return this; }
        public DeviceResponseBuilder osVersion(String osVersion) { this.osVersion = osVersion; return this; }
        public DeviceResponseBuilder appVersion(String appVersion) { this.appVersion = appVersion; return this; }
        public DeviceResponseBuilder browserName(String browserName) { this.browserName = browserName; return this; }
        public DeviceResponseBuilder isTrusted(Boolean isTrusted) { this.isTrusted = isTrusted; return this; }
        public DeviceResponseBuilder isSmartOtpEnrolled(Boolean isSmartOtpEnrolled) { this.isSmartOtpEnrolled = isSmartOtpEnrolled; return this; }
        public DeviceResponseBuilder lastIpAddress(String lastIpAddress) { this.lastIpAddress = lastIpAddress; return this; }
        public DeviceResponseBuilder lastLocation(String lastLocation) { this.lastLocation = lastLocation; return this; }
        public DeviceResponseBuilder lastActiveAt(Instant lastActiveAt) { this.lastActiveAt = lastActiveAt; return this; }
        public DeviceResponseBuilder status(String status) { this.status = status; return this; }
        public DeviceResponseBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public DeviceResponse build() {
            DeviceResponse r = new DeviceResponse();
            r.setId(id);
            r.setDeviceId(deviceId);
            r.setDeviceName(deviceName);
            r.setPlatform(platform);
            r.setOsVersion(osVersion);
            r.setAppVersion(appVersion);
            r.setBrowserName(browserName);
            r.setIsTrusted(isTrusted);
            r.setIsSmartOtpEnrolled(isSmartOtpEnrolled);
            r.setLastIpAddress(lastIpAddress);
            r.setLastLocation(lastLocation);
            r.setLastActiveAt(lastActiveAt);
            r.setStatus(status);
            r.setCreatedAt(createdAt);
            return r;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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

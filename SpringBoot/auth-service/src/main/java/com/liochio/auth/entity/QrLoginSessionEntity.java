package com.liochio.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

/**
 * ==============================================================================
 * Thực Thể Phiên Đăng Nhập Quét Mã QR (QR Login Session Entity - Bảng 36)
 * ==============================================================================
 * 
 * Mục đích:
 * - Quản lý chuỗi trạng thái đăng nhập không mật khẩu (Passwordless QR Login).
 * - Theo dõi các trạng thái: PENDING -> SCANNED -> CONFIRMED -> EXPIRED / REJECTED.
 * - Cấp mã 'exchange_auth_code' dùng 1 lần (TTL 10s) cho trình duyệt web đổi Token.
 */
@Entity
@Table(name = "qr_login_sessions", indexes = {
        @Index(name = "idx_qr_tenant_status", columnList = "tenant_id, status"),
        @Index(name = "idx_qr_expiry", columnList = "expires_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QrLoginSessionEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id; // QR Session UUID

    @Column(name = "tenant_id", length = 50, nullable = false)
    @Builder.Default
    private String tenantId = "SYSTEM";

    @Column(name = "web_device_id", length = 150, nullable = false)
    private String webDeviceId;

    @Column(name = "web_ip_address", length = 50, nullable = false)
    private String webIpAddress;

    @Column(name = "web_user_agent", length = 500, nullable = false)
    private String webUserAgent;

    @Column(name = "mobile_user_id")
    private Long mobileUserId;

    @Column(name = "mobile_device_id", length = 150)
    private String mobileDeviceId;

    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private String status = "PENDING"; // PENDING, SCANNED, CONFIRMED, EXPIRED, REJECTED

    @Column(name = "exchange_auth_code", length = 255)
    private String exchangeAuthCode;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    public static QrLoginSessionEntityBuilder builder() { return new QrLoginSessionEntityBuilder(); }
    public static class QrLoginSessionEntityBuilder {
        private String id;
        private String tenantId = "SYSTEM";
        private String webDeviceId;
        private String webIpAddress;
        private String webUserAgent;
        private Long mobileUserId;
        private String mobileDeviceId;
        private String status = "PENDING";
        private String exchangeAuthCode;
        private Instant expiresAt;
        private Instant createdAt = Instant.now();
        private Instant updatedAt = Instant.now();

        public QrLoginSessionEntityBuilder id(String id) { this.id = id; return this; }
        public QrLoginSessionEntityBuilder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
        public QrLoginSessionEntityBuilder webDeviceId(String webDeviceId) { this.webDeviceId = webDeviceId; return this; }
        public QrLoginSessionEntityBuilder webIpAddress(String webIpAddress) { this.webIpAddress = webIpAddress; return this; }
        public QrLoginSessionEntityBuilder webUserAgent(String webUserAgent) { this.webUserAgent = webUserAgent; return this; }
        public QrLoginSessionEntityBuilder mobileUserId(Long mobileUserId) { this.mobileUserId = mobileUserId; return this; }
        public QrLoginSessionEntityBuilder mobileDeviceId(String mobileDeviceId) { this.mobileDeviceId = mobileDeviceId; return this; }
        public QrLoginSessionEntityBuilder status(String status) { this.status = status; return this; }
        public QrLoginSessionEntityBuilder exchangeAuthCode(String exchangeAuthCode) { this.exchangeAuthCode = exchangeAuthCode; return this; }
        public QrLoginSessionEntityBuilder expiresAt(Instant expiresAt) { this.expiresAt = expiresAt; return this; }
        public QrLoginSessionEntityBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public QrLoginSessionEntityBuilder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }
        public QrLoginSessionEntity build() {
            QrLoginSessionEntity e = new QrLoginSessionEntity();
            e.setId(id);
            e.setTenantId(tenantId);
            e.setWebDeviceId(webDeviceId);
            e.setWebIpAddress(webIpAddress);
            e.setWebUserAgent(webUserAgent);
            e.setMobileUserId(mobileUserId);
            e.setMobileDeviceId(mobileDeviceId);
            e.setStatus(status);
            e.setExchangeAuthCode(exchangeAuthCode);
            e.setExpiresAt(expiresAt);
            e.setCreatedAt(createdAt);
            e.setUpdatedAt(updatedAt);
            return e;
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getWebDeviceId() { return webDeviceId; }
    public void setWebDeviceId(String webDeviceId) { this.webDeviceId = webDeviceId; }
    public String getWebIpAddress() { return webIpAddress; }
    public void setWebIpAddress(String webIpAddress) { this.webIpAddress = webIpAddress; }
    public String getWebUserAgent() { return webUserAgent; }
    public void setWebUserAgent(String webUserAgent) { this.webUserAgent = webUserAgent; }
    public Long getMobileUserId() { return mobileUserId; }
    public void setMobileUserId(Long mobileUserId) { this.mobileUserId = mobileUserId; }
    public String getMobileDeviceId() { return mobileDeviceId; }
    public void setMobileDeviceId(String mobileDeviceId) { this.mobileDeviceId = mobileDeviceId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getExchangeAuthCode() { return exchangeAuthCode; }
    public void setExchangeAuthCode(String exchangeAuthCode) { this.exchangeAuthCode = exchangeAuthCode; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

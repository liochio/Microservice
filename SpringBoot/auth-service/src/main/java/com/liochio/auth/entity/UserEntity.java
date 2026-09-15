package com.liochio.auth.entity;

import com.liochio.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.HashSet;
import java.util.Set;

/**
 * ==============================================================================
 * Thực Thể Người Dùng (User Entity)
 * ==============================================================================
 * 
 * Mục đích:
 * - Đại diện cho tài khoản người dùng trong hệ thống Multi-Tenancy.
 * - Liên kết quan hệ N-N với RoleEntity qua bảng trung gian 'user_roles'.
 * - Tự động xóa mềm (is_deleted = true) và cô lập đa khách thuê theo 'tenant_id'.
 */
@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity extends BaseEntity {

    @Column(name = "username", length = 100, nullable = false)
    private String username;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Column(name = "email", length = 150, nullable = false)
    private String email;

    @Column(name = "full_name", length = 150)
    private String fullName;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "user_type", length = 30, nullable = false)
    @Builder.Default
    private String userType = "CUSTOMER"; // SUPER_ADMIN, TENANT_OWNER, CORP_ADMIN, SUB_ACCOUNT, CUSTOMER

    @Column(name = "phone", length = 20)
    @com.liochio.common.annotation.MaskPII(type = com.liochio.common.annotation.MaskPII.MaskType.PHONE)
    private String phone;

    @Column(name = "ekyc_level", length = 20, nullable = false)
    @Builder.Default
    private String ekycLevel = "TIER_1"; // TIER_1 (5M/day), TIER_2 (500M/day), TIER_3 (UNLIMITED)

    @Column(name = "ekyc_status", length = 30, nullable = false)
    @Builder.Default
    private String ekycStatus = "UNVERIFIED"; // UNVERIFIED, PENDING_REVIEW, VERIFIED, REJECTED

    @Column(name = "id_card_number", length = 50)
    @com.liochio.common.annotation.MaskPII(type = com.liochio.common.annotation.MaskPII.MaskType.ID_CARD)
    private String idCardNumber;

    @Column(name = "id_card_type", length = 30)
    @Builder.Default
    private String idCardType = "CCCD"; // CCCD, PASSPORT, CMND

    @Column(name = "ekyc_verified_at")
    private java.time.Instant ekycVerifiedAt;

    @Column(name = "daily_transfer_limit", precision = 18, scale = 2, nullable = false)
    @Builder.Default
    private java.math.BigDecimal dailyTransferLimit = new java.math.BigDecimal("5000000.00");

    @Column(name = "auth_provider", length = 30)
    @Builder.Default
    private String authProvider = "LOCAL"; // LOCAL, GOOGLE, FACEBOOK, GITHUB

    @Column(name = "provider_id", length = 100)
    private String providerId;

    @Column(name = "is_email_verified", nullable = false)
    @Builder.Default
    private Boolean isEmailVerified = false;

    @Column(name = "is_phone_verified", nullable = false)
    @Builder.Default
    private Boolean isPhoneVerified = false;

    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "lockout_until")
    private java.time.Instant lockoutUntil;

    @Column(name = "password_changed_at")
    private java.time.Instant passwordChangedAt;

    @Column(name = "reset_password_token", length = 255)
    private String resetPasswordToken;

    @Column(name = "reset_password_token_expiry")
    private java.time.Instant resetPasswordTokenExpiry;

    @Column(name = "preferred_locale", length = 10, nullable = false)
    @Builder.Default
    private String preferredLocale = "vi";

    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<RoleEntity> roles = new HashSet<>();

    public static UserEntityBuilder builder() { return new UserEntityBuilder(); }
    public static class UserEntityBuilder {
        private String tenantId;
        private String username;
        private String password;
        private String email;
        private String fullName;
        private String avatarUrl;
        private String status = "ACTIVE";
        private String userType = "CUSTOMER";
        private String phone;
        private String authProvider = "LOCAL";
        private String providerId;
        private Boolean isEmailVerified = false;
        private Boolean isPhoneVerified = false;
        private Integer failedLoginAttempts = 0;
        private java.time.Instant lockoutUntil;
        private java.time.Instant passwordChangedAt;
        private String resetPasswordToken;
        private java.time.Instant resetPasswordTokenExpiry;
        private String preferredLocale = "vi";
        private String metadata;
        private Set<RoleEntity> roles = new HashSet<>();

        public UserEntityBuilder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
        public UserEntityBuilder username(String username) { this.username = username; return this; }
        public UserEntityBuilder password(String password) { this.password = password; return this; }
        public UserEntityBuilder email(String email) { this.email = email; return this; }
        public UserEntityBuilder fullName(String fullName) { this.fullName = fullName; return this; }
        public UserEntityBuilder avatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; return this; }
        public UserEntityBuilder status(String status) { this.status = status; return this; }
        public UserEntityBuilder userType(String userType) { this.userType = userType; return this; }
        public UserEntityBuilder phone(String phone) { this.phone = phone; return this; }
        public UserEntityBuilder authProvider(String authProvider) { this.authProvider = authProvider; return this; }
        public UserEntityBuilder providerId(String providerId) { this.providerId = providerId; return this; }
        public UserEntityBuilder isEmailVerified(Boolean isEmailVerified) { this.isEmailVerified = isEmailVerified; return this; }
        public UserEntityBuilder isPhoneVerified(Boolean isPhoneVerified) { this.isPhoneVerified = isPhoneVerified; return this; }
        public UserEntityBuilder failedLoginAttempts(Integer failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; return this; }
        public UserEntityBuilder lockoutUntil(java.time.Instant lockoutUntil) { this.lockoutUntil = lockoutUntil; return this; }
        public UserEntityBuilder passwordChangedAt(java.time.Instant passwordChangedAt) { this.passwordChangedAt = passwordChangedAt; return this; }
        public UserEntityBuilder resetPasswordToken(String resetPasswordToken) { this.resetPasswordToken = resetPasswordToken; return this; }
        public UserEntityBuilder resetPasswordTokenExpiry(java.time.Instant resetPasswordTokenExpiry) { this.resetPasswordTokenExpiry = resetPasswordTokenExpiry; return this; }
        public UserEntityBuilder preferredLocale(String preferredLocale) { this.preferredLocale = preferredLocale; return this; }
        public UserEntityBuilder metadata(String metadata) { this.metadata = metadata; return this; }
        public UserEntityBuilder roles(Set<RoleEntity> roles) { this.roles = roles; return this; }
        public UserEntity build() {
            UserEntity u = new UserEntity();
            if (tenantId != null) u.setTenantId(tenantId);
            u.setUsername(username);
            u.setPassword(password);
            u.setEmail(email);
            u.setFullName(fullName);
            u.setAvatarUrl(avatarUrl);
            if (status != null) u.setStatus(status);
            if (userType != null) u.setUserType(userType);
            u.setPhone(phone);
            if (authProvider != null) u.setAuthProvider(authProvider);
            u.setProviderId(providerId);
            if (isEmailVerified != null) u.setIsEmailVerified(isEmailVerified);
            if (isPhoneVerified != null) u.setIsPhoneVerified(isPhoneVerified);
            if (failedLoginAttempts != null) u.setFailedLoginAttempts(failedLoginAttempts);
            u.setLockoutUntil(lockoutUntil);
            u.setPasswordChangedAt(passwordChangedAt);
            u.setResetPasswordToken(resetPasswordToken);
            u.setResetPasswordTokenExpiry(resetPasswordTokenExpiry);
            if (preferredLocale != null) u.setPreferredLocale(preferredLocale);
            u.setMetadata(metadata);
            if (roles != null) u.setRoles(roles);
            return u;
        }
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAuthProvider() { return authProvider; }
    public void setAuthProvider(String authProvider) { this.authProvider = authProvider; }
    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }
    public Boolean getIsEmailVerified() { return isEmailVerified; }
    public void setIsEmailVerified(Boolean isEmailVerified) { this.isEmailVerified = isEmailVerified; }
    public Boolean getIsPhoneVerified() { return isPhoneVerified; }
    public void setIsPhoneVerified(Boolean isPhoneVerified) { this.isPhoneVerified = isPhoneVerified; }
    public Integer getFailedLoginAttempts() { return failedLoginAttempts != null ? failedLoginAttempts : 0; }
    public void setFailedLoginAttempts(Integer failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }
    public java.time.Instant getLockoutUntil() { return lockoutUntil; }
    public void setLockoutUntil(java.time.Instant lockoutUntil) { this.lockoutUntil = lockoutUntil; }
    public java.time.Instant getPasswordChangedAt() { return passwordChangedAt; }
    public void setPasswordChangedAt(java.time.Instant passwordChangedAt) { this.passwordChangedAt = passwordChangedAt; }
    public String getResetPasswordToken() { return resetPasswordToken; }
    public void setResetPasswordToken(String resetPasswordToken) { this.resetPasswordToken = resetPasswordToken; }
    public java.time.Instant getResetPasswordTokenExpiry() { return resetPasswordTokenExpiry; }
    public void setResetPasswordTokenExpiry(java.time.Instant resetPasswordTokenExpiry) { this.resetPasswordTokenExpiry = resetPasswordTokenExpiry; }
    public String getPreferredLocale() { return preferredLocale; }
    public void setPreferredLocale(String preferredLocale) { this.preferredLocale = preferredLocale; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public Set<RoleEntity> getRoles() { return roles != null ? roles : new HashSet<>(); }
    public void setRoles(Set<RoleEntity> roles) { this.roles = roles; }
}

package com.liochio.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

/**
 * ==============================================================================
 * DTO Trả Về Thông Tin Người Dùng (User Response DTO)
 * ==============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String tenantId;
    private String username;
    private String email;
    private String fullName;
    private String avatarUrl;
    private String status;
    private String userType;

    @com.liochio.common.annotation.MaskPII(type = com.liochio.common.annotation.MaskPII.MaskType.PHONE)
    private String phone;

    @com.liochio.common.annotation.MaskPII(type = com.liochio.common.annotation.MaskPII.MaskType.ID_CARD)
    private String idCardNumber;

    private String ekycLevel;
    private String ekycStatus;

    private Set<String> roles;
    private Set<String> permissions;
    private Instant createdAt;
    private Instant updatedAt;

    public static UserResponseBuilder builder() { return new UserResponseBuilder(); }
    public static class UserResponseBuilder {
        private Long id;
        private String tenantId;
        private String username;
        private String email;
        private String fullName;
        private String avatarUrl;
        private String status;
        private Set<String> roles;
        private Set<String> permissions;
        private Instant createdAt;
        private Instant updatedAt;

        public UserResponseBuilder id(Long id) { this.id = id; return this; }
        public UserResponseBuilder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
        public UserResponseBuilder username(String username) { this.username = username; return this; }
        public UserResponseBuilder email(String email) { this.email = email; return this; }
        public UserResponseBuilder fullName(String fullName) { this.fullName = fullName; return this; }
        public UserResponseBuilder avatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; return this; }
        public UserResponseBuilder status(String status) { this.status = status; return this; }
        public UserResponseBuilder userType(String userType) { this.userType = userType; return this; }
        public UserResponseBuilder phone(String phone) { this.phone = phone; return this; }
        public UserResponseBuilder idCardNumber(String idCardNumber) { this.idCardNumber = idCardNumber; return this; }
        public UserResponseBuilder ekycLevel(String ekycLevel) { this.ekycLevel = ekycLevel; return this; }
        public UserResponseBuilder ekycStatus(String ekycStatus) { this.ekycStatus = ekycStatus; return this; }
        public UserResponseBuilder roles(Set<String> roles) { this.roles = roles; return this; }
        public UserResponseBuilder permissions(Set<String> permissions) { this.permissions = permissions; return this; }
        public UserResponseBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public UserResponseBuilder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }
        public UserResponse build() {
            UserResponse r = new UserResponse();
            r.setId(id);
            r.setTenantId(tenantId);
            r.setUsername(username);
            r.setEmail(email);
            r.setFullName(fullName);
            r.setAvatarUrl(avatarUrl);
            r.setStatus(status);
            r.setUserType(userType);
            r.setPhone(phone);
            r.setIdCardNumber(idCardNumber);
            r.setEkycLevel(ekycLevel);
            r.setEkycStatus(ekycStatus);
            r.setRoles(roles);
            r.setPermissions(permissions);
            r.setCreatedAt(createdAt);
            r.setUpdatedAt(updatedAt);
            return r;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
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
    public String getIdCardNumber() { return idCardNumber; }
    public void setIdCardNumber(String idCardNumber) { this.idCardNumber = idCardNumber; }
    public String getEkycLevel() { return ekycLevel; }
    public void setEkycLevel(String ekycLevel) { this.ekycLevel = ekycLevel; }
    public String getEkycStatus() { return ekycStatus; }
    public void setEkycStatus(String ekycStatus) { this.ekycStatus = ekycStatus; }
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
    public Set<String> getPermissions() { return permissions; }
    public void setPermissions(Set<String> permissions) { this.permissions = permissions; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

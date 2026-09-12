package com.liochio.auth.dto;

import com.liochio.common.annotation.DynamicSanitize;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ==============================================================================
 * DTO Yêu Cầu Đăng Nhập (Login Request DTO)
 * ==============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @DynamicSanitize(ruleKey = "STRIP_SPECIAL")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    private String portalType; // SUPERADMIN, CORP, CONSUMER, RETAIL

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPortalType() { return portalType; }
    public void setPortalType(String portalType) { this.portalType = portalType; }
}

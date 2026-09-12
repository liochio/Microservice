package com.liochio.common.utils;

import com.liochio.common.constant.HeaderConstants;
import com.liochio.common.constant.SecurityConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * ==============================================================================
 * Tiện Ích Bảo Mật & Mã Hóa (Security Utility)
 * ==============================================================================
 * 
 * Mục đích:
 * - Cung cấp hàm băm mật khẩu chuẩn BCrypt.
 * - Trích xuất Token Bearer từ HttpServletRequest.
 * 
 * Khi nào gọi:
 * - Được Auth Service, Filter và Interceptors sử dụng.
 */
public final class SecurityUtils {

    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder(12);

    private SecurityUtils() {
        // Chống khởi tạo instance cho Utility Class
    }

    public static PasswordEncoder getPasswordEncoder() {
        return PASSWORD_ENCODER;
    }

    public static String encodePassword(String rawPassword) {
        return PASSWORD_ENCODER.encode(rawPassword);
    }

    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        return PASSWORD_ENCODER.matches(rawPassword, encodedPassword);
    }

    public static String extractBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HeaderConstants.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith(SecurityConstants.BEARER_PREFIX)) {
            return authHeader.substring(SecurityConstants.BEARER_PREFIX.length()).trim();
        }
        return null;
    }
}

package com.liochio.common.security;

import com.liochio.common.dto.ClientContextRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * ==============================================================================
 * Trình Bóc Tách Dấu Vân Tay Thiết Bị & Ngữ Cảnh Client (Device Fingerprint Extractor)
 * ==============================================================================
 * 
 * Mục đích:
 * - Trích xuất địa chỉ IP thực, User-Agent, Device ID, Platform, OS từ HTTP Request.
 * - Nhận diện thiết bị mới/lạ phục vụ quy trình Thách thức bảo mật 2FA (Step-up Auth).
 */
@Slf4j
@Component
public class DeviceFingerprintExtractor {

    private static final String HEADER_DEVICE_ID = "X-Device-Id";
    private static final String HEADER_DEVICE_NAME = "X-Device-Name";
    private static final String HEADER_APP_VERSION = "X-App-Version";

    /**
     * Bóc tách toàn diện thông tin Client từ Request
     */
    public ClientContextRequest extractContext(HttpServletRequest request) {
        String clientIp = extractClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) userAgent = "Unknown";

        String deviceId = request.getHeader(HEADER_DEVICE_ID);
        if (deviceId == null || deviceId.isBlank()) {
            deviceId = generateFingerprintHash(clientIp, userAgent);
        }

        String platform = detectPlatform(userAgent, request);
        String browserName = detectBrowser(userAgent);
        String osVersion = detectOs(userAgent);
        String deviceName = request.getHeader(HEADER_DEVICE_NAME);
        if (deviceName == null || deviceName.isBlank()) {
            deviceName = browserName + " on " + osVersion;
        }

        String appVersion = request.getHeader(HEADER_APP_VERSION);
        if (appVersion == null) appVersion = "1.0.0";

        return ClientContextRequest.builder()
                .deviceId(deviceId)
                .deviceName(deviceName)
                .platform(platform)
                .osVersion(osVersion)
                .appVersion(appVersion)
                .browserName(browserName)
                .clientIp(clientIp)
                .userAgent(userAgent)
                .build();
    }

    public String extractClientIp(HttpServletRequest request) {
        // 1. Kiểm tra Cloudflare Real IP Header
        String ip = request.getHeader("CF-Connecting-IP");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "127.0.0.1";
    }

    private String detectPlatform(String ua, HttpServletRequest request) {
        String customPlatform = request.getHeader("X-Platform");
        if (customPlatform != null && !customPlatform.isBlank()) {
            return customPlatform.toUpperCase();
        }
        String uaLower = ua.toLowerCase();
        if (uaLower.contains("postman")) return "POSTMAN";
        if (uaLower.contains("iphone") || uaLower.contains("ipad") || uaLower.contains("ios")) return "IOS";
        if (uaLower.contains("android")) return "ANDROID";
        if (uaLower.contains("windows")) return "WINDOWS";
        if (uaLower.contains("macintosh") || uaLower.contains("mac os")) return "MACOS";
        if (uaLower.contains("linux")) return "LINUX";
        return "WEB";
    }

    private String detectBrowser(String ua) {
        String uaLower = ua.toLowerCase();
        if (uaLower.contains("postman")) return "Postman";
        if (uaLower.contains("edg/")) return "Edge";
        if (uaLower.contains("chrome") && !uaLower.contains("edg/")) return "Chrome";
        if (uaLower.contains("safari") && !uaLower.contains("chrome")) return "Safari";
        if (uaLower.contains("firefox")) return "Firefox";
        return "Browser";
    }

    private String detectOs(String ua) {
        String uaLower = ua.toLowerCase();
        if (uaLower.contains("windows nt 10.0")) return "Windows 11/10";
        if (uaLower.contains("windows")) return "Windows";
        if (uaLower.contains("mac os x")) return "macOS";
        if (uaLower.contains("android")) return "Android";
        if (uaLower.contains("iphone") || uaLower.contains("ipad")) return "iOS";
        if (uaLower.contains("linux")) return "Linux";
        return "Unknown OS";
    }

    private String generateFingerprintHash(String ip, String userAgent) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest((ip + "|" + userAgent).getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder("fp_");
            for (int i = 0; i < 8; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return "fp_default_" + Math.abs((ip + userAgent).hashCode());
        }
    }
}

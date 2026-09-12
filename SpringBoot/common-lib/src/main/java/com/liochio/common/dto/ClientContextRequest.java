package com.liochio.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ==============================================================================
 * DTO Ngữ cảnh Yêu cầu từ Phía Client (Client Context Request)
 * ==============================================================================
 * 
 * Mục đích:
 * - Thu thập toàn bộ ngữ cảnh môi trường của Client (Tenant ID, User ID, Ngôn ngữ, IP, User-Agent)
 *   từ HTTP request để truyền xuyên suốt các layer và ghi vết Audit Log.
 * 
 * Khi nào gọi:
 * - Được TenantFilter / RequestLoggingFilter khởi tạo tại đầu vào của mỗi HTTP Request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientContextRequest {

    /**
     * Mã định danh khách thuê (Multi-Tenancy)
     */
    private String tenantId;

    /**
     * Mã định danh người dùng đã xác thực (nếu có)
     */
    private Long userId;

    /**
     * Tên tài khoản người dùng
     */
    private String username;

    /**
     * Mã ngôn ngữ yêu cầu (vi, en, zh)
     */
    private String language;

    /**
     * Địa chỉ IP thực của Client
     */
    private String clientIp;

    /**
     * Thông tin User-Agent (Trình duyệt, Hệ điều hành)
     */
    private String userAgent;

    /**
     * Mã định danh duy nhất của Request (X-Request-ID / TraceId)
     */
    private String requestId;

    /**
     * Dấu vân tay thiết bị (Fingerprint Hash)
     */
    private String deviceId;

    /**
     * Tên thiết bị (ví dụ: Chrome on Windows 11, iPhone 15 Pro)
     */
    private String deviceName;

    /**
     * Nền tảng (WEB, IOS, ANDROID, WINDOWS, MACOS, LINUX)
     */
    private String platform;

    /**
     * Phiên bản hệ điều hành
     */
    private String osVersion;

    /**
     * Phiên bản ứng dụng Client
     */
    private String appVersion;

    /**
     * Tên trình duyệt
     */
    private String browserName;
}
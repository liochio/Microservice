package com.liochio.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

/**
 * ==============================================================================
 * Thực Thể Nhật Ký Kiểm Toán Toàn Diện (Audit Log Entity - Enterprise Standard)
 * ==============================================================================
 * 
 * Mục đích:
 * - Ghi vết 100% Request/Response và sự kiện nghiệp vụ bất đồng bộ (@Async).
 * - Lưu trữ client_ip, user_agent, device_id, platform, trace_id, old_data, new_data.
 */
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_tenant_time", columnList = "tenant_id, created_at"),
        @Index(name = "idx_audit_user", columnList = "tenant_id, user_id, action_type"),
        @Index(name = "idx_audit_module", columnList = "tenant_id, module, status"),
        @Index(name = "idx_audit_trace", columnList = "trace_id"),
        @Index(name = "idx_audit_ip", columnList = "client_ip")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trace_id", length = 64, nullable = false)
    private String traceId;

    @Column(name = "tenant_id", length = 50, nullable = false)
    private String tenantId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "user_email", length = 150)
    private String userEmail;

    @Column(name = "user_role", length = 50)
    private String userRole;

    @Column(name = "client_ip", length = 50, nullable = false)
    private String clientIp;

    @Column(name = "platform", length = 30, nullable = false)
    @Builder.Default
    private String platform = "WEB";

    @Column(name = "device_id", length = 150)
    private String deviceId;

    @Column(name = "device_name", length = 150)
    private String deviceName;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "module", length = 50, nullable = false)
    @Builder.Default
    private String module = "AUTH";

    @Column(name = "action_type", length = 50, nullable = false)
    @Builder.Default
    private String actionType = "VIEW";

    @Column(name = "action_description", length = 255, nullable = false)
    @Builder.Default
    private String actionDescription = "";

    @Column(name = "http_method", length = 10, nullable = false)
    @Builder.Default
    private String httpMethod = "GET";

    @Column(name = "request_uri", length = 500, nullable = false)
    private String requestUri;

    @Column(name = "request_params", columnDefinition = "JSON")
    private String requestParams;

    @Column(name = "request_body", columnDefinition = "JSON")
    private String requestBody;

    @Column(name = "old_data", columnDefinition = "JSON")
    private String oldData;

    @Column(name = "new_data", columnDefinition = "JSON")
    private String newData;

    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private String status = "SUCCESS";

    @Column(name = "http_status_code", nullable = false)
    @Builder.Default
    private Integer httpStatusCode = 200;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "execution_time_ms", nullable = false)
    @Builder.Default
    private Long executionTimeMs = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}

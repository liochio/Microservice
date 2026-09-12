package com.liochio.payment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

/**
 * ==============================================================================
 * Thực Thể Khóa Bất Biến Giao Dịch (Idempotency Key Entity)
 * ==============================================================================
 */
@Entity(name = "PaymentIdempotencyKeyEntity")
@Table(name = "idempotency_keys", indexes = {
        @Index(name = "uk_idempotency_key", columnList = "idempotency_key", unique = true),
        @Index(name = "idx_idempotency_tenant_user", columnList = "tenant_id, user_id"),
        @Index(name = "idx_idempotency_lock", columnList = "locked_until")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdempotencyKeyEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", length = 128, nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "tenant_id", length = 50, nullable = false)
    @Builder.Default
    private String tenantId = "SYSTEM";

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "request_path", length = 255, nullable = false)
    private String requestPath;

    @Column(name = "request_hash", length = 128)
    private String requestHash;

    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private String status = "PROCESSING"; // PROCESSING, COMPLETED, FAILED

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "status_code")
    @Builder.Default
    private Integer statusCode = 200;

    @Column(name = "locked_until", nullable = false)
    private Instant lockedUntil;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    @Builder.Default
    private Instant updatedAt = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getRequestPath() { return requestPath; }
    public void setRequestPath(String requestPath) { this.requestPath = requestPath; }
    public String getRequestHash() { return requestHash; }
    public void setRequestHash(String requestHash) { this.requestHash = requestHash; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
    public Integer getStatusCode() { return statusCode; }
    public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }
    public Instant getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(Instant lockedUntil) { this.lockedUntil = lockedUntil; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
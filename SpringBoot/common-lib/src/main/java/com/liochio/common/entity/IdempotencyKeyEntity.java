package com.liochio.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

/**
 * ==============================================================================
 * Thực Thể Khóa Chống Trùng Lặp Request (Idempotency Key Entity - Bảng 37)
 * ==============================================================================
 * 
 * Mục đích:
 * - Lưu trữ Idempotency Key, Hash Payload và kết quả Response Body.
 * - Khi Client gửi lại cùng một Idempotency-Key (do Retry mạng), trả về kết quả đã cache
 *   mà không thực thi lại giao dịch (tránh double charge hoặc double creation).
 */
@Entity
@Table(name = "idempotency_keys", indexes = {
        @Index(name = "idx_idempotency_tenant", columnList = "idempotency_key, tenant_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdempotencyKeyEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "idempotency_key", length = 100, nullable = false)
    private String idempotencyKey;

    @Column(name = "tenant_id", length = 50, nullable = false)
    private String tenantId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "request_hash", length = 64, nullable = false)
    private String requestHash;

    @Column(name = "response_body", columnDefinition = "JSON", nullable = false)
    private String responseBody;

    @Column(name = "status_code", nullable = false)
    private Integer statusCode;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}

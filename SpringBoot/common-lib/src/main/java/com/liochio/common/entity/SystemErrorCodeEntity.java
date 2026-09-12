package com.liochio.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

/**
 * ==============================================================================
 * Thực Thể Danh Mục Mã Lỗi Hệ Thống (System Error Code Entity - Bảng 17)
 * ==============================================================================
 * 
 * Mục đích:
 * - Lưu trữ động mã lỗi số nguyên (code), HTTP status và thông điệp lỗi đa ngôn ngữ JSON
 *   (default_message: {"vi": "...", "en": "..."}).
 */
@Entity
@Table(name = "system_error_codes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemErrorCodeEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "code", nullable = false)
    private Integer code;

    @Column(name = "error_key", length = 100, nullable = false)
    private String errorKey;

    @Column(name = "http_status", nullable = false)
    private Integer httpStatus;

    @Column(name = "default_message", columnDefinition = "JSON", nullable = false)
    private String defaultMessage;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    @Builder.Default
    private Instant updatedAt = Instant.now();
}

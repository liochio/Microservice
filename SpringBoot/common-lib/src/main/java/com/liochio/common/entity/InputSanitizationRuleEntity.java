package com.liochio.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

/**
 * ==============================================================================
 * Thực Thể Quy Tắc Làm Sạch Dữ Liệu Động (Input Sanitization Rule Entity - Bảng 35)
 * ==============================================================================
 * 
 * Mục đích:
 * - Lưu trữ các biểu thức chính quy (Regex Pattern) dùng để lọc bỏ mã độc XSS, HTML,
 *   SQL Injection hoặc ký tự lạ từ dữ liệu đầu vào.
 * - Được nạp vào L1 Cache (Caffeine) và áp dụng qua @DynamicSanitize.
 */
@Entity
@Table(name = "input_sanitization_rules")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InputSanitizationRuleEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_key", length = 50, nullable = false, unique = true)
    private String ruleKey;

    @Column(name = "regex_pattern", length = 255, nullable = false)
    private String regexPattern;

    @Column(name = "replacement", length = 50, nullable = false)
    @Builder.Default
    private String replacement = "";

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

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

package com.liochio.entityservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

/**
 * ==============================================================================
 * Thực Thể Từ Điển Đa Ngôn Ngữ (i18n Dictionary Entity - Bảng 16)
 * ==============================================================================
 */
@Entity
@Table(name = "i18n_dictionaries", indexes = {
        @Index(name = "uk_tenant_locale_key", columnList = "tenant_id, locale, text_key", unique = true),
        @Index(name = "idx_i18n_lookup", columnList = "tenant_id, locale")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class I18nDictionaryEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", length = 50, nullable = false)
    @Builder.Default
    private String tenantId = "SYSTEM";

    @Column(name = "text_key", length = 150, nullable = false)
    private String textKey;

    @Column(name = "locale", length = 10, nullable = false)
    private String locale;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

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

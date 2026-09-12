package com.liochio.entityservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

/**
 * ==============================================================================
 * Thực Thể Tích Hợp API Ngoài (External API Integration - Bảng 30)
 * ==============================================================================
 */
@Entity
@Table(name = "external_api_integrations", indexes = {
        @Index(name = "uk_tenant_service", columnList = "tenant_id, service_code", unique = true),
        @Index(name = "idx_service_lookup", columnList = "tenant_id, service_code, is_active")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalApiIntegrationEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", length = 50, nullable = false)
    @Builder.Default
    private String tenantId = "SYSTEM";

    @Column(name = "service_code", length = 50, nullable = false)
    private String serviceCode;

    @Column(name = "provider_name", length = 100, nullable = false)
    private String providerName;

    @Column(name = "base_url", length = 500, nullable = false)
    private String baseUrl;

    @Column(name = "config_payload", columnDefinition = "JSON", nullable = false)
    private String configPayload;

    @Column(name = "cache_ttl_seconds", nullable = false)
    @Builder.Default
    private Integer cacheTtlSeconds = 3600;

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

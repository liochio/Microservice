package com.liochio.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serializable;
import java.time.Instant;

/**
 * ==============================================================================
 * Thực Thể Khách Thuê Nền Tảng (Tenant Entity - Bảng 1)
 * ==============================================================================
 */
@Entity
@Table(name = "tenants")
@SQLDelete(sql = "UPDATE tenants SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", length = 50, nullable = false)
    private String id;

    @Column(name = "name", length = 150, nullable = false)
    private String name;

    @Column(name = "type", length = 30, nullable = false)
    @Builder.Default
    private String type = "INDIVIDUAL"; // INDIVIDUAL, ENTERPRISE

    @Column(name = "domain", length = 255, unique = true)
    private String domain;

    @Column(name = "subdomain", length = 100, unique = true)
    private String subdomain;

    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, SUSPENDED, EXPIRED, PENDING_SETUP

    @Column(name = "storage_limit_mb", nullable = false)
    @Builder.Default
    private Long storageLimitMb = 5120L;

    @Column(name = "max_sub_accounts", nullable = false)
    @Builder.Default
    private Integer maxSubAccounts = 1;

    @Column(name = "contact_email", length = 150, nullable = false)
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @Column(name = "last_modified_by", length = 100)
    private String lastModifiedBy;
}

package com.liochio.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tenant_roles", uniqueConstraints = {
    @UniqueConstraint(name = "uk_tenant_role", columnNames = {"tenant_id", "role_code"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantRoleEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", length = 50, nullable = false)
    private String tenantId;

    @Column(name = "role_code", length = 50, nullable = false)
    private String roleCode;

    @Column(name = "role_name", length = 100, nullable = false)
    private String roleName;

    @Column(name = "role_type", length = 30, nullable = false)
    @Builder.Default
    private String roleType = "STAFF"; // CORP_ADMIN, MAKER, CHECKER, BRANCH_MANAGER, AUDITOR, STAFF, CUSTOMER

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "is_system_reserved", nullable = false)
    @Builder.Default
    private Boolean isSystemReserved = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "tenant_role_permissions",
        joinColumns = @JoinColumn(name = "tenant_role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<MasterPermissionEntity> permissions = new HashSet<>();
}

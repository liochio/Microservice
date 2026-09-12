package com.liochio.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "master_menus")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterMenuEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "code", length = 80, nullable = false)
    private String code;

    @Column(name = "parent_code", length = 80)
    private String parentCode;

    @Column(name = "portal_type", length = 30, nullable = false)
    private String portalType; // SUPERADMIN, CORP, CONSUMER, SYSTEM

    @Column(name = "route_path", length = 150)
    private String routePath;

    @Column(name = "icon", length = 60)
    private String icon;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_leaf", nullable = false)
    @Builder.Default
    private Boolean isLeaf = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "menuCode", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MenuI18nEntity> i18nList = new ArrayList<>();
}

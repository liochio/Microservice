package com.liochio.entityservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

/**
 * ==============================================================================
 * Thực Thể Định Nghĩa Loại Thực Thể (Entity Type Entity - Bảng 10)
 * ==============================================================================
 */
@Entity
@Table(name = "entity_types")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityTypeEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "code", length = 50, nullable = false)
    private String code;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "is_hierarchical", nullable = false)
    @Builder.Default
    private Boolean isHierarchical = false;

    @Column(name = "icon", length = 100)
    private String icon;

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

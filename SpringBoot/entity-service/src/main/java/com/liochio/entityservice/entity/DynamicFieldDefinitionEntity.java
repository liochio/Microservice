package com.liochio.entityservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

/**
 * ==============================================================================
 * Thực Thể Định Nghĩa Thuộc Tính Động (Field Definition Entity - Bảng 11)
 * ==============================================================================
 */
@Entity
@Table(name = "dynamic_field_definitions", indexes = {
        @Index(name = "uk_entity_type_field", columnList = "entity_type_code, field_key", unique = true)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DynamicFieldDefinitionEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type_code", length = 50, nullable = false)
    private String entityTypeCode;

    @Column(name = "field_key", length = 50, nullable = false)
    private String fieldKey;

    @Column(name = "field_label", columnDefinition = "JSON", nullable = false)
    private String fieldLabel;

    @Column(name = "data_type", length = 30, nullable = false)
    private String dataType;

    @Column(name = "is_required", nullable = false)
    @Builder.Default
    private Boolean isRequired = false;

    @Column(name = "is_searchable", nullable = false)
    @Builder.Default
    private Boolean isSearchable = false;

    @Column(name = "is_filterable", nullable = false)
    @Builder.Default
    private Boolean isFilterable = false;

    @Column(name = "validation_rules", columnDefinition = "JSON")
    private String validationRules;

    @Column(name = "ui_component", length = 50, nullable = false)
    private String uiComponent;

    @Column(name = "default_value", length = 255)
    private String defaultValue;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

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

package com.liochio.entityservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

/**
 * ==============================================================================
 * Thực Thể Lưu Trữ Lịch Sử Phiên Bản Thực Thể Động (Dynamic Entity Revision Snapshot)
 * ==============================================================================
 * 
 * Mục đích:
 * 1. Lưu lại toàn bộ trạng thái và snapshot của `attributes` qua từng lần chỉnh sửa.
 * 2. Hỗ trợ truy vết audit, so sánh khác biệt (Diff), và khôi phục (Rollback).
 */
@Entity
@Table(name = "dynamic_entity_revisions", indexes = {
        @Index(name = "idx_entity_revision", columnList = "tenant_id, entity_id, revision_number", unique = true),
        @Index(name = "idx_revision_created", columnList = "entity_id, created_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DynamicEntityRevisionEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", length = 50, nullable = false)
    private String tenantId;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "revision_number", nullable = false)
    private Integer revisionNumber;

    @Column(name = "entity_type", length = 100, nullable = false)
    private String entityType;

    @Column(name = "slug", length = 200, nullable = false)
    private String slug;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attributes_snapshot", columnDefinition = "json", nullable = false)
    private Map<String, Object> attributesSnapshot;

    @Column(name = "status", length = 30, nullable = false)
    private String status;

    @Column(name = "modified_by", length = 100)
    private String modifiedBy;

    @Column(name = "change_summary", length = 500)
    private String changeSummary;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
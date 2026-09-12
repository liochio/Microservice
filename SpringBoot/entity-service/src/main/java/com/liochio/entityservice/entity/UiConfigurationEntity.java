package com.liochio.entityservice.entity;

import com.liochio.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.util.Map;

/**
 * ==============================================================================
 * Thực Thể Cấu Hình Giao Diện Server-Driven UI (UI Configuration Entity)
 * ==============================================================================
 * 
 * Mục đích:
 * - Lưu trữ cấu trúc khối giao diện (Layout Schema) dạng JSON để trả về cho Frontend ReactJS
 *   tự động render mà không cần deploy lại mã nguồn Frontend.
 */
@Entity
@Table(name = "ui_configurations")
@SQLDelete(sql = "UPDATE ui_configurations SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UiConfigurationEntity extends BaseEntity {

    @Column(name = "page_code", length = 100, nullable = false)
    private String pageCode;

    @Column(name = "theme_name", length = 50, nullable = false)
    @Builder.Default
    private String themeName = "default_theme";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "layout_schema", columnDefinition = "json", nullable = false)
    private Map<String, Object> layoutSchema;
}

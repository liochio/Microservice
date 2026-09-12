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
 * Thực Thể Định Nghĩa Biểu Mẫu Nhập Liệu Động (Form Definition Entity)
 * ==============================================================================
 * 
 * Mục đích:
 * - Lưu trữ danh sách các trường (Fields), kiểu dữ liệu (Text, Number, Email...)
 *   và quy tắc validate để Frontend tự động vẽ Form và kiểm tra dữ liệu đầu vào.
 */
@Entity
@Table(name = "form_definitions")
@SQLDelete(sql = "UPDATE form_definitions SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormDefinitionEntity extends BaseEntity {

    @Column(name = "form_code", length = 100, nullable = false)
    private String formCode;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "field_definitions", columnDefinition = "json", nullable = false)
    private Map<String, Object> fieldDefinitions;
}

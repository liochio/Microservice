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
 * Thực Thể Động Hybrid EAV + JSONB (Dynamic Entity)
 * ==============================================================================
 * 
 * Mục đích:
 * - Hỗ trợ mở rộng không giới hạn các loại thực thể (Blog, Sản phẩm, Khách hàng, Chứng chỉ...)
 *   mà không cần phải chạy lại migration sửa cấu trúc bảng cơ sở dữ liệu.
 * - Toàn bộ các thuộc tính động được lưu dưới trường 'attributes' (JSON).
 */
@Entity
@Table(name = "dynamic_entities")
@SQLDelete(sql = "UPDATE dynamic_entities SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DynamicEntity extends BaseEntity {

    @Column(name = "entity_type", length = 100, nullable = false)
    private String entityType;

    @Column(name = "slug", length = 200, nullable = false)
    private String slug;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attributes", columnDefinition = "json", nullable = false)
    private Map<String, Object> attributes;

    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private String status = "PUBLISHED";

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Long viewCount = 0L;
}

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

import java.util.List;
import java.util.Map;

/**
 * ==============================================================================
 * Thực Thể Cấu Hình Menu & Điều Hướng Động (Navigation Menu Entity)
 * ==============================================================================
 * 
 * Mục đích:
 * - Lưu trữ cây điều hướng (Menu tree, links, order) để Frontend tự động hiển thị Navbar / Footer.
 */
@Entity
@Table(name = "navigation_menus")
@SQLDelete(sql = "UPDATE navigation_menus SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NavigationMenuEntity extends BaseEntity {

    @Column(name = "menu_code", length = 100, nullable = false)
    private String menuCode;

    @Column(name = "title", length = 150, nullable = false)
    private String title;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "items", columnDefinition = "json", nullable = false)
    private List<Map<String, Object>> items;
}

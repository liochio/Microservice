package com.liochio.auth.entity;

import com.liochio.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * ==============================================================================
 * Thực Thể Quyền Hạn Chi Tiết (Permission Entity - Dynamic RBAC)
 * ==============================================================================
 * 
 * Mục đích:
 * - Lưu trữ các quyền hạn tài nguyên chi tiết theo chuẩn 'resource:action'
 *   (vd: 'portfolio:read', 'portfolio:write', 'entity:delete').
 */
@Entity
@Table(name = "permissions")
@SQLDelete(sql = "UPDATE permissions SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionEntity extends BaseEntity {

    @Column(name = "permission_code", length = 100, nullable = false, unique = true)
    private String permissionCode;

    @Column(name = "resource_name", length = 50, nullable = false)
    private String resourceName;

    @Column(name = "action_name", length = 50, nullable = false)
    private String actionName;

    @Column(name = "description", length = 255)
    private String description;

    public String getPermissionCode() { return permissionCode; }
    public void setPermissionCode(String permissionCode) { this.permissionCode = permissionCode; }
    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }
    public String getActionName() { return actionName; }
    public void setActionName(String actionName) { this.actionName = actionName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

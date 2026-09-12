package com.liochio.auth.entity;

import com.liochio.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.HashSet;
import java.util.Set;

/**
 * ==============================================================================
 * Thực Thể Vai Trò Người Dùng (Role Entity - Dynamic RBAC)
 * ==============================================================================
 * 
 * Mục đích:
 * - Đại diện cho vai trò của người dùng (ROLE_SUPER_ADMIN, ROLE_TENANT_ADMIN, ROLE_EDITOR...).
 * - Liên kết N-N với PermissionEntity qua bảng `role_permissions`.
 */
@Entity
@Table(name = "roles")
@SQLDelete(sql = "UPDATE roles SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleEntity extends BaseEntity {

    @Column(name = "role_name", length = 50, nullable = false)
    private String roleName;

    @Column(name = "description", length = 255)
    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<PermissionEntity> permissions = new HashSet<>();

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Set<PermissionEntity> getPermissions() { return permissions != null ? permissions : new HashSet<>(); }
    public void setPermissions(Set<PermissionEntity> permissions) { this.permissions = permissions; }
}

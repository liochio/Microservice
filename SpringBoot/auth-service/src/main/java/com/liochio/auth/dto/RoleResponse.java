package com.liochio.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * ==============================================================================
 * DTO Trả Về Thông Tin Vai Trò (Role Response DTO)
 * ==============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {

    private Long id;
    private String roleName;
    private String description;
    private Set<String> permissions;

    public static RoleResponseBuilder builder() { return new RoleResponseBuilder(); }
    public static class RoleResponseBuilder {
        private Long id;
        private String roleName;
        private String description;
        private Set<String> permissions;

        public RoleResponseBuilder id(Long id) { this.id = id; return this; }
        public RoleResponseBuilder roleName(String roleName) { this.roleName = roleName; return this; }
        public RoleResponseBuilder description(String description) { this.description = description; return this; }
        public RoleResponseBuilder permissions(Set<String> permissions) { this.permissions = permissions; return this; }
        public RoleResponse build() {
            RoleResponse r = new RoleResponse();
            r.setId(id);
            r.setRoleName(roleName);
            r.setDescription(description);
            r.setPermissions(permissions);
            return r;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Set<String> getPermissions() { return permissions; }
    public void setPermissions(Set<String> permissions) { this.permissions = permissions; }
}

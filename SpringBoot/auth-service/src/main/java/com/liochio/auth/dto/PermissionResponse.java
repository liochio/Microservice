package com.liochio.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ==============================================================================
 * DTO Trả Về Quyền Hạn (Permission Response DTO)
 * ==============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponse {

    private Long id;
    private String permissionCode;
    private String resourceName;
    private String actionName;
    private String description;

    public static PermissionResponseBuilder builder() { return new PermissionResponseBuilder(); }
    public static class PermissionResponseBuilder {
        private Long id;
        private String permissionCode;
        private String resourceName;
        private String actionName;
        private String description;

        public PermissionResponseBuilder id(Long id) { this.id = id; return this; }
        public PermissionResponseBuilder permissionCode(String permissionCode) { this.permissionCode = permissionCode; return this; }
        public PermissionResponseBuilder resourceName(String resourceName) { this.resourceName = resourceName; return this; }
        public PermissionResponseBuilder actionName(String actionName) { this.actionName = actionName; return this; }
        public PermissionResponseBuilder description(String description) { this.description = description; return this; }
        public PermissionResponse build() {
            PermissionResponse r = new PermissionResponse();
            r.setId(id);
            r.setPermissionCode(permissionCode);
            r.setResourceName(resourceName);
            r.setActionName(actionName);
            r.setDescription(description);
            return r;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPermissionCode() { return permissionCode; }
    public void setPermissionCode(String permissionCode) { this.permissionCode = permissionCode; }
    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }
    public String getActionName() { return actionName; }
    public void setActionName(String actionName) { this.actionName = actionName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

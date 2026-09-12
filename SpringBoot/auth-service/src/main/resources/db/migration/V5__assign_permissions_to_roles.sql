-- ==============================================================================
-- Migration V5: Gán quyền hạn chi tiết cho các vai trò còn lại
-- ==============================================================================

-- ROLE_TENANT_ADMIN (id = 2): Có toàn quyền quản trị portfolio, dynamic entity và upload media
INSERT IGNORE INTO `role_permissions` (`role_id`, `permission_id`)
SELECT 2, id FROM `permissions` WHERE `permission_code` IN ('portfolio:read', 'portfolio:write', 'portfolio:delete', 'entity:read', 'entity:write', 'entity:delete', 'media:upload');

-- ROLE_EDITOR (id = 3): Có quyền đọc, ghi portfolio, entity và upload media
INSERT IGNORE INTO `role_permissions` (`role_id`, `permission_id`)
SELECT 3, id FROM `permissions` WHERE `permission_code` IN ('portfolio:read', 'portfolio:write', 'entity:read', 'entity:write', 'media:upload');

-- ROLE_VIEWER (id = 4): Có quyền đọc portfolio và dynamic entity
INSERT IGNORE INTO `role_permissions` (`role_id`, `permission_id`)
SELECT 4, id FROM `permissions` WHERE `permission_code` IN ('portfolio:read', 'entity:read');

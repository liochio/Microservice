-- ==============================================================================
-- Migration V1: Khởi tạo bảng dữ liệu cho Auth Service (Dynamic RBAC & Tokens)
-- ==============================================================================

-- 1. Bảng Quản lý Quyền hạn (Permissions)
CREATE TABLE IF NOT EXISTS `permissions` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `permission_code` VARCHAR(100) NOT NULL UNIQUE,
    `resource_name` VARCHAR(50) NOT NULL,
    `action_name` VARCHAR(50) NOT NULL,
    `description` VARCHAR(255),
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    INDEX `idx_perm_tenant` (`tenant_id`),
    INDEX `idx_perm_code` (`permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Bảng Quản lý Vai trò (Roles)
CREATE TABLE IF NOT EXISTS `roles` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `role_name` VARCHAR(50) NOT NULL,
    `description` VARCHAR(255),
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE KEY `uk_role_tenant` (`tenant_id`, `role_name`),
    INDEX `idx_role_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Bảng Quản lý Người dùng (Users)
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `username` VARCHAR(100) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `email` VARCHAR(150) NOT NULL,
    `full_name` VARCHAR(150),
    `avatar_url` VARCHAR(500),
    `status` VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE KEY `uk_user_tenant_username` (`tenant_id`, `username`),
    INDEX `idx_user_tenant_email` (`tenant_id`, `email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Bảng Liên kết N-N: User và Role
CREATE TABLE IF NOT EXISTS `user_roles` (
    `user_id` BIGINT NOT NULL,
    `role_id` BIGINT NOT NULL,
    PRIMARY KEY (`user_id`, `role_id`),
    CONSTRAINT `fk_ur_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_ur_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Bảng Liên kết N-N: Role và Permission
CREATE TABLE IF NOT EXISTS `role_permissions` (
    `role_id` BIGINT NOT NULL,
    `permission_id` BIGINT NOT NULL,
    PRIMARY KEY (`role_id`, `permission_id`),
    CONSTRAINT `fk_rp_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_rp_permission` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- DỮ LIỆU KHỞI TẠO MẶC ĐỊNH (SEED DATA)
-- ==============================================================================

-- Thêm Permissions chuẩn
INSERT IGNORE INTO `permissions` (`permission_code`, `resource_name`, `action_name`, `description`) VALUES
('portfolio:read', 'portfolio', 'read', 'Quyền xem danh sách portfolio'),
('portfolio:write', 'portfolio', 'write', 'Quyền tạo mới hoặc chỉnh sửa portfolio'),
('portfolio:delete', 'portfolio', 'delete', 'Quyền xóa portfolio'),
('entity:read', 'entity', 'read', 'Quyền đọc dynamic entity'),
('entity:write', 'entity', 'write', 'Quyền ghi dynamic entity'),
('entity:delete', 'entity', 'delete', 'Quyền xóa dynamic entity'),
('media:upload', 'media', 'upload', 'Quyền tải lên hình ảnh và media'),
('system:admin', 'system', 'admin', 'Toàn quyền quản trị hệ thống');

-- Thêm Roles chuẩn
INSERT IGNORE INTO `roles` (`id`, `tenant_id`, `role_name`, `description`) VALUES
(1, 'default', 'ROLE_SUPER_ADMIN', 'Quản trị viên tối cao'),
(2, 'default', 'ROLE_TENANT_ADMIN', 'Quản trị viên website khách thuê'),
(3, 'default', 'ROLE_EDITOR', 'Biên tập viên nội dung'),
(4, 'default', 'ROLE_VIEWER', 'Người xem thông thường');

-- Gán toàn bộ quyền cho ROLE_SUPER_ADMIN
INSERT IGNORE INTO `role_permissions` (`role_id`, `permission_id`)
SELECT 1, id FROM `permissions`;

-- Thêm tài khoản mặc định: superadmin (Mật khẩu: 12345678)
-- Hash BCrypt $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi tương đương password 'password' hoặc '12345678'
INSERT IGNORE INTO `users` (`id`, `tenant_id`, `username`, `password`, `email`, `full_name`, `status`) VALUES
(1, 'default', 'superadmin', '$2a$12$Nq9v7P8vV1eR7o4M2E4uEu9yD0iC1dJ0v8kK5rG3u2oB4zL9mN8m.', 'admin@portfolio-engine.dev', 'System Super Administrator', 'ACTIVE');

-- Gán ROLE_SUPER_ADMIN cho user superadmin
INSERT IGNORE INTO `user_roles` (`user_id`, `role_id`) VALUES (1, 1);

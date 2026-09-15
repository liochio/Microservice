-- ==============================================================================
-- Liochio FinTech Platform - Enterprise Security & RBAC Master Schema DDL
-- CSDL: `liochio_core_db` (Core Infrastructure Database)
-- ==============================================================================

USE `liochio_core_db`;

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Bảng Quản lý Phiên Quét mã QR Đăng nhập (qr_login_sessions)
CREATE TABLE IF NOT EXISTS `qr_login_sessions` (
    `id` VARCHAR(64) PRIMARY KEY,                 -- QR Session UUID (Hạn 120s)
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    `web_device_id` VARCHAR(150) NOT NULL,
    `web_ip_address` VARCHAR(50) NOT NULL,
    `web_user_agent` VARCHAR(500) NOT NULL,
    `mobile_user_id` BIGINT NULL,
    `mobile_device_id` VARCHAR(150) NULL,
    `status` VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- 'PENDING', 'SCANNED', 'CONFIRMED', 'EXPIRED', 'REJECTED'
    `exchange_auth_code` VARCHAR(255) NULL,          -- Code 1 lần đổi Token (Hạn 10s)
    `expires_at` TIMESTAMP NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_qr_tenant_status` (`tenant_id`, `status`),
    INDEX `idx_qr_expiry` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Bảng Audit Logs Toàn diện (audit_logs)
DROP TABLE IF EXISTS `audit_logs`;
CREATE TABLE `audit_logs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `trace_id` VARCHAR(64) NOT NULL,              -- Distributed Tracing ID từ Gateway
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    `user_id` BIGINT NULL,
    `username` VARCHAR(100) NULL,
    `user_email` VARCHAR(150) NULL,
    `user_role` VARCHAR(50) NULL,
    `client_ip` VARCHAR(50) NOT NULL,
    `platform` VARCHAR(30) NOT NULL DEFAULT 'WEB', -- 'WEB', 'IOS', 'ANDROID', 'POSTMAN', 'UNKNOWN'
    `device_id` VARCHAR(150) NULL,
    `device_name` VARCHAR(150) NULL,
    `user_agent` VARCHAR(500) NULL,
    `module` VARCHAR(50) NOT NULL DEFAULT 'AUTH',  -- 'AUTH', 'TOUR', 'BOOKING', 'PAYMENT', 'USER', 'MEDIA', 'AI'
    `action_type` VARCHAR(50) NOT NULL DEFAULT 'VIEW', -- 'LOGIN', 'LOGOUT', 'REGISTER', 'CLICK_FEATURE', 'VIEW', 'CREATE', 'UPDATE', 'DELETE', 'APPROVE', 'REJECT', 'REFUND', 'EXPORT'
    `action_description` VARCHAR(255) NOT NULL DEFAULT '',
    `http_method` VARCHAR(10) NOT NULL DEFAULT 'GET',
    `request_uri` VARCHAR(500) NOT NULL,
    `request_params` JSON NULL,
    `request_body` JSON NULL,
    `old_data` JSON NULL,
    `new_data` JSON NULL,
    `status` VARCHAR(30) NOT NULL DEFAULT 'SUCCESS', -- 'SUCCESS', 'FAILED', 'PENDING', 'REJECTED'
    `http_status_code` INT NOT NULL DEFAULT 200,
    `error_message` TEXT NULL,
    `execution_time_ms` BIGINT NOT NULL DEFAULT 0,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_audit_tenant_time` (`tenant_id`, `created_at` DESC),
    INDEX `idx_audit_user` (`tenant_id`, `user_id`, `action_type`),
    INDEX `idx_audit_module` (`tenant_id`, `module`, `status`),
    INDEX `idx_audit_trace` (`trace_id`),
    INDEX `idx_audit_ip` (`client_ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- MASTER DATA SEEDING (Khởi tạo dữ liệu hệ thống)
-- ==============================================================================

-- 1. Seed Tenants
INSERT INTO `tenants` (`id`, `name`, `type`, `domain`, `subdomain`, `status`, `tenant_id`, `contact_email`)
VALUES
('SYSTEM', 'System Super Administration', 'SYSTEM', 'system.fintech.local', 'system', 'ACTIVE', 'SYSTEM', 'admin.fintech@liochio.com'),
('corp_vietravel', 'Vietravel Corporation FinTech Partner', 'ENTERPRISE', 'partner.fintech.local', 'partner', 'ACTIVE', 'corp_vietravel', 'contact@partner.com'),
('tenant_fintech_01', 'Liochio Retail Banking Services', 'ENTERPRISE', 'retail.fintech.local', 'retail', 'ACTIVE', 'tenant_fintech_01', 'retail@liochio.com')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `status` = VALUES(`status`), `contact_email` = VALUES(`contact_email`);

-- 2. Seed Permissions
INSERT INTO `permissions` (`id`, `tenant_id`, `permission_code`, `resource_name`, `action_name`, `description`)
VALUES
(1, 'SYSTEM', 'auth:login', 'AUTH', 'READ', 'Quyền đăng nhập vào hệ thống'),
(2, 'SYSTEM', 'auth:refresh', 'AUTH', 'READ', 'Quyền làm mới Token JWT'),
(3, 'SYSTEM', 'auth:logout', 'AUTH', 'READ', 'Quyền đăng xuất khỏi hệ thống'),

(4, 'SYSTEM', 'user:read', 'USER', 'READ', 'Xem danh sách và chi tiết người dùng'),
(5, 'SYSTEM', 'user:create', 'USER', 'CREATE', 'Tạo tài khoản người dùng mới'),
(6, 'SYSTEM', 'user:update', 'USER', 'UPDATE', 'Cập nhật thông tin người dùng'),
(7, 'SYSTEM', 'user:delete', 'USER', 'DELETE', 'Xóa tài khoản người dùng'),
(8, 'SYSTEM', 'user:assign_role', 'USER', 'UPDATE', 'Gán vai trò và quyền cho người dùng'),

(9, 'SYSTEM', 'tour:read', 'TOUR', 'READ', 'Xem danh sách và chi tiết các tour du lịch'),
(10, 'SYSTEM', 'tour:create', 'TOUR', 'CREATE', 'Tạo mới tour du lịch'),
(11, 'SYSTEM', 'tour:update', 'TOUR', 'UPDATE', 'Chỉnh sửa tour du lịch'),
(12, 'SYSTEM', 'tour:delete', 'TOUR', 'DELETE', 'Xóa tour du lịch'),
(13, 'SYSTEM', 'tour:approve', 'TOUR', 'APPROVE', 'Phê duyệt xuất bản tour du lịch'),

(14, 'SYSTEM', 'booking:read', 'BOOKING', 'READ', 'Xem danh sách đơn đặt chỗ/tour'),
(15, 'SYSTEM', 'booking:create', 'BOOKING', 'CREATE', 'Đặt tour hoặc tạo booking mới'),
(16, 'SYSTEM', 'booking:cancel', 'BOOKING', 'UPDATE', 'Hủy đơn đặt chỗ/booking'),
(17, 'SYSTEM', 'booking:refund', 'BOOKING', 'REFUND', 'Xử lý hoàn tiền đơn đặt chỗ'),

(18, 'SYSTEM', 'payment:create', 'PAYMENT', 'CREATE', 'Tạo lệnh thanh toán đơn hàng'),
(19, 'SYSTEM', 'payment:verify', 'PAYMENT', 'READ', 'Xác thực trạng thái giao dịch thanh toán'),
(20, 'SYSTEM', 'payment:config', 'PAYMENT', 'UPDATE', 'Cấu hình cổng thanh toán Tenant'),

(21, 'SYSTEM', 'media:upload', 'MEDIA', 'CREATE', 'Tải lên tệp tin và media dung lượng lớn'),
(22, 'SYSTEM', 'media:delete', 'MEDIA', 'DELETE', 'Xóa tệp tin media khỏi hệ thống'),

(23, 'SYSTEM', 'ai:chat', 'AI', 'READ', 'Gửi tin nhắn hội thoại với AI Assistant'),
(24, 'SYSTEM', 'ai:train_kb', 'AI', 'CREATE', 'Nạp tài liệu và huấn luyện RAG Vector Knowledge Base')
ON DUPLICATE KEY UPDATE `permission_code` = VALUES(`permission_code`), `description` = VALUES(`description`);

-- 3. Seed Roles
INSERT INTO `roles` (`id`, `tenant_id`, `role_name`, `description`)
VALUES
(1, 'SYSTEM', 'ROLE_SUPER_ADMIN', 'Quản trị viên tối cao toàn bộ hệ sinh thái'),
(2, 'SYSTEM', 'ROLE_CORP_ADMIN', 'Quản trị viên cấp doanh nghiệp Tenant'),
(3, 'SYSTEM', 'ROLE_SALE_MANAGER', 'Quản lý kinh doanh và điều phối dịch vụ'),
(4, 'SYSTEM', 'ROLE_CUSTOMER', 'Khách hàng / Thành viên hệ thống')
ON DUPLICATE KEY UPDATE `role_name` = VALUES(`role_name`), `description` = VALUES(`description`);

-- 4. Seed Role Permissions
-- ROLE_SUPER_ADMIN (Toàn quyền 100% ID 1..24)
INSERT IGNORE INTO `role_permissions` (`role_id`, `permission_id`)
SELECT 1, `id` FROM `permissions`;

-- ROLE_CORP_ADMIN (Quản trị Tenant)
INSERT IGNORE INTO `role_permissions` (`role_id`, `permission_id`)
SELECT 2, `id` FROM `permissions` WHERE `permission_code` NOT IN ('ai:train_kb');

-- ROLE_SALE_MANAGER (Quản lý tour, booking, user xem)
INSERT IGNORE INTO `role_permissions` (`role_id`, `permission_id`)
SELECT 3, `id` FROM `permissions` WHERE `permission_code` IN (
    'auth:login', 'auth:refresh', 'auth:logout',
    'user:read',
    'tour:read', 'tour:create', 'tour:update',
    'booking:read', 'booking:create', 'booking:cancel',
    'payment:create', 'payment:verify',
    'media:upload'
);

-- ROLE_CUSTOMER (Khách hàng)
INSERT IGNORE INTO `role_permissions` (`role_id`, `permission_id`)
SELECT 4, `id` FROM `permissions` WHERE `permission_code` IN (
    'auth:login', 'auth:refresh', 'auth:logout',
    'tour:read',
    'booking:read', 'booking:create',
    'payment:create', 'payment:verify',
    'ai:chat'
);

-- 5. Seed Super Admin User (admin / 12345678)
-- BCrypt password hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
INSERT INTO `users` (`id`, `tenant_id`, `username`, `email`, `password`, `phone`, `full_name`, `user_type`, `status`, `is_email_verified`, `is_phone_verified`)
VALUES
(1, 'SYSTEM', 'admin', 'admin.fintech@liochio.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0901234567', 'Super System Administrator', 'SUPER_ADMIN', 'ACTIVE', TRUE, TRUE)
ON DUPLICATE KEY UPDATE `password` = VALUES(`password`), `status` = 'ACTIVE';

-- 6. Gán Super Admin Role cho user admin
INSERT IGNORE INTO `user_roles` (`user_id`, `role_id`) VALUES (1, 1);

SET FOREIGN_KEY_CHECKS = 1;

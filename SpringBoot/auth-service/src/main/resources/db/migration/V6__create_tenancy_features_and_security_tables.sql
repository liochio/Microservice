-- ==============================================================================
-- Migration V6: Khởi tạo các bảng Quản trị Tenant, Gói tính năng & Bảo mật đa lớp
-- ==============================================================================

-- 1. Bảng Quản lý Tenant (Cá nhân & Doanh nghiệp)
CREATE TABLE IF NOT EXISTS `tenants` (
    `id` VARCHAR(50) PRIMARY KEY,
    `name` VARCHAR(150) NOT NULL,
    `type` ENUM('INDIVIDUAL', 'ENTERPRISE') NOT NULL DEFAULT 'INDIVIDUAL',
    `domain` VARCHAR(255) NULL,
    `subdomain` VARCHAR(100) NULL,
    `status` ENUM('ACTIVE', 'SUSPENDED', 'EXPIRED', 'PENDING_SETUP') DEFAULT 'ACTIVE',
    `storage_limit_mb` BIGINT NOT NULL DEFAULT 5120,
    `max_sub_accounts` INT NOT NULL DEFAULT 1,
    `contact_email` VARCHAR(150) NOT NULL,
    `contact_phone` VARCHAR(20) NULL,
    `metadata` JSON NULL,
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL,
    `created_by` BIGINT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_tenant_domain` (`domain`),
    UNIQUE KEY `uk_tenant_subdomain` (`subdomain`),
    INDEX `idx_tenant_status` (`status`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Thêm dữ liệu mặc định cho Tenant 'default'
INSERT IGNORE INTO `tenants` (`id`, `name`, `type`, `domain`, `subdomain`, `status`, `contact_email`)
VALUES ('default', 'Nền Tảng Tài Chính Số Liochio (Liochio FinTech Platform)', 'ENTERPRISE', 'localhost', 'default', 'ACTIVE', 'admin.fintech@liochio.com');

-- 2. Bảng Danh mục Tính năng Hệ thống (Super Admin Quản lý)
CREATE TABLE IF NOT EXISTS `system_features` (
    `code` VARCHAR(50) PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `category` VARCHAR(50) NOT NULL,
    `description` TEXT NULL,
    `is_base_feature` BOOLEAN NOT NULL DEFAULT FALSE,
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO `system_features` (`code`, `name`, `category`, `description`, `is_base_feature`) VALUES
('FEATURE_PORTFOLIO', 'Quản lý Portfolio & Dự án', 'CORE', 'Cho phép tạo và trưng bày portfolio sản phẩm', TRUE),
('FEATURE_DYNAMIC_ENTITY', 'Động cơ Thực thể Động (Dynamic Entity)', 'CORE', 'Tự tạo schema và quản lý thực thể dữ liệu đa năng', TRUE),
('FEATURE_ECOMMERCE_BOOKING', 'Thương mại điện tử & Đặt chỗ', 'BUSINESS', 'Tính năng tạo booking, đơn hàng và thanh toán trực tuyến', FALSE),
('FEATURE_AI_CHATBOT', 'Trợ lý ảo AI & RAG Chatbot', 'AI_MODULE', 'Tích hợp Chatbot AI và nạp kho tri thức tự động', FALSE),
('FEATURE_MULTI_LANGUAGE', 'Đa ngôn ngữ Toàn diện (i18n)', 'ADVANCED', 'Tự động dịch thuật và quản lý từ điển đa ngữ', TRUE),
('FEATURE_CHUNK_UPLOAD', 'Tải lên Tệp tin Dung lượng lớn', 'MEDIA', 'Chunk upload video, tài liệu dung lượng lớn lên Cloud Storage', TRUE);

-- 3. Bảng Cấp phát Tính năng cho Tenant
CREATE TABLE IF NOT EXISTS `tenant_features` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL,
    `feature_code` VARCHAR(50) NOT NULL,
    `is_enabled` BOOLEAN NOT NULL DEFAULT TRUE,
    `quota_limit` INT NULL,
    `expired_at` TIMESTAMP NULL,
    `created_by` BIGINT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_tenant_feature` (`tenant_id`, `feature_code`),
    FOREIGN KEY (`tenant_id`) REFERENCES `tenants`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`feature_code`) REFERENCES `system_features`(`code`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Gán toàn bộ feature cho default tenant
INSERT IGNORE INTO `tenant_features` (`tenant_id`, `feature_code`, `is_enabled`)
SELECT 'default', `code`, TRUE FROM `system_features`;

-- Cập nhật bảng `users` với các trường mở rộng
ALTER TABLE `users`
    ADD COLUMN `user_type` ENUM('SUPER_ADMIN', 'TENANT_OWNER', 'CORP_ADMIN', 'SUB_ACCOUNT', 'CUSTOMER') NOT NULL DEFAULT 'CUSTOMER' AFTER `avatar_url`,
    ADD COLUMN `phone` VARCHAR(20) NULL AFTER `password`,
    ADD COLUMN `auth_provider` ENUM('LOCAL', 'GOOGLE', 'FACEBOOK', 'GITHUB') DEFAULT 'LOCAL' AFTER `status`,
    ADD COLUMN `provider_id` VARCHAR(100) NULL AFTER `auth_provider`,
    ADD COLUMN `is_email_verified` BOOLEAN NOT NULL DEFAULT FALSE AFTER `provider_id`,
    ADD COLUMN `is_phone_verified` BOOLEAN NOT NULL DEFAULT FALSE AFTER `is_email_verified`,
    ADD COLUMN `failed_login_attempts` INT NOT NULL DEFAULT 0 AFTER `is_phone_verified`,
    ADD COLUMN `lockout_until` TIMESTAMP NULL AFTER `failed_login_attempts`,
    ADD COLUMN `password_changed_at` TIMESTAMP NULL AFTER `lockout_until`,
    ADD COLUMN `reset_password_token` VARCHAR(255) NULL AFTER `password_changed_at`,
    ADD COLUMN `reset_password_token_expiry` TIMESTAMP NULL AFTER `reset_password_token`,
    ADD COLUMN `preferred_locale` VARCHAR(10) NOT NULL DEFAULT 'vi' AFTER `reset_password_token_expiry`,
    ADD COLUMN `metadata` JSON NULL AFTER `preferred_locale`;

-- Cập nhật user_type cho admin và superadmin
UPDATE `users` SET `user_type` = 'SUPER_ADMIN', `is_email_verified` = TRUE WHERE `username` IN ('superadmin', 'admin');

-- 32. Bảng Quản lý Thiết bị Tin cậy (Device Fingerprints)
CREATE TABLE IF NOT EXISTS `user_devices` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL,
    `user_id` BIGINT NOT NULL,
    `device_id` VARCHAR(150) NOT NULL,
    `device_name` VARCHAR(150) NOT NULL,
    `platform` ENUM('WEB', 'IOS', 'ANDROID', 'MACOS', 'WINDOWS', 'LINUX') NOT NULL,
    `os_version` VARCHAR(50) NULL,
    `app_version` VARCHAR(50) NULL,
    `browser_name` VARCHAR(50) NULL,
    `is_trusted` BOOLEAN NOT NULL DEFAULT FALSE,
    `is_smart_otp_enrolled` BOOLEAN NOT NULL DEFAULT FALSE,
    `fcm_push_token` VARCHAR(500) NULL,
    `last_ip_address` VARCHAR(50) NULL,
    `last_location` VARCHAR(150) NULL,
    `last_active_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `status` ENUM('ACTIVE', 'BLOCKED', 'REVOKED') DEFAULT 'ACTIVE',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_device` (`tenant_id`, `user_id`, `device_id`),
    INDEX `idx_device_status` (`user_id`, `status`),
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 33. Bảng Quản lý Phiên Đăng nhập & Refresh Token
CREATE TABLE IF NOT EXISTS `user_sessions` (
    `id` VARCHAR(64) PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL,
    `user_id` BIGINT NOT NULL,
    `device_id` VARCHAR(150) NOT NULL,
    `refresh_token_hash` VARCHAR(255) NOT NULL,
    `ip_address` VARCHAR(50) NOT NULL,
    `user_agent` VARCHAR(500) NOT NULL,
    `is_revoked` BOOLEAN NOT NULL DEFAULT FALSE,
    `revoked_reason` VARCHAR(100) NULL,
    `expires_at` TIMESTAMP NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `last_accessed_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_session_user` (`tenant_id`, `user_id`, `is_revoked`),
    INDEX `idx_session_expiry` (`expires_at`),
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 34. Bảng Theo vết Đăng nhập & Phân tích Rủi ro IP
CREATE TABLE IF NOT EXISTS `security_login_histories` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL,
    `user_id` BIGINT NULL,
    `attempted_username` VARCHAR(100) NOT NULL,
    `ip_address` VARCHAR(50) NOT NULL,
    `user_agent` VARCHAR(500) NOT NULL,
    `country_code` VARCHAR(10) NULL,
    `city` VARCHAR(100) NULL,
    `isp` VARCHAR(100) NULL,
    `device_fingerprint` VARCHAR(150) NULL,
    `login_status` ENUM('SUCCESS', 'WRONG_PASSWORD', 'LOCKED', 'UNTRUSTED_DEVICE_CHALLENGE', 'INVALID_2FA') NOT NULL,
    `failure_reason` VARCHAR(255) NULL,
    `risk_score` TINYINT DEFAULT 0,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_login_history_ip` (`ip_address`, `created_at`),
    INDEX `idx_login_history_user` (`tenant_id`, `user_id`, `created_at`),
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

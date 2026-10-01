-- ==============================================================================
-- Liochio FinTech Platform - Hệ Thống Cấu Hình SMTP & Chuyển Tiếp Mail Mặc Định
-- Thiết lập bảng cấu hình và dữ liệu mẫu cho cả liochio_app_db và liochio_core_db
-- ==============================================================================

-- 1. BẢNG CẤU HÌNH PYTHON FASTAPI (liochio_app_db.system_settings)
CREATE DATABASE IF NOT EXISTS `liochio_app_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `liochio_app_db`;

CREATE TABLE IF NOT EXISTS `system_settings` (
    `key` VARCHAR(100) NOT NULL PRIMARY KEY COMMENT 'Từ khóa cấu hình',
    `value` VARCHAR(255) NOT NULL COMMENT 'Giá trị cấu hình',
    `type` VARCHAR(50) NOT NULL COMMENT 'Phân loại cấu hình (MAIL, AUTH...)',
    `status` VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Trạng thái cấu hình',
    `description` TEXT NULL COMMENT 'Mô tả chi tiết tác dụng cài đặt',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `system_settings` (`key`, `value`, `type`, `status`, `description`)
VALUES
    ('smtp.host', 'smtp.gmail.com', 'MAIL', 'ACTIVE', 'Máy chủ gửi mail SMTP (Gmail)'),
    ('smtp.port', '587', 'MAIL', 'ACTIVE', 'Cổng kết nối TLS SMTP'),
    ('smtp.username', 'voduylebt99@gmail.com', 'MAIL', 'ACTIVE', 'Tài khoản người gửi SMTP Gmail'),
    ('smtp.password', 'uaennreskbytgkjt', 'MAIL', 'ACTIVE', 'Mật khẩu ứng dụng 16 ký tự Gmail'),
    ('smtp.from_email', 'voduylebt99@gmail.com', 'MAIL', 'ACTIVE', 'Địa chỉ email người gửi hiển thị'),
    ('mail.fallback_to_default_recipient', 'true', 'MAIL', 'ACTIVE', 'Cho phép gửi mail mặc định khi email người nhận không có thật/test (mặc định có)'),
    ('mail.default_recipient', 'voduylebt99@gmail.com', 'MAIL', 'ACTIVE', 'Email mặc định tiếp nhận thông báo và mã OTP thay thế')
ON DUPLICATE KEY UPDATE 
    `value` = VALUES(`value`),
    `status` = 'ACTIVE',
    `description` = VALUES(`description`),
    `updated_at` = NOW();


-- 2. BẢNG CẤU HÌNH SPRING BOOT CORE (liochio_core_db.system_configs)
CREATE DATABASE IF NOT EXISTS `liochio_core_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `liochio_core_db`;

CREATE TABLE IF NOT EXISTS `system_configs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `config_key` VARCHAR(100) NOT NULL UNIQUE,
    `config_value` TEXT NOT NULL,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `description` VARCHAR(255) NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `system_configs` (`config_key`, `config_value`, `is_active`, `description`)
VALUES
    ('smtp.host', 'smtp.gmail.com', 1, 'Máy chủ gửi mail SMTP (Gmail)'),
    ('smtp.port', '587', 1, 'Cổng kết nối TLS SMTP'),
    ('smtp.username', 'voduylebt99@gmail.com', 1, 'Tài khoản người gửi SMTP Gmail'),
    ('smtp.password', 'uaennreskbytgkjt', 1, 'Mật khẩu ứng dụng 16 ký tự Gmail'),
    ('smtp.from_email', 'voduylebt99@gmail.com', 1, 'Địa chỉ email người gửi hiển thị'),
    ('mail.fallback_to_default_recipient', 'true', 1, 'Cho phép gửi mail mặc định khi email người nhận không có thật/test (mặc định có)'),
    ('mail.default_recipient', 'voduylebt99@gmail.com', 1, 'Email mặc định tiếp nhận thông báo và mã OTP thay thế')
ON DUPLICATE KEY UPDATE 
    `config_value` = VALUES(`config_value`),
    `is_active` = 1,
    `description` = VALUES(`description`),
    `updated_at` = NOW();

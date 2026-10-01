-- ==============================================================================
-- Liochio FinTech Platform - MySQL Initialization Script (Docker & Local)
-- Khởi tạo toàn bộ hệ sinh thái Database chuẩn Microservices (Database-per-Service)
-- ==============================================================================

-- 1. Database Quản trị Định danh & Xác thực (Pure IAM & Compliance)
CREATE DATABASE IF NOT EXISTS `liochio_core_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2. Database Sổ cái Kế toán kép Bất biến (Core Banking General Ledger)
CREATE DATABASE IF NOT EXISTS `liochio_ledger_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 3. Database Thực thể Động, Menu Phân quyền, Headless CMS & UI Config Matrix
CREATE DATABASE IF NOT EXISTS `liochio_entity_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 4. Database Cổng Thanh toán, Đặt cọc & Giao dịch Booking
CREATE DATABASE IF NOT EXISTS `liochio_payment_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 5. Database Quản lý Thông báo Đa kênh & Audit Log
CREATE DATABASE IF NOT EXISTS `liochio_notification_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 6. Database Cổng Ứng dụng Tiện ích, IoT Heo Đất & AI Biometrics (Python FastAPI)
CREATE DATABASE IF NOT EXISTS `liochio_app_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Dọn dẹp dứt điểm các Database cũ không còn sử dụng
DROP DATABASE IF EXISTS `db_tour`;
DROP DATABASE IF EXISTS `db_film`;
DROP DATABASE IF EXISTS `db_media`;
DROP DATABASE IF EXISTS `db_music`;
DROP DATABASE IF EXISTS `db_gaming`;
DROP DATABASE IF EXISTS `db_blog`;
DROP DATABASE IF EXISTS `db_content_eav`;
DROP DATABASE IF EXISTS `db_ai_vector`;
DROP DATABASE IF EXISTS `portfolio-engine`;
DROP DATABASE IF EXISTS `liochio_auth_db`;

-- 7. Bảng Cấu hình Toàn Cục & Mail Gateway
USE `liochio_app_db`;
CREATE TABLE IF NOT EXISTS `system_settings` (
    `key` VARCHAR(100) NOT NULL PRIMARY KEY,
    `value` VARCHAR(255) NOT NULL,
    `type` VARCHAR(50) NOT NULL,
    `status` VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    `description` TEXT NULL,
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
ON DUPLICATE KEY UPDATE `value`=VALUES(`value`), `status`='ACTIVE';

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
ON DUPLICATE KEY UPDATE `config_value`=VALUES(`config_value`), `is_active`=1;

-- Cấp toàn quyền cho root user truy cập từ mọi host
GRANT ALL PRIVILEGES ON `liochio_%`.* TO 'root'@'%';
FLUSH PRIVILEGES;

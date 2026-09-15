-- ==============================================================================
-- Migration V3: Khởi tạo bảng Cấu hình Kênh Thông Báo & Mẫu Thông Báo Đa Ngữ
-- ==============================================================================

-- 24. Bảng Cấu hình Kênh Gửi Tin của Tenant
CREATE TABLE IF NOT EXISTS `tenant_notification_configs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL,
    `channel` ENUM('EMAIL', 'SMS', 'TELEGRAM', 'DISCORD', 'FIREBASE_PUSH') NOT NULL,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `config_data` JSON NOT NULL,
    `version` BIGINT NOT NULL DEFAULT 0,
    `created_by` BIGINT NULL,
    `updated_by` BIGINT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_tenant_channel` (`tenant_id`, `channel`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 25. Bảng Mẫu Thông báo Đa ngôn ngữ (Notification Templates)
CREATE TABLE IF NOT EXISTS `notification_templates` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    `template_code` VARCHAR(100) NOT NULL,
    `channel` ENUM('EMAIL', 'SMS', 'TELEGRAM', 'FIREBASE_PUSH') NOT NULL,
    `locale` VARCHAR(10) NOT NULL DEFAULT 'vi',
    `subject` VARCHAR(255) NULL,
    `body_template` TEXT NOT NULL,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `version` BIGINT NOT NULL DEFAULT 0,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_template_lookup` (`tenant_id`, `template_code`, `channel`, `locale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO `notification_templates` (`tenant_id`, `template_code`, `channel`, `locale`, `subject`, `body_template`) VALUES
('SYSTEM', 'WELCOME_EMAIL', 'EMAIL', 'vi', 'Chào mừng bạn đến với Nền tảng Tài chính Số Liochio', '<p>Xin chào {{fullName}}, tài khoản của bạn đã được khởi tạo thành công trên hệ thống Liochio FinTech!</p>'),
('SYSTEM', 'WELCOME_EMAIL', 'EMAIL', 'en', 'Welcome to Liochio FinTech Digital Platform', '<p>Hello {{fullName}}, your account has been successfully created on Liochio FinTech Platform!</p>'),
('SYSTEM', 'BOOKING_CONFIRMATION', 'EMAIL', 'vi', 'Xác nhận giao dịch thành công #{{bookingCode}}', '<p>Giao dịch {{bookingCode}} của bạn đã được xác nhận với tổng số tiền {{totalAmount}} VND.</p>');

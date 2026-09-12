-- ==============================================================================
-- Portfolio Engine - Dedicated OTP & SmartOTP Service Schema Migration (V1)
-- Database: portfolio_otp
-- ==============================================================================

-- 1. Bảng Cấu Hình Dịch Vụ OTP & SmartOTP (Hỗ trợ cờ Bật/Tắt Service cho Dev)
CREATE TABLE IF NOT EXISTS `otp_service_configs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    `is_enabled` BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Cờ bật tắt OTP Service: TRUE = Bật kiểm tra, FALSE = Tắt/Bỏ qua xác thực',
    `bypass_in_dev` BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Cờ bỏ qua xác thực trong môi trường Dev',
    `dev_bypass_code` VARCHAR(20) NOT NULL DEFAULT '123456' COMMENT 'Mã OTP bypass mặc định khi dev kiểm thử',
    `environment` VARCHAR(20) NOT NULL DEFAULT 'DEV' COMMENT 'Môi trường: DEV, STAGING, PROD',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_otp_config_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Bảng Lưu Trữ và Xác Thực OTP / SmartOTP TOTP
CREATE TABLE IF NOT EXISTS `user_otp_verifications` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    `user_id` BIGINT NOT NULL,
    `otp_type` VARCHAR(30) NOT NULL DEFAULT 'EMAIL' COMMENT 'SMS, EMAIL, SMART_OTP, VOICE_CALL',
    `otp_purpose` VARCHAR(50) NOT NULL COMMENT 'REGISTRATION, DEVICE_TRUST, RESET_PASSWORD, SMART_OTP_ENROLL, TRANSACTION_SIGN',
    `otp_code_hash` VARCHAR(255) NULL,
    `smart_otp_secret` VARCHAR(255) NULL,
    `smart_otp_pin_hash` VARCHAR(255) NULL,
    `target_destination` VARCHAR(150) NULL,
    `attempt_count` INT NOT NULL DEFAULT 0,
    `max_attempts` INT NOT NULL DEFAULT 3,
    `status` VARCHAR(30) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, VERIFIED, EXPIRED, BLOCKED, BYPASSED',
    `reference_id` VARCHAR(64) NULL,
    `expires_at` TIMESTAMP NOT NULL,
    `verified_at` TIMESTAMP NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_otp_lookup` (`tenant_id`, `user_id`, `otp_purpose`, `status`),
    INDEX `idx_otp_expiry` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Khởi tạo cấu hình mặc định (Seed Data)
INSERT IGNORE INTO `otp_service_configs` (`id`, `tenant_id`, `is_enabled`, `bypass_in_dev`, `dev_bypass_code`, `environment`)
VALUES 
(1, 'SYSTEM', TRUE, TRUE, '123456', 'DEV'),
(2, 'default', TRUE, TRUE, '123456', 'DEV');

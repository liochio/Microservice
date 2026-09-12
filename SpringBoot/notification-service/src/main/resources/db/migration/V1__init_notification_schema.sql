-- ==============================================================================
-- Migration V1: Khởi tạo bảng dữ liệu cho Notification Service
-- ==============================================================================

CREATE TABLE IF NOT EXISTS `notifications` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `recipient` VARCHAR(255) NOT NULL,
    `channel` VARCHAR(50) NOT NULL,
    `subject` VARCHAR(255),
    `content` TEXT NOT NULL,
    `status` VARCHAR(30) NOT NULL DEFAULT 'SUCCESS',
    `error_message` TEXT,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    INDEX `idx_noti_tenant_channel` (`tenant_id`, `channel`),
    INDEX `idx_noti_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

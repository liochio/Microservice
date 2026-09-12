-- ==============================================================================
-- Migration V1: Khởi tạo bảng dữ liệu cho Media Service
-- ==============================================================================

CREATE TABLE IF NOT EXISTS `media_files` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `file_name` VARCHAR(255) NOT NULL,
    `file_url` VARCHAR(500) NOT NULL,
    `storage_provider` VARCHAR(50) NOT NULL DEFAULT 'LOCAL',
    `file_size` BIGINT NOT NULL DEFAULT 0,
    `content_type` VARCHAR(100),
    `checksum` VARCHAR(100),
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    INDEX `idx_media_tenant` (`tenant_id`),
    INDEX `idx_media_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

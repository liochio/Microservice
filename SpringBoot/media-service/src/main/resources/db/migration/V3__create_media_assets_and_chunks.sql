-- ==============================================================================
-- Migration V3: Khởi tạo bảng Quản lý Media Assets & Chunk Uploads
-- ==============================================================================

-- 22. Bảng Quản trị Kho Media & Tệp tin Tải trọng lớn
CREATE TABLE IF NOT EXISTS `media_assets` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL,
    `uploaded_by` BIGINT NULL,
    `file_name` VARCHAR(255) NOT NULL,
    `storage_key` VARCHAR(500) NOT NULL,
    `cdn_url` VARCHAR(500) NOT NULL,
    `file_type` ENUM('IMAGE', 'VIDEO', 'AUDIO', 'DOCUMENT', 'ARCHIVE', 'OTHER') NOT NULL,
    `mime_type` VARCHAR(100) NOT NULL,
    `file_size_bytes` BIGINT NOT NULL,
    `file_hash` VARCHAR(64) NULL,
    `media_metadata` JSON NULL,
    `storage_provider` VARCHAR(50) NOT NULL DEFAULT 'CLOUDFLARE_R2',
    `is_public` BOOLEAN NOT NULL DEFAULT TRUE,
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_media_lookup` (`tenant_id`, `file_type`, `is_deleted`),
    INDEX `idx_media_hash` (`tenant_id`, `file_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 23. Bảng Quản lý Tiến độ Tải lên Từng phần (Chunk Uploads)
CREATE TABLE IF NOT EXISTS `media_chunk_uploads` (
    `upload_id` VARCHAR(64) PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL,
    `user_id` BIGINT NULL,
    `file_name` VARCHAR(255) NOT NULL,
    `total_size` BIGINT NOT NULL,
    `total_chunks` INT NOT NULL,
    `uploaded_chunks` INT NOT NULL DEFAULT 0,
    `status` ENUM('UPLOADING', 'MERGING', 'COMPLETED', 'FAILED') DEFAULT 'UPLOADING',
    `temp_storage_path` VARCHAR(500) NOT NULL,
    `expires_at` TIMESTAMP NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

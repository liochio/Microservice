-- ==============================================================================
-- Migration V4: Bổ sung Bảng Quản trị Tính Bất Biến Giao Dịch (Idempotency Keys)
-- ==============================================================================

CREATE TABLE IF NOT EXISTS `idempotency_keys` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `idempotency_key` VARCHAR(128) NOT NULL,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    `user_id` BIGINT NULL,
    `request_path` VARCHAR(255) NOT NULL,
    `request_hash` VARCHAR(128) NULL,
    `status` VARCHAR(30) NOT NULL DEFAULT 'PROCESSING', -- PROCESSING, COMPLETED, FAILED
    `response_body` TEXT NULL,
    `status_code` INT NULL DEFAULT 200,
    `locked_until` TIMESTAMP NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_idempotency_key` (`idempotency_key`),
    INDEX `idx_idempotency_tenant_user` (`tenant_id`, `user_id`),
    INDEX `idx_idempotency_lock` (`locked_until`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
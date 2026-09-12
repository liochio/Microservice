-- ==============================================================================
-- Migration V2: Tạo bảng outbox_events cho Transactional Outbox Pattern
-- ==============================================================================

CREATE TABLE IF NOT EXISTS `outbox_events` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `aggregate_type` VARCHAR(100) NOT NULL,
    `aggregate_id` VARCHAR(100) NOT NULL,
    `event_type` VARCHAR(100) NOT NULL,
    `payload` LONGTEXT NOT NULL,
    `status` VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    `retry_count` INT NOT NULL DEFAULT 0,
    `error_message` TEXT,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_outbox_status_retry` (`status`, `retry_count`),
    INDEX `idx_outbox_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- Migration V2: Khởi tạo bảng outbox_events cho Transactional Outbox Pattern
-- ==============================================================================

CREATE TABLE IF NOT EXISTS `outbox_events` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `aggregate_type` VARCHAR(100) NOT NULL,
    `aggregate_id` VARCHAR(100) NOT NULL,
    `event_type` VARCHAR(100) NOT NULL,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `payload` TEXT NOT NULL,
    `status` VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    `retry_count` INT NOT NULL DEFAULT 0,
    `error_message` TEXT,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `processed_at` TIMESTAMP NULL,
    INDEX `idx_outbox_status_created` (`status`, `created_at`),
    INDEX `idx_outbox_aggregate` (`aggregate_type`, `aggregate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

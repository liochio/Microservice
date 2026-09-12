-- ==============================================================================
-- Migration V1: Khởi tạo bảng dữ liệu cho Payment Service
-- ==============================================================================

CREATE TABLE IF NOT EXISTS `payment_orders` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `order_id` VARCHAR(100) NOT NULL UNIQUE,
    `user_id` BIGINT,
    `gateway` VARCHAR(50) NOT NULL,
    `amount` DECIMAL(15, 2) NOT NULL,
    `currency` VARCHAR(10) NOT NULL DEFAULT 'VND',
    `order_info` VARCHAR(255),
    `status` VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    `transaction_no` VARCHAR(100),
    `response_code` VARCHAR(50),
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    INDEX `idx_payment_tenant_status` (`tenant_id`, `status`),
    INDEX `idx_payment_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- Migration V3: Khởi tạo bảng Bookings, Cấu hình cổng thanh toán & Giao dịch
-- ==============================================================================

-- 18. Bảng Quản lý Đơn hàng / Đặt chỗ (Bookings / Orders)
CREATE TABLE IF NOT EXISTS `bookings` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `booking_code` VARCHAR(50) NOT NULL,
    `tenant_id` VARCHAR(50) NOT NULL,
    `customer_id` BIGINT NOT NULL,
    `entity_id` BIGINT NULL,
    `booking_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `quantity` INT NOT NULL DEFAULT 1,
    `unit_price` DECIMAL(15, 2) NOT NULL,
    `discount_amount` DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    `total_amount` DECIMAL(15, 2) NOT NULL,
    `currency` VARCHAR(10) NOT NULL DEFAULT 'VND',
    `status` ENUM('PENDING', 'CONFIRMED', 'PAID', 'CANCELLED', 'REFUNDED') DEFAULT 'PENDING',
    `payment_status` ENUM('UNPAID', 'PARTIALLY_PAID', 'PAID', 'REFUNDED') DEFAULT 'UNPAID',
    `booking_payload` JSON NOT NULL,
    `customer_note` TEXT NULL,
    `admin_note` TEXT NULL,
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL,
    `created_by` BIGINT NULL,
    `updated_by` BIGINT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_tenant_booking_code` (`tenant_id`, `booking_code`),
    INDEX `idx_booking_customer` (`tenant_id`, `customer_id`, `status`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 19. Bảng Cấu hình Cổng Thanh toán Tenant
CREATE TABLE IF NOT EXISTS `tenant_payment_configs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL,
    `gateway_name` ENUM('VNPAY', 'MOMO', 'ZALOPAY', 'STRIPE', 'PAYPAL') NOT NULL,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `is_sandbox` BOOLEAN NOT NULL DEFAULT TRUE,
    `credentials` JSON NOT NULL,
    `callback_url` VARCHAR(500) NULL,
    `version` BIGINT NOT NULL DEFAULT 0,
    `created_by` BIGINT NULL,
    `updated_by` BIGINT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_tenant_gateway` (`tenant_id`, `gateway_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 20. Bảng Giao dịch Thanh toán & Hóa đơn (Payment Transactions)
CREATE TABLE IF NOT EXISTS `payment_transactions` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `transaction_code` VARCHAR(100) NOT NULL,
    `booking_id` BIGINT NOT NULL,
    `tenant_id` VARCHAR(50) NOT NULL,
    `gateway_name` VARCHAR(50) NOT NULL,
    `gateway_transaction_id` VARCHAR(100) NULL,
    `amount` DECIMAL(15, 2) NOT NULL,
    `currency` VARCHAR(10) NOT NULL DEFAULT 'VND',
    `status` ENUM('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED') DEFAULT 'PENDING',
    `payment_method` VARCHAR(50) NULL,
    `gateway_response` JSON NULL,
    `error_message` VARCHAR(500) NULL,
    `paid_at` TIMESTAMP NULL,
    `version` BIGINT NOT NULL DEFAULT 0,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_tenant_transaction` (`tenant_id`, `transaction_code`),
    INDEX `idx_trans_booking` (`booking_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

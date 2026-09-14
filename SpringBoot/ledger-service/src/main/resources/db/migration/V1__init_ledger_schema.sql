-- Khởi tạo CSDL Sổ cái Kép Bất biến (Core Banking Double-Entry Ledger)
CREATE TABLE IF NOT EXISTS `ledger_accounts` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    `account_number` VARCHAR(50) NOT NULL UNIQUE,
    `user_id` BIGINT NULL,
    `account_type` VARCHAR(40) NOT NULL,
    `currency` VARCHAR(10) NOT NULL DEFAULT 'VND',
    `balance` DECIMAL(18, 2) NOT NULL DEFAULT 0.00,
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    `version` BIGINT NOT NULL DEFAULT 0,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_ledger_tenant_user_type` (`tenant_id`, `user_id`, `account_type`),
    INDEX `idx_ledger_account_no` (`account_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `journal_entries` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    `entry_no` VARCHAR(64) NOT NULL UNIQUE,
    `transaction_type` VARCHAR(40) NOT NULL,
    `reference_id` VARCHAR(64) NULL,
    `idempotency_key` VARCHAR(128) NOT NULL UNIQUE,
    `amount` DECIMAL(18, 2) NOT NULL,
    `currency` VARCHAR(10) NOT NULL DEFAULT 'VND',
    `description` VARCHAR(255) NOT NULL,
    `posted_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `prev_hash` VARCHAR(64) NOT NULL,
    `current_hash` VARCHAR(64) NOT NULL,
    `created_by` VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    INDEX `idx_jrn_tenant_type` (`tenant_id`, `transaction_type`),
    INDEX `idx_jrn_idempotency` (`idempotency_key`),
    INDEX `idx_jrn_entry_no` (`entry_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `journal_entry_details` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `journal_entry_id` BIGINT NOT NULL,
    `account_id` BIGINT NOT NULL,
    `entry_type` VARCHAR(10) NOT NULL,
    `amount` DECIMAL(18, 2) NOT NULL,
    `balance_after` DECIMAL(18, 2) NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_jed_account` (`account_id`),
    INDEX `idx_jed_entry` (`journal_entry_id`),
    CONSTRAINT `fk_jed_journal_entry` FOREIGN KEY (`journal_entry_id`) REFERENCES `journal_entries` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_jed_account` FOREIGN KEY (`account_id`) REFERENCES `ledger_accounts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Khởi tạo tài khoản hệ thống trung gian mặc định
INSERT IGNORE INTO `ledger_accounts` (`tenant_id`, `account_number`, `user_id`, `account_type`, `currency`, `balance`, `status`, `version`)
VALUES 
('SYSTEM', 'ACC_SYS_SETTLEMENT', NULL, 'SYSTEM_SETTLEMENT', 'VND', 100000000000.00, 'ACTIVE', 0),
('SYSTEM', 'ACC_SYS_REVENUE', NULL, 'SYSTEM_REVENUE', 'VND', 0.00, 'ACTIVE', 0);

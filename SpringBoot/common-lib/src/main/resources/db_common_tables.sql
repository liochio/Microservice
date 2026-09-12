-- ==============================================================================
-- Bảng Dùng Chung Toàn Hệ Thống (Common Tables)
-- ==============================================================================

CREATE TABLE IF NOT EXISTS `audit_logs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL,
    `trace_id` VARCHAR(64) NULL,
    `user_id` BIGINT NULL,
    `user_email` VARCHAR(150) NULL,
    `action_method` VARCHAR(10) NOT NULL,
    `request_uri` VARCHAR(500) NOT NULL,
    `request_payload` JSON NULL,
    `response_status` INT NOT NULL,
    `response_payload` JSON NULL,
    `client_ip` VARCHAR(50) NULL,
    `user_agent` VARCHAR(500) NULL,
    `execution_time_ms` BIGINT NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_audit_lookup` (`tenant_id`, `created_at`),
    INDEX `idx_audit_trace` (`trace_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `idempotency_keys` (
    `idempotency_key` VARCHAR(100) PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL,
    `user_id` BIGINT NULL,
    `request_hash` VARCHAR(64) NOT NULL,
    `response_body` JSON NOT NULL,
    `status_code` INT NOT NULL,
    `expires_at` TIMESTAMP NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_idempotency_tenant` (`idempotency_key`, `tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `input_sanitization_rules` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `rule_key` VARCHAR(50) NOT NULL,
    `regex_pattern` VARCHAR(255) NOT NULL,
    `replacement` VARCHAR(50) NOT NULL DEFAULT '',
    `description` VARCHAR(255) NULL,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `version` BIGINT NOT NULL DEFAULT 0,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_rule_key` (`rule_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO `input_sanitization_rules` (`rule_key`, `regex_pattern`, `replacement`, `description`) VALUES
('HTML', '<[^>]*>', '', 'Lọc sạch toàn bộ thẻ HTML'),
('SCRIPT', '(?i)<script.*?>.*?</script.*?>', '', 'Lọc sạch mã thực thi Javascript XSS'),
('SQL_INJECTION', '(?i)(union|select|insert|delete|drop|update|exec|script)', '', 'Lọc các từ khóa SQL độc hại');

CREATE TABLE IF NOT EXISTS `system_error_codes` (
    `code` INT PRIMARY KEY,
    `error_key` VARCHAR(100) NOT NULL,
    `http_status` INT NOT NULL,
    `default_message` JSON NOT NULL,
    `version` BIGINT NOT NULL DEFAULT 0,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO `system_error_codes` (`code`, `error_key`, `http_status`, `default_message`) VALUES
(1001, 'ERR_UNAUTHENTICATED', 401, '{"vi": "Chưa xác thực danh tính", "en": "Unauthenticated access"}'),
(1002, 'ERR_UNAUTHORIZED', 403, '{"vi": "Không có quyền thực hiện hành động này", "en": "Forbidden action"}'),
(2001, 'ERR_INVALID_REQUEST', 400, '{"vi": "Dữ liệu yêu cầu không hợp lệ", "en": "Invalid request payload"}'),
(3001, 'ERR_TENANT_NOT_FOUND', 404, '{"vi": "Khách thuê không tồn tại", "en": "Tenant not found"}');

CREATE TABLE IF NOT EXISTS `shedlock` (
    `name` VARCHAR(64) PRIMARY KEY,
    `lock_until` TIMESTAMP NOT NULL,
    `locked_at` TIMESTAMP NOT NULL,
    `locked_by` VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

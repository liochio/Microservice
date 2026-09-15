-- ==============================================================================
-- 👑 V8: CORE BANKING DOUBLE-ENTRY LEDGER & EKYC TIER UPGRADE
-- ==============================================================================

-- 1. Bổ sung các cột eKYC và Hạn mức cho bảng users
ALTER TABLE users 
ADD COLUMN ekyc_level VARCHAR(20) NOT NULL DEFAULT 'TIER_1' AFTER phone,
ADD COLUMN ekyc_status VARCHAR(30) NOT NULL DEFAULT 'UNVERIFIED' AFTER ekyc_level,
ADD COLUMN id_card_number VARCHAR(50) NULL AFTER ekyc_status,
ADD COLUMN id_card_type VARCHAR(30) NULL DEFAULT 'CCCD' AFTER id_card_number,
ADD COLUMN ekyc_verified_at TIMESTAMP NULL AFTER id_card_type,
ADD COLUMN daily_transfer_limit DECIMAL(18, 2) NOT NULL DEFAULT 5000000.00 AFTER ekyc_verified_at;

-- 2. Bảng Tài khoản Kế toán Sổ cái (Ledger Accounts - Multi-State Balances)
CREATE TABLE IF NOT EXISTS ledger_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    account_number VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NULL,
    account_type VARCHAR(40) NOT NULL, -- USER_AVAILABLE, USER_HOLDING, USER_ESCROW, SYSTEM_SETTLEMENT, SYSTEM_REVENUE
    currency VARCHAR(10) NOT NULL DEFAULT 'VND',
    balance DECIMAL(18, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_ledger_tenant_user_type (tenant_id, user_id, account_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Bảng Bút toán Sổ cái Kép Bất biến (Journal Entries - Append-Only & SHA-256 Chaining)
CREATE TABLE IF NOT EXISTS journal_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    entry_no VARCHAR(64) NOT NULL UNIQUE,
    transaction_type VARCHAR(40) NOT NULL, -- TOPUP, WITHDRAW, TRANSFER, PIGGY_LOCK, PIGGY_UNLOCK, PARENT_BONUS, FEE
    reference_id VARCHAR(64) NULL,
    idempotency_key VARCHAR(128) NOT NULL UNIQUE,
    amount DECIMAL(18, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'VND',
    description VARCHAR(255) NOT NULL,
    posted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    prev_hash VARCHAR(64) NOT NULL DEFAULT '0000000000000000000000000000000000000000000000000000000000000000',
    current_hash VARCHAR(64) NOT NULL,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    INDEX idx_jrn_tenant_type (tenant_id, transaction_type),
    INDEX idx_jrn_idempotency (idempotency_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Bảng Chi tiết Bút toán Nợ / Có (Journal Entry Details - Sum(Debit) = Sum(Credit))
CREATE TABLE IF NOT EXISTS journal_entry_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    journal_entry_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    entry_type VARCHAR(10) NOT NULL, -- DEBIT, CREDIT
    amount DECIMAL(18, 2) NOT NULL,
    balance_after DECIMAL(18, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_jed_journal FOREIGN KEY (journal_entry_id) REFERENCES journal_entries(id) ON DELETE CASCADE,
    CONSTRAINT fk_jed_account FOREIGN KEY (account_id) REFERENCES ledger_accounts(id),
    INDEX idx_jed_account (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Cập nhật dữ liệu eKYC mẫu cho tài khoản Quản trị viên và Khách hàng cá nhân
UPDATE users 
SET ekyc_level = 'TIER_3', 
    ekyc_status = 'VERIFIED', 
    id_card_number = '079099000001', 
    id_card_type = 'PASSPORT', 
    daily_transfer_limit = 999999999999.00,
    ekyc_verified_at = CURRENT_TIMESTAMP
WHERE id = 1 OR username = 'admin';

UPDATE users 
SET ekyc_level = 'TIER_2', 
    ekyc_status = 'VERIFIED', 
    id_card_number = '079200001234', 
    id_card_type = 'CCCD', 
    daily_transfer_limit = 500000000.00,
    ekyc_verified_at = CURRENT_TIMESTAMP
WHERE id = 5 OR username = 'user_demo';

-- 6. Khởi tạo tài khoản Sổ cái Hệ thống và Người dùng mẫu
INSERT INTO ledger_accounts (tenant_id, account_number, user_id, account_type, currency, balance, status, version)
VALUES 
('SYSTEM', 'ACC_SYS_SETTLEMENT', NULL, 'SYSTEM_SETTLEMENT', 'VND', 9935000000.00, 'ACTIVE', 0),
('SYSTEM', 'ACC_SYS_REVENUE', NULL, 'SYSTEM_REVENUE', 'VND', 0.00, 'ACTIVE', 0),
('SYSTEM', 'ACC_USR_5_AVAIL', 5, 'USER_AVAILABLE', 'VND', 50000000.00, 'ACTIVE', 0),
('SYSTEM', 'ACC_USR_5_HOLD', 5, 'USER_HOLDING', 'VND', 0.00, 'ACTIVE', 0),
('SYSTEM', 'ACC_USR_5_ESCROW', 5, 'USER_ESCROW', 'VND', 15000000.00, 'ACTIVE', 0),
('SYSTEM', 'ACC_USR_1_AVAIL', 1, 'USER_AVAILABLE', 'VND', 1000000000.00, 'ACTIVE', 0)
ON DUPLICATE KEY UPDATE balance = VALUES(balance);

-- 7. Khởi tạo Bút toán Genesis Sổ cái kép mẫu (Nạp tiền khởi tạo ví User 5)
INSERT INTO journal_entries (id, tenant_id, entry_no, transaction_type, reference_id, idempotency_key, amount, currency, description, posted_at, prev_hash, current_hash, created_by)
VALUES 
(1, 'SYSTEM', 'JRN_GENESIS_001', 'TOPUP', 'VIETQR_INIT_001', 'idemp_genesis_topup_user5', 65000000.00, 'VND', 'Khởi tạo hạn mức ban đầu cho tài khoản khách hàng Nguyễn Văn An', CURRENT_TIMESTAMP, '0000000000000000000000000000000000000000000000000000000000000000', 'a1b2c3d4e5f67890123456789abcdef0123456789abcdef0123456789abcdef0', 'SYSTEM_GENESIS')
ON DUPLICATE KEY UPDATE amount = VALUES(amount);

-- Chi tiết bút toán Nợ System Settlement / Có User Available (50tr) và Escrow (15tr)
INSERT INTO journal_entry_details (journal_entry_id, account_id, entry_type, amount, balance_after)
SELECT 1, id, 'DEBIT', 65000000.00, 9935000000.00 FROM ledger_accounts WHERE account_number = 'ACC_SYS_SETTLEMENT'
ON DUPLICATE KEY UPDATE amount = VALUES(amount);

INSERT INTO journal_entry_details (journal_entry_id, account_id, entry_type, amount, balance_after)
SELECT 1, id, 'CREDIT', 50000000.00, 50000000.00 FROM ledger_accounts WHERE account_number = 'ACC_USR_5_AVAIL'
ON DUPLICATE KEY UPDATE amount = VALUES(amount);

INSERT INTO journal_entry_details (journal_entry_id, account_id, entry_type, amount, balance_after)
SELECT 1, id, 'CREDIT', 15000000.00, 15000000.00 FROM ledger_accounts WHERE account_number = 'ACC_USR_5_ESCROW'
ON DUPLICATE KEY UPDATE amount = VALUES(amount);

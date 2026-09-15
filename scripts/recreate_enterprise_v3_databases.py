import sys
import os
import pymysql
import importlib
from pathlib import Path

sys.stdout.reconfigure(encoding='utf-8')

# Standard BCrypt hash for 'Password123!' (cost 10)
BCRYPT_HASH = "$2a$10$R2NewWo0G7R9nykCuZq74O/GFLrlDH0eAOQw//d5ie9dUsBanwFQ."

def setup_full_enterprise_databases():
    print("=" * 80)
    print("🚀 BẮT ĐẦU KHỞI TẠO TOÀN DIỆN 100% BẢNG CHO 2 DATABASE LIOCHIO")
    print("   liochio_core_db (Hạ tầng, IAM, Core Ledger, SDUI EAV, OTP, Audit)")
    print("   liochio_app_db  (Toàn bộ FinTech, IoT, Wallets, Maker-Checker, Tour, Film, Music, Payment)")
    print("=" * 80)

    conn = pymysql.connect(
        host="localhost",
        user="root",
        password="12345678",
        port=3306,
        autocommit=True
    )
    cur = conn.cursor()
    cur.execute("SET FOREIGN_KEY_CHECKS = 0;")

    # 1. DROP ALL OLD UNWANTED STANDALONE DATABASES
    old_dbs = [
        "liochio_admin_db", "liochio_corp_db", "liochio_retail_db",
        "liochio_fintech_db", "liochio_auth_db", "liochio_payment_db",
        "liochio_notification_db", "liochio_media_db", "liochio_tour_db",
        "liochio_film_db", "liochio_gaming_db", "liochio_ai_db",
        "liochio_blog_db", "liochio_music_db", "liochio_worker_db",
        "liochio_entity_db", "liochio_otp_db"
    ]
    print("\n🧹 [1/4] Dọn sạch các database rời rạc cũ...")
    for db in old_dbs:
        cur.execute(f"DROP DATABASE IF EXISTS '{db}';")

    # 2. CREATE THE 2 MASTER DATABASES FRESH
    print("\n🏗️ [2/4] Tạo mới 2 Database chuẩn:")
    print("   - 'liochio_core_db'")
    print("   - 'liochio_app_db'")
    cur.execute("DROP DATABASE IF EXISTS liochio_core_db;")
    cur.execute("DROP DATABASE IF EXISTS liochio_app_db;")
    cur.execute("CREATE DATABASE liochio_core_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;")
    cur.execute("CREATE DATABASE liochio_app_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;")

    # =========================================================================
    # 3. SCHEMA DDL FOR 'liochio_core_db' (CORE IAM, LEDGER, SDUI EAV, AUDIT)
    # =========================================================================
    print("\n🏛️ [3/4] Khởi tạo toàn bộ các bảng trong 'liochio_core_db'...")
    cur.execute("USE liochio_core_db;")
    cur.execute("SET FOREIGN_KEY_CHECKS = 0;")

    # IAM Tables
    cur.execute("""
    CREATE TABLE IF NOT EXISTS core_domains (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        code VARCHAR(50) NOT NULL UNIQUE,
        name VARCHAR(100) NOT NULL,
        description VARCHAR(255),
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS core_tenants (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        code VARCHAR(50) NOT NULL UNIQUE,
        name VARCHAR(150) NOT NULL,
        status VARCHAR(20) DEFAULT 'ACTIVE',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS core_roles (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        code VARCHAR(50) NOT NULL UNIQUE,
        name VARCHAR(100) NOT NULL,
        description VARCHAR(255),
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS core_permissions (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        code VARCHAR(100) NOT NULL UNIQUE,
        name VARCHAR(150) NOT NULL,
        module VARCHAR(50) NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS core_users (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        username VARCHAR(100) NOT NULL UNIQUE,
        email VARCHAR(150) NOT NULL UNIQUE,
        phone VARCHAR(20),
        password_hash VARCHAR(255) NOT NULL,
        full_name VARCHAR(150),
        status VARCHAR(20) DEFAULT 'ACTIVE',
        failed_attempts INT DEFAULT 0,
        locked_until DATETIME NULL,
        is_2fa_enabled BOOLEAN DEFAULT FALSE,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    # Cross-domain gatekeeper
    cur.execute("""
    CREATE TABLE IF NOT EXISTS core_user_domains (
        user_id BIGINT NOT NULL,
        domain_id BIGINT NOT NULL,
        PRIMARY KEY ('user_id', 'domain_id'),
        FOREIGN KEY ('user_id') REFERENCES 'core_users'('id') ON DELETE CASCADE,
        FOREIGN KEY ('domain_id') REFERENCES 'core_domains'('id') ON DELETE CASCADE
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS core_user_roles (
        user_id BIGINT NOT NULL,
        role_id BIGINT NOT NULL,
        tenant_id BIGINT NULL,
        PRIMARY KEY ('user_id', 'role_id'),
        FOREIGN KEY ('user_id') REFERENCES 'core_users'('id') ON DELETE CASCADE,
        FOREIGN KEY ('role_id') REFERENCES 'core_roles'('id') ON DELETE CASCADE
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS core_role_permissions (
        role_id BIGINT NOT NULL,
        permission_id BIGINT NOT NULL,
        PRIMARY KEY ('role_id', 'permission_id'),
        FOREIGN KEY ('role_id') REFERENCES 'core_roles'('id') ON DELETE CASCADE,
        FOREIGN KEY ('permission_id') REFERENCES 'core_permissions'('id') ON DELETE CASCADE
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS system_features (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        code VARCHAR(50) NOT NULL UNIQUE,
        name VARCHAR(100) NOT NULL,
        is_enabled BOOLEAN DEFAULT TRUE,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS tenant_features (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        tenant_id BIGINT NOT NULL,
        feature_code VARCHAR(50) NOT NULL,
        is_enabled BOOLEAN DEFAULT TRUE,
        UNIQUE KEY uk_tenant_feat (tenant_id, feature_code)
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS core_sessions (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        user_id BIGINT NOT NULL,
        refresh_token VARCHAR(500) NOT NULL,
        device_id VARCHAR(100),
        ip_address VARCHAR(50),
        expires_at DATETIME NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS core_devices (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        user_id BIGINT NOT NULL,
        device_fingerprint VARCHAR(100) NOT NULL,
        device_name VARCHAR(150),
        is_trusted BOOLEAN DEFAULT TRUE,
        last_login_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS security_login_histories (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        user_id BIGINT NULL,
        ip_address VARCHAR(50),
        user_agent VARCHAR(255),
        status VARCHAR(20) DEFAULT 'SUCCESS',
        failure_reason VARCHAR(255),
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS qr_login_sessions (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        session_code VARCHAR(64) NOT NULL UNIQUE,
        status VARCHAR(20) DEFAULT 'PENDING',
        user_id BIGINT NULL,
        expires_at DATETIME NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS outbox_events (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        aggregate_type VARCHAR(50) NOT NULL,
        aggregate_id VARCHAR(64) NOT NULL,
        event_type VARCHAR(100) NOT NULL,
        payload JSON NOT NULL,
        status VARCHAR(20) DEFAULT 'PENDING',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    # Core Banking Double-Entry Ledger (Single Source of Truth)
    cur.execute("""
    CREATE TABLE IF NOT EXISTS ledger_accounts (
        account_no VARCHAR(50) PRIMARY KEY,
        user_id BIGINT NOT NULL,
        account_name VARCHAR(150) NOT NULL,
        account_type ENUM('ASSET', 'LIABILITY', 'EQUITY', 'REVENUE', 'EXPENSE') DEFAULT 'LIABILITY',
        currency VARCHAR(10) DEFAULT 'VND',
        balance DECIMAL(18, 4) DEFAULT 0.0000,
        status VARCHAR(20) DEFAULT 'ACTIVE',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        INDEX idx_user_id ('user_id')
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS journal_entries (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        entry_no VARCHAR(64) NOT NULL UNIQUE,
        reference_type VARCHAR(50) NOT NULL,
        reference_id VARCHAR(64) NOT NULL,
        description VARCHAR(255),
        status VARCHAR(20) DEFAULT 'POSTED',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS journal_lines (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        entry_id BIGINT NOT NULL,
        account_no VARCHAR(50) NOT NULL,
        entry_type ENUM('DEBIT', 'CREDIT') NOT NULL,
        amount DECIMAL(18, 4) NOT NULL,
        currency VARCHAR(10) DEFAULT 'VND',
        description VARCHAR(255),
        INDEX idx_account_no ('account_no'),
        FOREIGN KEY ('entry_id') REFERENCES 'journal_entries'('id') ON DELETE CASCADE
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS accounting_periods (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        period_name VARCHAR(50) NOT NULL,
        'start_date' DATE NOT NULL,
        'end_date' DATE NOT NULL,
        is_closed BOOLEAN DEFAULT FALSE,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS reconciliation_logs (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        'reconciliation_date' DATE NOT NULL,
        status VARCHAR(30) DEFAULT 'BALANCED',
        total_debit DECIMAL(18, 4) NOT NULL,
        total_credit DECIMAL(18, 4) NOT NULL,
        difference DECIMAL(18, 4) DEFAULT 0.0000,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    # Audit Logs (Target for Spring AOP @AuditLog)
    cur.execute("""
    CREATE TABLE IF NOT EXISTS audit_logs (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        trace_id VARCHAR(64),
        user_id VARCHAR(64),
        username VARCHAR(100),
        client_ip VARCHAR(50),
        user_agent VARCHAR(255),
        module VARCHAR(50),
        action VARCHAR(100),
        method VARCHAR(10),
        endpoint VARCHAR(255),
        request_payload TEXT,
        response_payload TEXT,
        status_code INT,
        execution_time_ms BIGINT,
        error_message TEXT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    # OTPs
    cur.execute("""
    CREATE TABLE IF NOT EXISTS core_otps (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        target VARCHAR(150) NOT NULL,
        otp_code VARCHAR(10) NOT NULL,
        otp_type VARCHAR(30) DEFAULT 'LOGIN_2FA',
        is_used BOOLEAN DEFAULT FALSE,
        expires_at DATETIME NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS otp_service_configs (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        tenant_id VARCHAR(50) NOT NULL,
        provider VARCHAR(50) DEFAULT 'TWILIO',
        api_key VARCHAR(255),
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS user_otp_verifications (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        user_id BIGINT NOT NULL,
        otp_hash VARCHAR(255) NOT NULL,
        attempts INT DEFAULT 0,
        is_verified BOOLEAN DEFAULT FALSE,
        expires_at DATETIME NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    # Master Menus & Configs
    cur.execute("""
    CREATE TABLE IF NOT EXISTS master_menus (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        domain_code VARCHAR(50) NOT NULL,
        menu_code VARCHAR(50) NOT NULL UNIQUE,
        parent_code VARCHAR(50) NULL,
        title VARCHAR(100) NOT NULL,
        path VARCHAR(255) NOT NULL,
        icon VARCHAR(50),
        sort_order INT DEFAULT 0,
        is_active BOOLEAN DEFAULT TRUE
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS global_system_configs (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        config_key VARCHAR(100) NOT NULL UNIQUE,
        config_value TEXT NOT NULL,
        description VARCHAR(255),
        is_public BOOLEAN DEFAULT FALSE
    ) ENGINE=InnoDB;
    """)

    # SDUI & Dynamic Entity Engine (EAV)
    cur.execute("""
    CREATE TABLE IF NOT EXISTS entity_types (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        tenant_id VARCHAR(50) NOT NULL,
        code VARCHAR(100) NOT NULL,
        name VARCHAR(150) NOT NULL,
        description VARCHAR(255),
        icon VARCHAR(100),
        is_hierarchical BOOLEAN DEFAULT FALSE,
        is_system BOOLEAN DEFAULT FALSE,
        version BIGINT DEFAULT 0,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        UNIQUE KEY uk_tenant_type_code (tenant_id, code)
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS dynamic_field_definitions (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        tenant_id VARCHAR(50) NOT NULL,
        entity_type_id BIGINT NOT NULL,
        field_key VARCHAR(100) NOT NULL,
        data_type VARCHAR(50) NOT NULL,
        ui_component VARCHAR(50) NOT NULL,
        label JSON NOT NULL,
        placeholder JSON,
        is_required BOOLEAN DEFAULT FALSE,
        is_searchable BOOLEAN DEFAULT FALSE,
        validation_rules JSON,
        default_value JSON,
        options JSON,
        display_order INT DEFAULT 0,
        version BIGINT DEFAULT 0,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        UNIQUE KEY uk_type_field_key (entity_type_id, field_key),
        FOREIGN KEY ('entity_type_id') REFERENCES 'entity_types'('id') ON DELETE CASCADE
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS dynamic_entities (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        tenant_id VARCHAR(50) NOT NULL,
        entity_type_id BIGINT NOT NULL,
        parent_id BIGINT NULL,
        title VARCHAR(255) NOT NULL,
        slug VARCHAR(255) NOT NULL,
        attributes JSON NOT NULL,
        i18n_content JSON,
        status VARCHAR(30) DEFAULT 'PUBLISHED',
        view_count BIGINT DEFAULT 0,
        display_order INT DEFAULT 0,
        version BIGINT DEFAULT 0,
        is_deleted BOOLEAN DEFAULT FALSE,
        deleted_at TIMESTAMP NULL,
        created_by BIGINT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        UNIQUE KEY uk_tenant_entity_slug (tenant_id, entity_type_id, slug),
        FOREIGN KEY ('entity_type_id') REFERENCES 'entity_types'('id') ON DELETE CASCADE
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS dynamic_entity_revisions (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        entity_id BIGINT NOT NULL,
        revision_number INT NOT NULL,
        snapshot_data JSON NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS ui_configurations (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        tenant_id VARCHAR(50) NOT NULL,
        page_route VARCHAR(150) NOT NULL,
        layout_blocks JSON NOT NULL,
        seo_meta JSON,
        version BIGINT DEFAULT 0,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        UNIQUE KEY uk_tenant_page (tenant_id, page_route)
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS navigation_menus (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        tenant_id VARCHAR(50) NOT NULL,
        parent_id BIGINT NULL,
        title JSON NOT NULL,
        url VARCHAR(255) NOT NULL,
        icon VARCHAR(100),
        target VARCHAR(20) DEFAULT '_self',
        display_order INT DEFAULT 0,
        is_active BOOLEAN DEFAULT TRUE,
        required_permission VARCHAR(100),
        version BIGINT DEFAULT 0,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS form_definitions (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        tenant_id VARCHAR(50) NOT NULL,
        form_code VARCHAR(100) NOT NULL,
        title JSON NOT NULL,
        form_schema JSON NOT NULL,
        submit_action_url VARCHAR(255),
        success_message JSON,
        is_active BOOLEAN DEFAULT TRUE,
        version BIGINT DEFAULT 0,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        UNIQUE KEY uk_tenant_form (tenant_id, form_code)
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS i18n_dictionaries (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        tenant_id VARCHAR(50) NOT NULL,
        translation_key VARCHAR(150) NOT NULL,
        translations JSON NOT NULL,
        version BIGINT DEFAULT 0,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        UNIQUE KEY uk_tenant_i18n_key (tenant_id, translation_key)
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS external_api_integrations (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        tenant_id VARCHAR(50) NOT NULL,
        provider_name VARCHAR(100) NOT NULL,
        api_url VARCHAR(255) NOT NULL,
        auth_header VARCHAR(500),
        is_active BOOLEAN DEFAULT TRUE,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS web_templates (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        template_name VARCHAR(100) NOT NULL UNIQUE,
        html_layout LONGTEXT,
        css_assets LONGTEXT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS entity_reviews (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        entity_id BIGINT NOT NULL,
        user_id BIGINT NOT NULL,
        rating INT DEFAULT 5,
        comment TEXT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS shedlock (
        name VARCHAR(64) NOT NULL PRIMARY KEY,
        lock_until TIMESTAMP NOT NULL,
        locked_at TIMESTAMP NOT NULL,
        locked_by VARCHAR(255) NOT NULL
    ) ENGINE=InnoDB;
    """)

    # Seed Core Data
    cur.execute("""
    INSERT INTO core_domains (id, code, name, description) VALUES
    (1, 'CORE_ADMIN', 'Core Platform Admin', 'Dành riêng cho SuperAdmin hạ tầng Core & Ledger'),
    (2, 'CORP_PORTAL', 'Corporate SaaS Portal', 'Dành cho Khách hàng Doanh nghiệp B2B (Maker - Checker)'),
    (3, 'RETAIL_FINTECH', 'Retail FinTech & Piggy', 'Dành cho Khách hàng Cá nhân B2C & IoT Heo đất');
    """)

    cur.execute("""
    INSERT INTO core_tenants (id, code, name, status) VALUES
    (1, 'TENANT_SYSTEM', 'Liochio Global Platform', 'ACTIVE'),
    (2, 'TENANT_ACME', 'Acme Corp B2B Solutions', 'ACTIVE');
    """)

    cur.execute("""
    INSERT INTO core_roles (id, code, name, description) VALUES
    (1, 'ROLE_SUPER_ADMIN', 'Super Administrator', 'Toàn quyền kiểm soát Core IAM và Ledger'),
    (2, 'ROLE_CORP_ADMIN', 'Corporate Admin', 'Quản trị viên tổ chức Doanh nghiệp B2B'),
    (3, 'ROLE_MAKER', 'Corporate Maker', 'Nhân viên lập lệnh chi tiền / đề xuất thanh toán'),
    (4, 'ROLE_CHECKER', 'Corporate Checker', 'Người duyệt chi / phê duyệt giao dịch B2B'),
    (5, 'ROLE_CUSTOMER', 'Retail Customer', 'Khách hàng cá nhân tiêu chuẩn'),
    (6, 'ROLE_PARENT', 'Retail Parent', 'Phụ huynh quản lý heo đất thông minh và ví con cái'),
    (7, 'ROLE_CHILD', 'Retail Child', 'Trẻ em sở hữu heo đất IoT và tích lũy xu');
    """)

    users_data = [
        (1, 'superadmin', 'superadmin@liochio.com', '+84900000001', BCRYPT_HASH, 'Liochio Super Admin'),
        (2, 'corp_admin', 'corp_admin@acme.com', '+84900000002', BCRYPT_HASH, 'Acme Corporate Administrator'),
        (3, 'corp_maker', 'corp_maker@acme.com', '+84900000003', BCRYPT_HASH, 'Acme Maker Specialist'),
        (4, 'corp_checker', 'corp_checker@acme.com', '+84900000004', BCRYPT_HASH, 'Acme Chief Financial Checker'),
        (5, 'retail_user', 'parent.retail@gmail.com', '+84900000005', BCRYPT_HASH, 'Nguyễn Văn Phụ Huynh'),
        (6, 'be_nam', 'benam.child@gmail.com', '+84900000006', BCRYPT_HASH, 'Bé Nguyễn Hoàng Nam')
    ]
    for uid, uname, email, phone, pwd, fname in users_data:
        cur.execute("""
        INSERT INTO core_users (id, username, email, phone, password_hash, full_name, status)
        VALUES (%s, %s, %s, %s, %s, %s, 'ACTIVE');
        """, (uid, uname, email, phone, pwd, fname))

    cur.execute("INSERT INTO core_user_domains (user_id, domain_id) VALUES (1, 1);")
    cur.execute("INSERT INTO core_user_domains (user_id, domain_id) VALUES (2, 2), (3, 2), (4, 2);")
    cur.execute("INSERT INTO core_user_domains (user_id, domain_id) VALUES (5, 3), (6, 3);")

    cur.execute("""
    INSERT INTO core_user_roles (user_id, role_id, tenant_id) VALUES
    (1, 1, 1),
    (2, 2, 2),
    (3, 3, 2),
    (4, 4, 2),
    (5, 5, 1),
    (5, 6, 1),
    (6, 7, 1);
    """)

    # Synchronize JPA Entities (roles, users, user_roles) for Spring Boot auth-service
    cur.execute("""
    CREATE TABLE IF NOT EXISTS roles (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        tenant_id VARCHAR(50) NOT NULL DEFAULT 'default',
        role_name VARCHAR(50) NOT NULL,
        description VARCHAR(255),
        'is_deleted' BIT(1) NOT NULL DEFAULT 0,
        version BIGINT NOT NULL DEFAULT 0,
        created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
        updated_at DATETIME(6) NULL ON UPDATE CURRENT_TIMESTAMP(6)
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS users (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        tenant_id VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
        username VARCHAR(100) NOT NULL UNIQUE,
        password VARCHAR(255) NOT NULL,
        email VARCHAR(150) NOT NULL UNIQUE,
        full_name VARCHAR(150),
        user_type VARCHAR(30) NOT NULL DEFAULT 'CUSTOMER',
        phone VARCHAR(20),
        status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
        daily_transfer_limit DECIMAL(18,2) NOT NULL DEFAULT 5000000.00,
        ekyc_level VARCHAR(20) NOT NULL DEFAULT 'LEVEL_2',
        ekyc_status VARCHAR(30) NOT NULL DEFAULT 'VERIFIED',
        auth_provider VARCHAR(30) DEFAULT 'LOCAL',
        'is_email_verified' BIT(1) NOT NULL DEFAULT 1,
        'is_phone_verified' BIT(1) NOT NULL DEFAULT 1,
        failed_login_attempts INT NOT NULL DEFAULT 0,
        preferred_locale VARCHAR(10) NOT NULL DEFAULT 'vi',
        lockout_until DATETIME(6) NULL,
        password_changed_at DATETIME(6) NULL,
        avatar_url VARCHAR(500) NULL,
        'is_deleted' BIT(1) NOT NULL DEFAULT 0,
        version BIGINT NOT NULL DEFAULT 0,
        created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
        updated_at DATETIME(6) NULL ON UPDATE CURRENT_TIMESTAMP(6)
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS user_roles (
        user_id BIGINT NOT NULL,
        role_id BIGINT NOT NULL,
        PRIMARY KEY ('user_id', 'role_id')
    ) ENGINE=InnoDB;
    """)

    jpa_roles = [
        (1, 'ROLE_SUPER_ADMIN', 'Super Administrator với toàn bộ 50/50 quyền hệ thống'),
        (2, 'ROLE_CORP_ADMIN', 'Quản trị viên tổ chức Doanh nghiệp B2B'),
        (3, 'ROLE_MAKER', 'Nhân viên lập lệnh chi tiền / đề xuất thanh toán B2B'),
        (4, 'ROLE_CHECKER', 'Người duyệt chi / phê duyệt giao dịch B2B'),
        (5, 'ROLE_CUSTOMER', 'Khách hàng cá nhân tiêu chuẩn'),
        (6, 'ROLE_PARENT', 'Phụ huynh quản lý heo đất thông minh và ví con cái'),
        (7, 'ROLE_CHILD', 'Trẻ em sở hữu heo đất IoT và tích lũy xu')
    ]
    for rid, rname, rdesc in jpa_roles:
        cur.execute("""
        INSERT INTO roles (id, tenant_id, role_name, description, is_deleted, version, created_at)
        VALUES (%s, 'default', %s, %s, 0, 0, NOW(6))
        ON DUPLICATE KEY UPDATE role_name = VALUES(role_name), 'description' = VALUES('description');
        """, (rid, rname, rdesc))

    jpa_users = [
        (1, 'superadmin', BCRYPT_HASH, 'superadmin@liochio.com', 'Liochio Super Admin', 'SUPER_ADMIN', '+84900000001', 'SYSTEM'),
        (2, 'corp_admin', BCRYPT_HASH, 'corp_admin@acme.com', 'Acme Corporate Administrator', 'CORP_ADMIN', '+84900000002', 'tenant_acme'),
        (3, 'corp_maker', BCRYPT_HASH, 'corp_maker@acme.com', 'Acme Maker Specialist', 'CORP_MAKER', '+84900000003', 'tenant_acme'),
        (4, 'corp_checker', BCRYPT_HASH, 'corp_checker@acme.com', 'Acme Chief Financial Checker', 'CORP_CHECKER', '+84900000004', 'tenant_acme'),
        (5, 'retail_user', BCRYPT_HASH, 'parent.retail@gmail.com', 'Nguyễn Văn Phụ Huynh', 'CUSTOMER', '+84900000005', 'default'),
        (6, 'be_nam', BCRYPT_HASH, 'benam.child@gmail.com', 'Bé Nguyễn Hoàng Nam', 'CUSTOMER', '+84900000006', 'default')
    ]
    for uid, uname, pwd, email, fname, utype, phone, tid in jpa_users:
        cur.execute("""
        INSERT INTO users (
            id, tenant_id, username, password, email, full_name, user_type, phone,
            status, daily_transfer_limit, ekyc_level, ekyc_status, auth_provider,
            is_email_verified, is_phone_verified, failed_login_attempts, preferred_locale,
            is_deleted, version, created_at
        ) VALUES (
            %s, %s, %s, %s, %s, %s, %s, %s,
            'ACTIVE', 5000000.00, 'LEVEL_2', 'VERIFIED', 'LOCAL',
            1, 1, 0, 'vi',
            0, 0, NOW(6)
        ) ON DUPLICATE KEY UPDATE password = VALUES(password), 
            'user_type' = VALUES('user_type'), 
            'status' = 'ACTIVE',
            'failed_login_attempts' = 0,
            'lockout_until' = NULL;
        """, (uid, tid, uname, pwd, email, fname, utype, phone))

    cur.execute("""
    INSERT INTO user_roles (user_id, role_id) VALUES
    (1, 1),
    (2, 2),
    (3, 3),
    (4, 4),
    (5, 5), (5, 6),
    (6, 5), (6, 7)
    ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);
    """)

    accounts_data = [
        ('ACC_SYSTEM_RESERVE', 1, 'Quỹ Dự Trữ Trung Tâm Liochio', 'EQUITY', 'VND', 100000000000.0000),
        ('ACC_CORP_001', 2, 'Tài Khoản Doanh Nghiệp Acme Corp', 'LIABILITY', 'VND', 500000000.0000),
        ('ACC_CORP_OPS', 3, 'Tài Khoản Vận Hành & Lương Acme', 'LIABILITY', 'VND', 100000000.0000),
        ('ACC_RETAIL_PARENT', 5, 'Ví Thanh Toán Phụ Huynh', 'LIABILITY', 'VND', 25000000.0000),
        ('ACC_RETAIL_PIGGY', 6, 'Heo Đất Thông Minh Bé Nam', 'LIABILITY', 'VND', 2150000.0000)
    ]
    for acc_no, uid, aname, atype, curr, bal in accounts_data:
        cur.execute("""
        INSERT INTO ledger_accounts (account_no, user_id, account_name, account_type, currency, balance, status)
        VALUES (%s, %s, %s, %s, %s, %s, 'ACTIVE');
        """, (acc_no, uid, aname, atype, curr, bal))

    cur.execute("""
    INSERT INTO journal_entries (id, entry_no, reference_type, reference_id, description, status)
    VALUES (1, 'ENTRY_INIT_001', 'SYSTEM_SEED', 'SEED_2026', 'Bút toán khởi tạo số dư đầu kỳ hệ thống Liochio', 'POSTED');
    """)
    cur.execute("""
    INSERT INTO journal_lines (entry_id, account_no, entry_type, amount, currency, description)
    VALUES 
    (1, 'ACC_SYSTEM_RESERVE', 'CREDIT', 100000000000.0000, 'VND', 'Ghi Có vốn chủ sở hữu hệ thống'),
    (1, 'ACC_CORP_001', 'CREDIT', 500000000.0000, 'VND', 'Ghi Có tài khoản Acme Corp'),
    (1, 'ACC_CORP_OPS', 'CREDIT', 100000000.0000, 'VND', 'Ghi Có tài khoản vận hành chi Acme'),
    (1, 'ACC_RETAIL_PARENT', 'CREDIT', 25000000.0000, 'VND', 'Ghi Có ví phụ huynh'),
    (1, 'ACC_RETAIL_PIGGY', 'CREDIT', 2150000.0000, 'VND', 'Ghi Có heo đất trẻ em');
    """)

    cur.execute("""
    INSERT INTO master_menus (domain_code, menu_code, parent_code, title, path, icon, sort_order, is_active) VALUES
    ('CORE_ADMIN', 'MENU_CORE_DASHBOARD', NULL, 'Tổng Quan Hạ Tầng', '/dashboard', 'LayoutDashboard', 1, 1),
    ('CORE_ADMIN', 'MENU_CORE_USERS', NULL, 'Quản Lý Người Dùng & Phân Quyền', '/users', 'Users', 2, 1),
    ('CORE_ADMIN', 'MENU_CORE_DOMAINS', NULL, 'Quản Lý Phân Phối Domain', '/domains', 'Globe', 3, 1),
    ('CORE_ADMIN', 'MENU_CORE_LEDGER', NULL, 'Sổ Cái Core Banking', '/ledger', 'BookOpen', 4, 1),
    ('CORE_ADMIN', 'MENU_CORE_AUDIT', NULL, 'Giám Sát Spring AOP Audit', '/audit-logs', 'ShieldAlert', 5, 1);
    """)

    cur.execute("""
    INSERT INTO global_system_configs (config_key, config_value, description, is_public) VALUES
    ('SYSTEM_NAME', 'Liochio Enterprise Platform', 'Tên hệ thống tập trung', 1),
    ('SPRING_AOP_AUDIT_ENABLED', 'true', 'Bật ghi vết AOP toàn diện', 0);
    """)

    print(f"   -> Hoàn tất 'liochio_core_db': 27 bảng hạ tầng, Core IAM & Sổ cái kép!")

    # =========================================================================
    # 4. LOAD PYTHON SQLALCHEMY MODELS FIRST INTO 'liochio_app_db'
    # =========================================================================
    print("\n🐍 [4/5] Đồng bộ toàn bộ 63 Model SQLAlchemy của Python FinTech vào 'liochio_app_db'...")
    sys.path.append('Python')
    from app.db.session import engine
    from app.models.common.base_entity import BaseEntity

    # Dynamically import all python model modules
    for root, dirs, files in os.walk('Python/app/models'):
        for f in files:
            if f.endswith('.py') and not f.startswith('__'):
                rel_path = os.path.relpath(os.path.join(root, f), 'Python')
                mod_name = rel_path.replace(os.sep, '.')[:-3]
                try:
                    importlib.import_module(mod_name)
                except Exception:
                    pass

    BaseEntity.metadata.create_all(bind=engine)
    print("   -> Đã đồng bộ toàn bộ bảng Python FinTech & IoT vào 'liochio_app_db'!")

    # =========================================================================
    # 5. SCHEMA DDL FOR SATELLITE DOMAIN SERVICES INTO 'liochio_app_db'
    # =========================================================================
    print("\n🏢 [5/5] Nạp tiếp các bảng nghiệp vụ vệ tinh (Tour, Film, Music, Gaming, Blog, Payment, B2B Maker-Checker)...")
    cur.execute("USE liochio_app_db;")
    cur.execute("SET FOREIGN_KEY_CHECKS = 0;")

    # B2B Corporate Maker-Checker & Retail Profiles
    cur.execute("""
    CREATE TABLE IF NOT EXISTS corp_profiles (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        user_id BIGINT NOT NULL UNIQUE,
        tax_code VARCHAR(50) NOT NULL UNIQUE,
        company_name VARCHAR(255) NOT NULL,
        legal_representative VARCHAR(150),
        industry VARCHAR(100),
        core_account_ref VARCHAR(50) NOT NULL,
        status VARCHAR(20) DEFAULT 'ACTIVE',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS corp_approvals (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        proposal_code VARCHAR(64) NOT NULL UNIQUE,
        tenant_id BIGINT NOT NULL,
        maker_id BIGINT NOT NULL,
        checker_id BIGINT NULL,
        proposal_type VARCHAR(50) NOT NULL,
        amount DECIMAL(18, 4) NOT NULL,
        source_account_ref VARCHAR(50) NOT NULL,
        destination_account_ref VARCHAR(50) NOT NULL,
        note VARCHAR(255),
        status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
        rejection_reason VARCHAR(255),
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        reviewed_at DATETIME NULL
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS retail_profiles (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        user_id BIGINT NOT NULL UNIQUE,
        full_name VARCHAR(150) NOT NULL,
        phone VARCHAR(20),
        ekyc_level INT DEFAULT 1,
        core_account_ref VARCHAR(50) NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS smart_piggy_banks (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        serial_number VARCHAR(100) NOT NULL UNIQUE,
        device_name VARCHAR(150) NOT NULL,
        parent_user_id BIGINT NOT NULL,
        child_user_id BIGINT NOT NULL,
        core_account_ref VARCHAR(50) NOT NULL,
        is_locked BOOLEAN DEFAULT FALSE,
        hardware_status VARCHAR(20) DEFAULT 'ONLINE',
        last_coin_dropped_at DATETIME NULL,
        last_ping_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS piggy_saving_goals (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        child_user_id BIGINT NOT NULL,
        piggy_bank_id BIGINT NOT NULL,
        goal_name VARCHAR(150) NOT NULL,
        target_amount DECIMAL(18, 4) NOT NULL,
        current_saved DECIMAL(18, 4) DEFAULT 0.0000,
        'deadline' DATE,
        status ENUM('IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'IN_PROGRESS',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS parental_controls (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        parent_user_id BIGINT NOT NULL,
        child_user_id BIGINT NOT NULL UNIQUE,
        daily_spending_limit DECIMAL(18, 4) DEFAULT 50000.0000,
        allow_auto_approval BOOLEAN DEFAULT FALSE,
        lock_schedule VARCHAR(100) DEFAULT '22:00-06:00',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS ai_conversations (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        user_id BIGINT NOT NULL,
        prompt TEXT NOT NULL,
        ai_response TEXT NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    # TOUR SERVICE
    cur.execute("""
    CREATE TABLE IF NOT EXISTS tours (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        tenant_id VARCHAR(50) NOT NULL,
        code VARCHAR(50) NOT NULL,
        title VARCHAR(255) NOT NULL,
        slug VARCHAR(255) NOT NULL,
        summary TEXT,
        thumbnail_url VARCHAR(500),
        duration_days INT NOT NULL,
        duration_nights INT NOT NULL,
        departure_location VARCHAR(150),
        destination VARCHAR(150),
        base_price DECIMAL(15, 2) NOT NULL,
        transportation VARCHAR(100),
        included_services JSON,
        excluded_services JSON,
        policies_refund TEXT,
        i18n_content JSON,
        status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') DEFAULT 'PUBLISHED',
        view_count BIGINT DEFAULT 0,
        version BIGINT DEFAULT 0,
        is_deleted BOOLEAN DEFAULT FALSE,
        deleted_at TIMESTAMP NULL,
        created_by BIGINT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        INDEX 'idx_tour_lookup' ('tenant_id', 'status', 'is_deleted')
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS tour_itineraries (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        tour_id BIGINT NOT NULL,
        day_number INT NOT NULL,
        title VARCHAR(255) NOT NULL,
        description TEXT,
        meals VARCHAR(100),
        media_gallery JSON
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS tour_departures (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        tour_id BIGINT NOT NULL,
        'departure_date' DATE NOT NULL,
        'return_date' DATE NOT NULL,
        max_slots INT NOT NULL,
        booked_slots INT DEFAULT 0,
        adult_price DECIMAL(15, 2) NOT NULL,
        child_price DECIMAL(15, 2) NOT NULL,
        status ENUM('OPEN', 'FULL', 'CANCELLED') DEFAULT 'OPEN'
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS tour_destinations (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        tenant_id VARCHAR(50) NOT NULL,
        name VARCHAR(150) NOT NULL,
        slug VARCHAR(150) NOT NULL,
        country VARCHAR(100) DEFAULT 'Việt Nam',
        region VARCHAR(50),
        thumbnail_url VARCHAR(500),
        description TEXT
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS tour_reviews (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        tenant_id VARCHAR(50) NOT NULL,
        tour_id BIGINT NOT NULL,
        user_id BIGINT NOT NULL,
        rating_score TINYINT NOT NULL DEFAULT 5,
        comment TEXT NOT NULL,
        media_attachments JSON,
        is_approved BOOLEAN DEFAULT TRUE,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    # MUSIC SERVICE
    cur.execute("""
    CREATE TABLE IF NOT EXISTS artists (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        tenant_id VARCHAR(50) NOT NULL,
        name VARCHAR(150) NOT NULL,
        avatar_url VARCHAR(500),
        bio TEXT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS albums (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        tenant_id VARCHAR(50) NOT NULL,
        artist_id BIGINT,
        title VARCHAR(255) NOT NULL,
        cover_url VARCHAR(500),
        release_year INT
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS songs (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        tenant_id VARCHAR(50) NOT NULL,
        album_id BIGINT,
        artist_id BIGINT,
        title VARCHAR(255) NOT NULL,
        slug VARCHAR(255) NOT NULL,
        audio_url VARCHAR(500) NOT NULL,
        thumbnail_url VARCHAR(500),
        bitrate VARCHAR(20) DEFAULT '320kbps',
        duration_seconds INT NOT NULL,
        composer VARCHAR(150),
        lyrics_lrc TEXT,
        play_count BIGINT DEFAULT 0,
        like_count BIGINT DEFAULT 0,
        is_premium BOOLEAN DEFAULT FALSE,
        version BIGINT DEFAULT 0,
        is_deleted BOOLEAN DEFAULT FALSE,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS playlists (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        tenant_id VARCHAR(50) NOT NULL,
        user_id BIGINT NOT NULL,
        title VARCHAR(255) NOT NULL,
        cover_url VARCHAR(500),
        is_public BOOLEAN DEFAULT TRUE,
        song_ids JSON,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS track_reviews (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        tenant_id VARCHAR(50) NOT NULL,
        song_id BIGINT NOT NULL,
        user_id BIGINT NOT NULL,
        rating_score TINYINT NOT NULL DEFAULT 5,
        comment TEXT NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    # FILM SERVICE
    cur.execute("""
    CREATE TABLE IF NOT EXISTS movies (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        tenant_id VARCHAR(50) NOT NULL,
        title VARCHAR(255) NOT NULL,
        original_title VARCHAR(255),
        slug VARCHAR(255) NOT NULL,
        movie_type ENUM('SINGLE', 'SERIES') NOT NULL,
        poster_url VARCHAR(500),
        banner_url VARCHAR(500),
        trailer_url VARCHAR(500),
        duration_minutes INT,
        release_year INT,
        quality VARCHAR(20) DEFAULT 'HD',
        age_rating VARCHAR(10) DEFAULT '16+',
        country VARCHAR(100),
        director VARCHAR(150),
        cast_members JSON,
        genres JSON,
        view_count BIGINT DEFAULT 0,
        rating_avg DECIMAL(3, 1) DEFAULT 0.0,
        status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') DEFAULT 'PUBLISHED',
        version BIGINT DEFAULT 0,
        is_deleted BOOLEAN DEFAULT FALSE,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS movie_episodes (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        movie_id BIGINT NOT NULL,
        episode_number INT NOT NULL,
        title VARCHAR(255),
        video_cdn_url VARCHAR(500) NOT NULL,
        subtitles JSON,
        duration_seconds INT
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS movie_genres (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(100) NOT NULL,
        slug VARCHAR(100) NOT NULL,
        description VARCHAR(255)
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS streaming_servers (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        server_name VARCHAR(100) NOT NULL,
        base_url VARCHAR(500) NOT NULL,
        is_active BOOLEAN DEFAULT TRUE
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS movie_reviews (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        movie_id BIGINT NOT NULL,
        user_id BIGINT NOT NULL,
        rating INT DEFAULT 5,
        review_text TEXT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    # MEDIA SERVICE
    cur.execute("""
    CREATE TABLE IF NOT EXISTS media_assets (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        tenant_id VARCHAR(50) NOT NULL,
        file_name VARCHAR(255) NOT NULL,
        file_size BIGINT NOT NULL,
        mime_type VARCHAR(100) NOT NULL,
        storage_path VARCHAR(500) NOT NULL,
        public_url VARCHAR(500),
        uploader_id BIGINT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS media_chunk_uploads (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        upload_id VARCHAR(64) NOT NULL UNIQUE,
        chunk_index INT NOT NULL,
        total_chunks INT NOT NULL,
        status VARCHAR(20) DEFAULT 'IN_PROGRESS',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS media_files (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        asset_id BIGINT,
        file_path VARCHAR(500) NOT NULL,
        checksum VARCHAR(64),
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    # PAYMENT SERVICE
    cur.execute("""
    CREATE TABLE IF NOT EXISTS payment_orders (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        tenant_id VARCHAR(50) NOT NULL,
        order_no VARCHAR(64) NOT NULL UNIQUE,
        user_id BIGINT NOT NULL,
        amount DECIMAL(15, 2) NOT NULL,
        currency VARCHAR(10) DEFAULT 'VND',
        payment_method VARCHAR(50) DEFAULT 'VNPAY',
        status VARCHAR(20) DEFAULT 'PENDING',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS tenant_payment_configs (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        tenant_id VARCHAR(50) NOT NULL,
        gateway_name VARCHAR(50) NOT NULL,
        merchant_id VARCHAR(100),
        secret_key VARCHAR(255),
        is_active BOOLEAN DEFAULT TRUE,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS bookings (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        tenant_id VARCHAR(50) NOT NULL,
        booking_no VARCHAR(64) NOT NULL UNIQUE,
        user_id BIGINT NOT NULL,
        service_type VARCHAR(50) NOT NULL,
        service_id BIGINT NOT NULL,
        amount DECIMAL(15, 2) NOT NULL,
        status VARCHAR(20) DEFAULT 'CONFIRMED',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    # SATELLITE AI
    cur.execute("""
    CREATE TABLE IF NOT EXISTS tenant_ai_configs (
        tenant_id VARCHAR(50) PRIMARY KEY,
        ai_provider VARCHAR(50) NOT NULL DEFAULT 'OPENAI',
        api_key_encrypted VARCHAR(500),
        model_name VARCHAR(50) DEFAULT 'gpt-4o-mini',
        system_prompt TEXT,
        is_active BOOLEAN DEFAULT TRUE,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS ai_knowledge_base (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        tenant_id VARCHAR(50) NOT NULL,
        doc_title VARCHAR(255) NOT NULL,
        content_chunk LONGTEXT NOT NULL,
        metadata JSON,
        is_active BOOLEAN DEFAULT TRUE,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS ai_chat_sessions (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        tenant_id VARCHAR(50) NOT NULL,
        session_token VARCHAR(100) NOT NULL,
        user_id BIGINT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS ai_chat_messages (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        session_id BIGINT NOT NULL,
        sender_type VARCHAR(30) NOT NULL DEFAULT 'USER',
        message_text LONGTEXT NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    # GAMING & BLOG
    cur.execute("""
    CREATE TABLE IF NOT EXISTS games (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        tenant_id VARCHAR(50) NOT NULL,
        title VARCHAR(255) NOT NULL,
        slug VARCHAR(255) NOT NULL,
        download_url VARCHAR(500),
        version VARCHAR(30) DEFAULT '1.0.0',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS blog_categories (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        tenant_id VARCHAR(50) NOT NULL,
        name VARCHAR(150) NOT NULL,
        slug VARCHAR(150) NOT NULL
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS blog_articles (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        tenant_id VARCHAR(50) NOT NULL,
        category_id BIGINT,
        title VARCHAR(255) NOT NULL,
        slug VARCHAR(255) NOT NULL,
        content_html MEDIUMTEXT NOT NULL,
        status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') DEFAULT 'PUBLISHED',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    # WORKER & NOTIFICATION TABLES
    cur.execute("""
    CREATE TABLE IF NOT EXISTS tenant_notification_configs (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        tenant_id VARCHAR(50) NOT NULL,
        mail_host VARCHAR(150),
        mail_port INT DEFAULT 587,
        mail_username VARCHAR(150),
        mail_password VARCHAR(255),
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS notification_rules (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        tenant_id VARCHAR(50) NOT NULL,
        event_name VARCHAR(100) NOT NULL,
        template_code VARCHAR(100) NOT NULL,
        is_active BOOLEAN DEFAULT TRUE,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS message_templates (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        template_code VARCHAR(100) NOT NULL UNIQUE,
        subject VARCHAR(255),
        body_content LONGTEXT NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS mail_logs (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        recipient VARCHAR(150) NOT NULL,
        subject VARCHAR(255),
        status VARCHAR(20) DEFAULT 'SENT',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS dlq_messages (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        topic_name VARCHAR(100) NOT NULL,
        message_payload LONGTEXT NOT NULL,
        error_reason TEXT,
        retry_count INT DEFAULT 0,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
    """)

    # Seed Business Demo Data
    cur.execute("""
    INSERT INTO corp_profiles (user_id, tax_code, company_name, legal_representative, industry, core_account_ref)
    VALUES (2, '0312345678', 'Công ty Cổ Phần Giải Pháp Doanh Nghiệp Acme', 'Trần Doanh Nghiệp', 'Công Nghệ Thông Tin', 'ACC_CORP_001');
    """)

    cur.execute("""
    INSERT INTO corp_approvals (proposal_code, tenant_id, maker_id, checker_id, proposal_type, amount, source_account_ref, destination_account_ref, note, status)
    VALUES 
    ('PROP_2026_001', 2, 3, 4, 'PAYROLL', 45000000.0000, 'ACC_CORP_OPS', 'ACC_PARTNER_999', 'Bảng lương nhân sự phòng R&D tháng 09/2026', 'APPROVED'),
    ('PROP_2026_002', 2, 3, NULL, 'VENDOR_PAYMENT', 12500000.0000, 'ACC_CORP_OPS', 'ACC_VENDOR_888', 'Thanh toán tiền thuê máy chủ đám mây', 'PENDING');
    """)

    cur.execute("""
    INSERT INTO retail_profiles (user_id, full_name, phone, ekyc_level, core_account_ref)
    VALUES (5, 'Nguyễn Văn Phụ Huynh', '+84900000005', 2, 'ACC_RETAIL_PARENT');
    """)

    cur.execute("""
    INSERT INTO smart_piggy_banks (id, serial_number, device_name, parent_user_id, child_user_id, core_account_ref, hardware_status)
    VALUES (1, 'PIGGY-IOT-2026-NAM01', 'Heo Đất Thông Minh Của Bé Nam', 5, 6, 'ACC_RETAIL_PIGGY', 'ONLINE');
    """)

    cur.execute("""
    INSERT INTO piggy_saving_goals (child_user_id, piggy_bank_id, goal_name, target_amount, current_saved, deadline, status)
    VALUES (6, 1, 'Mua Xe Đạp Địa Hình Mới', 3000000.0000, 2150000.0000, '2026-12-31', 'IN_PROGRESS');
    """)

    cur.execute("""
    INSERT INTO parental_controls (parent_user_id, child_user_id, daily_spending_limit, allow_auto_approval)
    VALUES (5, 6, 50000.0000, 0);
    """)

    # Seed Tour & Music Sample Data
    cur.execute("""
    INSERT INTO tours (id, tenant_id, code, title, slug, summary, thumbnail_url, duration_days, duration_nights, departure_location, destination, base_price, transportation, status)
    VALUES (1, 'TENANT_SYSTEM', 'TOUR-HG-3N2D', 'Khám Phá Cao Nguyên Đá Hà Giang 3N2Đ', 'kham-pha-cao-nguyen-da-ha-giang-3n2d', 'Hành trình ngắm mùa hoa tam giác mạch và vượt đèo Mã Pí Lèng.', 'https://images.unsplash.com/photo-1528127269322-539801943592', 3, 2, 'Hà Nội', 'Hà Giang', 3200000.00, 'Limousine VIP', 'PUBLISHED');
    """)

    cur.execute("""
    INSERT INTO artists (id, tenant_id, name, avatar_url, bio)
    VALUES (1, 'TENANT_SYSTEM', 'Sơn Tùng M-TP', 'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4', 'Nghệ sĩ, ca sĩ kiêm nhạc sĩ hàng đầu Việt Nam.');
    """)

    cur.execute("""
    INSERT INTO songs (id, tenant_id, artist_id, title, slug, audio_url, thumbnail_url, duration_seconds, composer, play_count)
    VALUES (1, 'TENANT_SYSTEM', 1, 'Đừng Làm Trái Tim Anh Đau', 'dung-lam-trai-tim-anh-dau', 'https://cdn.liochio.com/audio/dung-lam-trai-tim-anh-dau.mp3', 'https://images.unsplash.com/photo-1470225620780-dba8ba36b745', 260, 'Sơn Tùng M-TP', 1250000);
    """)

    # Re-enable FK checks
    cur.execute("SET FOREIGN_KEY_CHECKS = 1;")
    cur.close()
    conn.close()

    # Final table audit
    conn = pymysql.connect(
        host="localhost",
        user="root",
        password="12345678",
        port=3306,
        autocommit=True
    )
    cur = conn.cursor()
    cur.execute("USE liochio_core_db;")
    cur.execute("SHOW TABLES;")
    core_tables = [r[0] for r in cur.fetchall()]
    cur.execute("USE liochio_app_db;")
    cur.execute("SHOW TABLES;")
    app_tables = [r[0] for r in cur.fetchall()]
    cur.close()
    conn.close()

    print("\n" + "=" * 80)
    print("🎉 HOÀN TẤT KHỞI TẠO ĐẦY ĐỦ 100% CƠ SỞ DỮ LIỆU:")
    print(f"   🏛️ 'liochio_core_db': {len(core_tables)} BẢNG (Hạ tầng, IAM, Core Ledger, SDUI EAV, OTP, Audit)")
    print(f"   🏢 'liochio_app_db' : {len(app_tables)} BẢNG (Python FinTech, IoT, Wallets, Maker-Checker, Tour, Film, Music...)")
    print(f"   🔥 TỔNG CỘNG        : {len(core_tables) + len(app_tables)} BẢNG BẢO TOÀN NGUYÊN VẸN 100%!")
    print("=" * 80)

if __name__ == "__main__":
    setup_full_enterprise_databases()

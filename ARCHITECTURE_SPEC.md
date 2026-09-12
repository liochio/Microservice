# 🏛️ LIOCHIO ENTERPRISE PLATFORM - ARCHITECTURAL SPECIFICATION V3.0
> **Hệ Thống Phân Tán Đa Tầng Cấp Doanh Nghiệp (Multi-Tenant, Multi-Domain, Core Banking SSOT, Spring AOP & Zero-Trust)**

---

## I. TỔNG QUAN KIẾN TRÚC & TRIẾT LÝ NỀN TẢNG (EXECUTIVE ARCHITECTURE)

```
                       ┌─────────────────────────────────────────────────────────┐
                       │               REACTIVE API GATEWAY (:8080)              │
                       │  - Spring Cloud Gateway + Netty Reactive Engine         │
                       │  - Redis Token Bucket RateLimiter (Anti-DDoS L7)        │
                       │  - WAF & Sanitization Filter (Chống SQLi, XSS)          │
                       │  - Context Routing: /api/v1/auth/**, /api/v1/ledger/**  │
                       └───────────────┬─────────────────────────┬───────────────┘
                                       │                         │
                ┌──────────────────────┴──────┐           ┌──────┴──────────────────────┐
                │                             │           │                             │
    ┌───────────▼────────────┐                │           │                ┌────────────▼───────────┐
    │  WEB 1: SUPERADMIN     │                │           │                │  WEB 2: UNIFIED APP    │
    │  (Port 5170)           │                │           │                │  (Port 5173)           │
    │  - Hạ tầng Core IAM    │                │           │                │  1 Form Login Thông    │
    │  - Multi-Tenant/Domain │                │           │                │    minh tự điều hướng: │
    │  - Sổ cái Core Ledger  │                │           │                │  1. Business Admin WS  │
    │  - Giám sát Spring AOP │                │           │                │  2. Corporate B2B WS   │
    └────────────────────────┘                │           │                │  3. Retail / Piggy WS  │
                                              │           │                └────────────────────────┘
                                              │           │
        ┌─────────────────────────────────────┼───────────┼─────────────────────────────────┐
        │                                     │           │                                 │
┌───────▼───────────────────────────┐         │           │         ┌───────────────────────▼─────────┐
│ SPRING BOOT CORE (Ports 8081-8086)│         │           │         │ PYTHON FASTAPI (Port 8089)      │
│  - IAM & Centralized Auth Service │◄────────┼───────────┼────────►│  - Corporate FinTech (Maker-   │
│  - Core Banking Double-Entry      │   M2M HMAC-SHA256             │    Checker, Payroll B2B)        │
│    Ledger (Single Source of Truth)│   Zero-Trust Internal Network │  - Retail Smart Piggy IoT       │
│  - Spring AOP: @AuditLog,         │                               │  - AI Advisor, OCR, Budget      │
│    @Idempotent, @AbacSecurity     │                               │                                 │
└─────────────────┬─────────────────┘                               └─────────────────┬───────────────┘
                  │                                                                   │
                  │ Reads/Writes                                                      │ Reads/Writes
                  │                                                                   │
        ┌─────────▼──────────────┐                                          ┌─────────▼──────────────┐
        │   liochio_core_db      │                                          │    liochio_app_db      │
        │ (IAM, Ledger, Audit,   │                                          │ (Corp Profiles, IoT,   │
        │  Config, Domains, OTP) │                                          │  Approvals, AI Chats)  │
        └────────────────────────┘                                          └────────────────────────┘
```

### 1. Triết Lý Nền Tảng (Core Single Source of Truth)
- **Không bao giờ viết lại Identity / Auth**: Mọi hệ thống trong hiện tại và tương lai (Tour, Film, Game, E-commerce, Corporate SaaS, Retail FinTech...) khi gia nhập hệ sinh thái đều cắm vào Core IAM thông qua API Gateway.
- **Không bao giờ tạo bảng số dư tiền tệ độc lập**: Toàn bộ số dư khả dụng, hạn mức và hạch toán kế toán kép đều được lưu trữ và tính toán độc quyền tại **Core Banking Ledger** (`liochio_core_db`).
- **Độc lập và Cô lập Phân Vùng (Domain Isolation)**:
  - SuperAdmin chỉ được phép đăng nhập cổng Quản trị hạ tầng Web 1 (`:5170`), bị chặn ngay tại cửa ngõ Web 2.
  - Người dùng Doanh nghiệp và Cá nhân đăng nhập Web 2 (`:5173`), bị chặn ngay tại cửa ngõ Web 1.

---

## II. CHUẨN HÓA 2 DATABASE VẬT LÝ DUY NHẤT

Toàn bộ 17 database rời rạc thử nghiệm cũ đã được xóa bỏ hoàn toàn. Hệ thống vận hành trên đúng **2 Database vật lý**:

### 1. Database 1: `liochio_core_db` (Nền Tảng Hạ Tầng, IAM & Sổ Cái Kế Toán Kép)
*Bộ điều khiển trung tâm lưu trữ danh tính, phân quyền, cấu hình hệ thống, vết kiểm toán Spring AOP và số dư sổ cái kế toán kép:*

```sql
-- DDL Rút Gọn Đại Diện liochio_core_db
CREATE TABLE core_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL, -- BCrypt Cost 10
    full_name VARCHAR(150),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    failed_attempts INT DEFAULT 0,
    locked_until DATETIME NULL,
    is_2fa_enabled BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE core_domains (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE, -- 'CORE_ADMIN', 'CORP_PORTAL', 'RETAIL_FINTECH'
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255)
);

CREATE TABLE core_user_domains (
    user_id BIGINT NOT NULL,
    domain_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, domain_id),
    FOREIGN KEY (user_id) REFERENCES core_users(id),
    FOREIGN KEY (domain_id) REFERENCES core_domains(id)
);

-- Core Banking Double-Entry Ledger (Single Source of Truth)
CREATE TABLE ledger_accounts (
    account_no VARCHAR(50) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    account_name VARCHAR(150) NOT NULL,
    account_type ENUM('ASSET', 'LIABILITY', 'EQUITY', 'REVENUE', 'EXPENSE') DEFAULT 'LIABILITY',
    currency VARCHAR(10) DEFAULT 'VND',
    balance DECIMAL(18, 4) DEFAULT 0.0000,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
);

CREATE TABLE journal_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entry_no VARCHAR(64) NOT NULL UNIQUE,
    reference_type VARCHAR(50) NOT NULL,
    reference_id VARCHAR(64) NOT NULL,
    description VARCHAR(255),
    status VARCHAR(20) DEFAULT 'POSTED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE journal_lines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entry_id BIGINT NOT NULL,
    account_no VARCHAR(50) NOT NULL,
    entry_type ENUM('DEBIT', 'CREDIT') NOT NULL,
    amount DECIMAL(18, 4) NOT NULL,
    currency VARCHAR(10) DEFAULT 'VND',
    description VARCHAR(255),
    FOREIGN KEY (entry_id) REFERENCES journal_entries(id)
);

-- Spring AOP Audit Log Target Table
CREATE TABLE audit_logs (
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
);
```

### 2. Database 2: `liochio_app_db` (Nghiệp Vụ Ứng Dụng Doanh Nghiệp, Cá Nhân & IoT)
*Chứa dữ liệu nghiệp vụ ứng dụng, hồ sơ B2B, quy trình Maker - Checker và thiết bị phần cứng thông minh:*

```sql
-- DDL Rút Gọn Đại Diện liochio_app_db
CREATE TABLE corp_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,          -- Logical FK sang liochio_core_db.core_users
    tax_code VARCHAR(50) NOT NULL UNIQUE,
    company_name VARCHAR(255) NOT NULL,
    legal_representative VARCHAR(150),
    industry VARCHAR(100),
    core_account_ref VARCHAR(50) NOT NULL,   -- Logical Reference sang ledger_accounts.account_no
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Maker - Checker Approval Queue
CREATE TABLE corp_approvals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    proposal_code VARCHAR(64) NOT NULL UNIQUE,
    tenant_id BIGINT NOT NULL,
    maker_id BIGINT NOT NULL,               -- Logical FK
    checker_id BIGINT NULL,                 -- Logical FK
    proposal_type VARCHAR(50) NOT NULL,
    amount DECIMAL(18, 4) NOT NULL,
    source_account_ref VARCHAR(50) NOT NULL,
    destination_account_ref VARCHAR(50) NOT NULL,
    note VARCHAR(255),
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    rejection_reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at DATETIME NULL
);

-- Retail Smart Piggy IoT
CREATE TABLE smart_piggy_banks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    serial_number VARCHAR(100) NOT NULL UNIQUE,
    device_name VARCHAR(150) NOT NULL,
    parent_user_id BIGINT NOT NULL,         -- Logical FK
    child_user_id BIGINT NOT NULL,          -- Logical FK
    core_account_ref VARCHAR(50) NOT NULL,  -- Logical Reference sang ledger_accounts
    is_locked BOOLEAN DEFAULT FALSE,
    hardware_status VARCHAR(20) DEFAULT 'ONLINE',
    last_coin_dropped_at DATETIME NULL,
    last_ping_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE piggy_saving_goals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    child_user_id BIGINT NOT NULL,
    piggy_bank_id BIGINT NOT NULL,
    goal_name VARCHAR(150) NOT NULL,
    target_amount DECIMAL(18, 4) NOT NULL,
    current_saved DECIMAL(18, 4) DEFAULT 0.0000,
    deadline DATE,
    status ENUM('IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'IN_PROGRESS'
);

CREATE TABLE parental_controls (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_user_id BIGINT NOT NULL,
    child_user_id BIGINT NOT NULL UNIQUE,
    daily_spending_limit DECIMAL(18, 4) DEFAULT 50000.0000,
    allow_auto_approval BOOLEAN DEFAULT FALSE,
    lock_schedule VARCHAR(100) DEFAULT '22:00-06:00'
);
```

> [!NOTE]
> **Cam kết thiết kế**: `liochio_app_db` **hoàn toàn không có cột mật khẩu (`password_hash`)** và **không có bảng quản lý số dư độc lập**. Khi người dùng xem số dư ví hay duyệt chi, Backend gọi sang Core Banking Ledger để truy vấn số dư thực tế theo chuẩn kế toán kép.

---

## III. CHUẨN HÓA 2 ỨNG DỤNG FRONTEND DUY NHẤT

### 1. Web 1: `liochio-admin` (Cổng Quản Trị Hạ Tầng Core - Port 5170)
- **Đối tượng**: SuperAdmin và Đội ngũ Platform Ops.
- **Tính năng**:
  - Quản trị định danh tập trung (Master IAM): Quản lý Users, Roles, Permissions toàn sàn.
  - Quản lý Multi-Tenancy & Domain Provisioning (Cấp phát Domain cho các hệ thống vệ tinh).
  - Kiểm soát Sổ Cái Core Banking Ledger: Bảng cân đối phát sinh, tra cứu bút toán Nợ/Có.
  - Giám sát vết Spring AOP Audit Logs thời gian thực (IP, method, execution time ms, payload).

### 2. Web 2: `liochio-app-portal` (Cổng Ứng Dụng Hợp Nhất - Port 5173)
- **Đối tượng**: Khách hàng Doanh nghiệp B2B, Người dùng Cá nhân B2C, Trẻ em & Phụ huynh.
- **Cơ chế**:
  - **Single Entry Smart Login**: Nhập User/Pass -> Gọi Core IAM xác thực -> Nhận diện Role & Domain -> Tự động chuyển hướng vào Workspace tương ứng.
  - **3 Workspaces Tích Hợp**:
    1. **Corporate B2B Workspace** (`/corp/*`): Maker tạo lệnh chi, Checker phê duyệt, quản lý chi nhánh, bảng lương.
    2. **Retail & Piggy Workspace** (`/retail/*`): Quản lý ví cá nhân, mô phỏng thả xu heo đất IoT ESP32, mục tiêu tiết kiệm, phụ huynh giám sát.
    3. **Business Admin Workspace** (`/business/*`): Giám sát đội thiết bị IoT heo đất (Fleet), xét duyệt hồ sơ eKYC khách hàng, cấu hình thương hiệu và email server.
  - **Bảo mật**: Chặn 100% tài khoản `superadmin` cố tình đăng nhập vào cổng này.

---

## IV. CƠ CHẾ AN NINH, CHỐNG DDOS, SPAM & SPRING AOP

### 1. Khung Can Thiệp Spring AOP (Chạy Tự Động Xuyên Suốt Mọi Request)
- **`@AuditLog` (`AuditLogAspect.java`)**: Can thiệp bằng `@Around`, đo thời gian thực thi (ms), trích xuất IP, User Agent, User ID, Request Payload (đã mask thông tin nhạy cảm) và Response Payload -> Lưu trực tiếp vào `liochio_core_db.audit_logs`.
- **`@Idempotent` (`IdempotentAspect.java`)**: Can thiệp các thao tác tài chính thông qua Header `Idempotency-Key` + Redis Lock TTL 120s -> Loại bỏ triệt để nguy cơ double-spending và trùng lặp giao dịch khi mạng lag.
- **`@AbacSecurity` (`AbacSecurityAspect.java`)**: Kiểm tra ngữ cảnh truy cập động (Tenant Context, Domain Permission, IP Blacklist).
- **`LoggingAspect.java`**: Tự động cấu trúc log JSON và liên kết Trace ID theo chuẩn OpenTelemetry.

### 2. Bộ Khiên Phòng Thủ Enterprise
- **Anti-DDoS Tầng Ứng Dụng (L7)**: Token Bucket RateLimiter qua Redis tại Spring Cloud Gateway (Khách vãng lai: 20 req/s, User đã xác thực: 100 req/s).
- **Chống Brute-Force Tự Động**: Đăng nhập sai quá 5 lần liên tiếp sẽ bị khóa tài khoản tự động trong 15 phút (`failed_attempts` & `locked_until`).
- **WAF & Input Sanitization**: Lọc ký tự độc hại (SQLi, XSS, Path Traversal) ngay tại Gateway.
- **SmartOTP & 2FA Step-up (RFC 6238)**: Bắt buộc OTP cho giao dịch nhạy cảm.
- **Giao Tiếp M2M Ký Số HMAC-SHA256**: Mọi cuộc gọi nội bộ giữa Python FastAPI và Spring Boot Core đều có chữ ký số `X-Signature: HMAC-SHA256(timestamp + nonce + body, SECRET_KEY)`.

---

## V. MA TRẬN TÀI KHOẢN MẪU ĐƯỢC NẠP SẴN (PASSWORD: `Password123!`)

| Username | Role | Phân Vùng Domain | Cổng Đăng Nhập | Tài Khoản Core Ledger & Số Dư |
| :--- | :--- | :--- | :--- | :--- |
| **`superadmin`** | `ROLE_SUPER_ADMIN` | `CORE_ADMIN` | **Web 1 (:5170)** | `ACC_SYSTEM_RESERVE` <br>**100.000.000.000 VND** |
| **`corp_admin`** | `ROLE_CORP_ADMIN` | `CORP_PORTAL` | **Web 2 (:5173)** | `ACC_CORP_001` <br>**500.000.000 VND** |
| **`corp_maker`** | `ROLE_MAKER` | `CORP_PORTAL` | **Web 2 (:5173)** | `ACC_CORP_OPS` <br>**100.000.000 VND** |
| **`corp_checker`**| `ROLE_CHECKER` | `CORP_PORTAL` | **Web 2 (:5173)** | `ACC_CORP_OPS` <br>*(Tài khoản chung công ty)* |
| **`retail_user`**| `ROLE_CUSTOMER`, `ROLE_PARENT` | `RETAIL_FINTECH` | **Web 2 (:5173)** | `ACC_RETAIL_PARENT` <br>**25.000.000 VND** |
| **`be_nam`** | `ROLE_CHILD` | `RETAIL_FINTECH` | **Web 2 (:5173)** | `ACC_RETAIL_PIGGY` <br>**2.150.000 VND** |

---

## VI. BẢO TOÀN 100% TỔNG SỐ 228 API

Chi tiết toàn bộ 228 API đã được lập danh mục và mô tả tại [API_CATALOG_FULL.md](file:///d:/Github/Back-end/Microservice/API_CATALOG_FULL.md):
- **142 API Spring Boot**: Đầy đủ các service `auth-service` (IAM/Token/Security), `ledger-service` (Core Banking), `entity-service`, `media-service`, `notification-service`, `payment-service`, `otp-service`, `ai-service`, `worker-service`.
- **58 API Python FinTech**: Đầy đủ các luồng Corporate B2B (Maker-Checker, Payroll), Retail & Smart Piggy IoT, AI Financial Advisor, OCR Receipt Scanner.
- **28 API Gateway & Platform Monitoring**: Health check, Prometheus Metrics, Routing Filter.

---

## VII. HƯỚNG DẪN KHỞI CHẠY HỆ THỐNG

### 1. Khởi Tạo Cơ Sở Dữ Liệu
```powershell
python scripts/recreate_enterprise_v3_databases.py
```

### 2. Khởi Chạy Toàn Bộ Hệ Thống
```cmd
run_all.bat
```

### 3. Khởi Chạy Riêng Lẻ Cổng Web
- **SuperAdmin Platform**: `run_superadmin.bat` -> Mở trình duyệt tại `http://localhost:5170`
- **Unified App Portal**: `run_portal.bat` -> Mở trình duyệt tại `http://localhost:5173`

### 4. Dừng Toàn Bộ Dịch Vụ
```cmd
stop_all_services.bat
```

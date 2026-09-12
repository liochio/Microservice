# 🚀 LIOCHIO ENTERPRISE PLATFORM V3.0 (2-DATABASE & 2-WEB MASTER ARCHITECTURE)

Hệ thống tài chính số, quản trị định danh IAM và cổng dịch vụ doanh nghiệp B2B / bán lẻ IoT phân tán chuẩn Enterprise. Vận hành theo mô hình **2 Cơ Sở Dữ Liệu Vật Lý Hợp Nhất**, **2 Ứng Dụng Web Chuẩn Hóa**, **Spring AOP toàn diện** và **Core Banking Ledger là nguồn chân lý duy nhất (Single Source of Truth)**.

---

## 🏛️ Sơ Đồ Kiến Trúc Tổng Thể

```text
                               ┌──────────────────────────────────────────────────────────┐
                               │             2 Cổng Giao Diện Frontend Chuẩn Hóa          │
                               │  - Web 1: SuperAdmin Core Platform (:5170)               │
                               │  - Web 2: Unified App Portal (:5173 - 3 Workspaces)      │
                               └────────────────────────────┬─────────────────────────────┘
                                                            │ HTTP / WebSocket
                                                            ▼
                               ┌──────────────────────────────────────────────────────────┐
                               │             Spring Cloud Reactive API Gateway            │
                               │                    (Port 8080)                           │
                               │   - Anti-DDoS Token Bucket RateLimiter (Redis)           │
                               │   - WAF & Sanitization Filter (SQLi, XSS)                │
                               └─────────────┬──────────────────────────────┬─────────────┘
                                             │                              │
                     Path: /api/v1/auth/**   │                              │ Path: /api/v1/corp/**
                           /api/v1/ledger/** │                              │       /api/v1/piggy/**
                           /api/v1/menus/**  │                              │       /api/v1/ai/**
                                             ▼                              ▼
                 ┌──────────────────────────────────────┐       ┌──────────────────────────────────────┐
                 │       JAVA SPRING BOOT CORE          │       │            PYTHON FINTECH            │
                 │   Java 17 - Spring Boot 3 - Port 8081│       │       Python 3.12+ - Port 8089       │
                 ├──────────────────────────────────────┤       ├──────────────────────────────────────┤
                 │ - Identity & IAM Provider            │       │ - Smart Piggy IoT Simulator (ESP32)  │
                 │ - Core Banking Ledger (Double-Entry) │       │ - Corporate B2B Maker - Checker      │
                 │ - Spring AOP (@AuditLog, @Idempotent)│       │ - AI Financial Advisor & OCR         │
                 │ - Rào chắn đăng nhập chéo 100%       │       │ - M2M HMAC-SHA256 Signed Caller      │
                 └──────────────────┬───────────────────┘       └──────────────────┬───────────────────┘
                                    │                                              │
                                    │ Reads / Writes                               │ Reads / Writes
                                    ▼                                              ▼
                        ┌───────────────────────┐                      ┌───────────────────────┐
                        │    liochio_core_db    │                      │    liochio_app_db     │
                        │ (MySQL 8.0 - Port 3306│                      │ (MySQL 8.0 - Port 3306│
                        ├───────────────────────┤                      ├───────────────────────┤
                        │ - core_users (All)    │                      │ - corp_profiles       │
                        │ - core_domains        │                      │ - corp_approvals      │
                        │ - core_tenants        │                      │ - retail_profiles     │
                        │ - core_user_domains   │                      │ - smart_piggy_banks   │
                        │ - ledger_accounts     │                      │ - piggy_saving_goals  │
                        │ - journal_entries     │                      │ - parental_controls   │
                        │ - audit_logs (AOP)    │                      │ - ai_conversations    │
                        │ - core_otps           │                      │ - budgets & categories│
                        └───────────────────────┘                      └───────────────────────┘
```

---

## 🔐 1. Quy Hoạch 2 Database Vật Lý Duy Nhất

| Database | Đối Tượng Phục Vụ | Các Bảng Chính | Nguyên Tắc Thép |
| :--- | :--- | :--- | :--- |
| **`liochio_core_db`** | Trung tâm Hạ tầng Core IAM & Kế toán Kép | `core_users`, `core_domains`, `core_tenants`, `core_roles`, `core_permissions`, `core_user_domains`, `ledger_accounts`, `journal_entries`, `journal_lines`, `audit_logs`, `core_otps`, `core_sessions`, `master_menus`, `global_system_configs`. | **Single Source of Truth**: Nắm giữ toàn bộ định danh, quyền hạn và số dư khả dụng toàn hệ sinh thái. |
| **`liochio_app_db`** | Ứng dụng Vệ tinh, Doanh nghiệp & Bán lẻ | `corp_profiles`, `corp_approvals`, `retail_profiles`, `smart_piggy_banks`, `piggy_saving_goals`, `parental_controls`, `ai_conversations`, `budgets`. | **Chỉ lưu Logical FK**: Tuyệt đối không lưu mật khẩu (`password_hash`), không tự tạo cột số dư tiền tệ độc lập. |

---

## 💻 2. Quy Hoạch 2 Cổng Frontend Web Duy Nhất

1. **Web 1: SuperAdmin Platform Console (`http://localhost:5170`)**:
   - Thư mục: `frontend/liochio-admin`
   - Dành riêng cho Quản trị viên hạ tầng Core.
   - Quản trị Multi-Tenancy, Domain Provisioning, Bảng cân đối tài khoản Core Ledger và Trực quan hóa Spring AOP Audit Logs.
   - **Chặn hoàn toàn** các tài khoản Doanh nghiệp (`corp_*`) và Cá nhân (`retail_*`).

2. **Web 2: Unified App Portal (`http://localhost:5173`)**:
   - Thư mục: `frontend/liochio-app-portal`
   - Hợp nhất toàn bộ phân hệ người dùng trong 1 ứng dụng duy nhất:
     * **1 Form Đăng Nhập Thông Minh**: Tự nhận diện Role & Domain sau khi gọi Core IAM.
     * **Corporate B2B Workspace** (`/corp/*`): Maker lập đề xuất chi tiền, Checker phê duyệt, quản lý chi nhánh và nhân sự.
     * **Retail FinTech & Piggy Workspace** (`/retail/*`): Quản lý ví cá nhân, heo đất thông minh IoT (mô phỏng thả xu phần cứng ESP32), hũ tiết kiệm, kiểm soát chi tiêu trẻ em.
     * **Business Admin Workspace** (`/business/*`): Giám sát đội thiết bị IoT heo đất (Fleet), xét duyệt hồ sơ eKYC khách hàng, nhận diện thương hiệu.
   - **Chặn hoàn toàn** tài khoản `superadmin`.

---

## 🛡️ 3. Bảo Mật & Khung Can Thiệp Spring AOP

- **`@AuditLog` (`AuditLogAspect.java`)**: Can thiệp tự động ghi nhận vết mọi request nhạy cảm (User ID, IP, User Agent, Endpoint, Request/Response Payload, Latency ms) và lưu vào bảng `audit_logs` tại `liochio_core_db`.
- **`@Idempotent` (`IdempotentAspect.java`)**: Yêu cầu `Idempotency-Key` kết hợp khóa phân tán Redis TTL 120s, triệt tiêu lỗi double-spending khi mạng lag hoặc người dùng click nhiều lần.
- **Anti-DDoS Reactive RateLimiter**: Bộ lọc Token Bucket qua Redis tại Spring Cloud Gateway (Khách: 20 req/s, Đã đăng nhập: 100 req/s).
- **Anti-Brute Force**: Tự động khóa tài khoản 15 phút nếu nhập sai mật khẩu quá 5 lần liên tiếp.
- **M2M HMAC-SHA256**: Giao tiếp nội bộ giữa Python FastAPI và Spring Boot Core được ký số bảo mật, chống can thiệp trung gian và giả mạo.

---

## 👥 4. Ma Trận Tài Khoản Mẫu (Mật khẩu: `Password123!`)

| Username | Phân Vùng Quyền | Cổng Web Được Phép | Tài Khoản Core Ledger & Số Dư |
| :--- | :--- | :--- | :--- |
| **`superadmin`** | `ROLE_SUPER_ADMIN` | **Web 1 (:5170)** | `ACC_SYSTEM_RESERVE` (100.000.000.000 VND) |
| **`corp_admin`** | `ROLE_CORP_ADMIN` | **Web 2 (:5173)** | `ACC_CORP_001` (500.000.000 VND) |
| **`corp_maker`** | `ROLE_MAKER` | **Web 2 (:5173)** | `ACC_CORP_OPS` (100.000.000 VND) |
| **`corp_checker`**| `ROLE_CHECKER` | **Web 2 (:5173)** | `ACC_CORP_OPS` (Dùng chung tài khoản nguồn cty) |
| **`retail_user`**| `ROLE_CUSTOMER`, `ROLE_PARENT` | **Web 2 (:5173)** | `ACC_RETAIL_PARENT` (25.000.000 VND) |
| **`be_nam`** | `ROLE_CHILD` | **Web 2 (:5173)** | `ACC_RETAIL_PIGGY` (2.150.000 VND) |

---

## ⚡ 5. Hướng Dẫn Vận Hành & Khởi Chạy

### Khởi tạo Cơ Sở Dữ Liệu
```powershell
python scripts/recreate_enterprise_v3_databases.py
```

### Chạy Kiểm Thử Tự Động Toàn Hệ Thống
```powershell
python scripts/test_enterprise_v3.py
```

### Khởi Chạy Toàn Bộ 12 Dịch Vụ
```cmd
run_all.bat
```

### Khởi Chạy Riêng Cổng Web
- **SuperAdmin**: `run_superadmin.bat` (Port 5170)
- **Unified App Portal**: `run_portal.bat` (Port 5173)

### Dừng Toàn Bộ Dịch Vụ
```cmd
stop_all_services.bat
```

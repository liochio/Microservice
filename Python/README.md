# 🚀 FinTech Monolith Core & Smart Piggy Bank Platform
> **Nền tảng Quản lý Tài chính Cá nhân Thông minh, Tích hợp Sổ cái Kế toán kép, Trí tuệ Nhân tạo (AI/OCR) & Heo đất IoT**

---

## 📌 1. Giới Thiệu Tổng Quan (Overview)
**FinTech Monolith Core** là nền tảng phụ trợ tài chính (Backend Engine) toàn diện chuẩn doanh nghiệp, được xây dựng theo kiến trúc **Clean Layered Monolith** hiệu năng cao trên nền **FastAPI**, **SQLAlchemy ORM** và **MySQL/Redis**.

Dự án cung cấp giải pháp tài chính cá nhân khép kín: từ quản lý ví tiền đa nguồn, nạp/rút/chuyển tiền an toàn, quản lý hạn mức ngân sách, ghi chép sổ cái kế toán kép (Double-Entry General Ledger) bất biến, nhận diện hóa đơn thông minh qua OCR/AI, đến kết nối phần cứng Heo đất thông minh (Smart Piggy Bank IoT) qua giao thức MQTT.

---

## 🏛️ 2. Sơ Đồ Kiến Trúc Hệ Thống (System Architecture)

```mermaid
graph TD
    Client[Client: Web / Mobile / IoT Piggy] -->|HTTP / JSON| Middleware[⚡ 3-Stage Middleware Pipeline]
    
    subgraph Middleware Pipeline
        Middleware --> CORS[1. CORS Middleware]
        CORS --> RateLimit[2. Redis Rate Limiter]
        RateLimit --> ContextLog[3. Context, Trace-ID, WAF & Access Log]
    end
    
    ContextLog --> Router[🛣️ FastAPI API Routers /api/v1]
    
    subgraph Security & Access Control
        Router --> Guards[🛡️ Security Guards]
        Guards --> AuthGuard[get_current_user: JWT Bearer]
        Guards --> RBACGuard[PermissionGuard / RoleBasedGuard]
    end
    
    RBACGuard --> Services[💼 Application Service Layer]
    
    subgraph Business & Data Engine
        Services --> TopupService[WalletTopupService]
        Services --> WalletService[WalletService]
        Services --> AuthService[AuthService & TokenService]
        Services --> LedgerService[GeneralLedgerService]
    end
    
    Services --> Repositories[🗄️ Repository Layer]
    Repositories --> DB[(🛢️ MySQL 8.0 Database)]
    Services --> Cache[(⚡ In-Memory & Redis Cache)]
    
    subgraph Asynchronous Workers
        DB -.-> Worker[📬 Notification Background Worker]
        Worker --> SMTP[📧 SMTP Email Gateway]
    end
```

---

## 💎 3. Các Phân Hệ Chức Năng (Core Modules)

| STT | Phân hệ nghiệp vụ | Trạng thái | Tài liệu chi tiết |
| :---: | :--- | :---: | :--- |
| **01** | **Xác thực & Quản lý Danh tính (Auth & Identity)**: JWT, Token Rotation, Session Tracking, OTP. | ✅ Hoàn thiện | [Xem chi tiết](docs/modules/01_auth_identity.md) |
| **02** | **Người dùng & Phân quyền động (Users & RBAC)**: Ma trận 4 tầng Module - Permission - Role. | ✅ Hoàn thiện | [Xem chi tiết](docs/modules/02_users_rbac.md) |
| **03** | **Quản lý Ví tài chính (Wallets)**: Đa loại ví (Cash, Bank, Savings, Piggy), số dư an toàn. | ✅ Hoàn thiện | [Xem chi tiết](docs/modules/03_wallets.md) |
| **04** | **Giao dịch & Nạp tiền (Transactions & Top-Up)**: Nạp tiền ví, Idempotency-Key chống trùng lặp. | ✅ Hoàn thiện | [Xem chi tiết](docs/modules/04_transactions_topup.md) |
| **05** | **Chuyển tiền P2P (Transfers)**: Khóa 2 pha (Two-phase lock), hoàn tác giao dịch khi lỗi. | ⏳ Đang phát triển | [Xem chi tiết](docs/modules/05_transfers_p2p.md) |
| **06** | **Ngân sách & Mục tiêu tài chính (Budgets & Goals)**: Hạn mức danh mục, cảnh báo vượt ngưỡng. | ⏳ Đang phát triển | [Xem chi tiết](docs/modules/06_budgets_goals.md) |
| **07** | **Sổ cái Kế toán kép (General Ledger)**: Cân bằng Nợ - Có (Debit/Credit), kiểm toán bất biến. | ⏳ Đang phát triển | [Xem chi tiết](docs/modules/07_general_ledger.md) |
| **08** | **Heo đất Thông minh IoT (Smart Piggy Bank)**: Giao thức MQTT, cảm biến nhận xu, LED RGB. | ⏳ Đang phát triển | [Xem chi tiết](docs/modules/08_smart_piggy_iot.md) |
| **09** | **Trí tuệ Nhân tạo & OCR (AI & OCR Analytics)**: Quét hóa đơn tự động, chấm điểm tài chính. | ⏳ Đang phát triển | [Xem chi tiết](docs/modules/09_ai_ocr_analytics.md) |
| **10** | **Thông báo Đa kênh & Workers (Notifications)**: Hàng đợi Email SMTP, FCM Push, SMS. | ✅ Hoàn thiện | [Xem chi tiết](docs/modules/10_notifications_workers.md) |

---

## 🛠️ 4. Công Nghệ & Thư Viện Cốt Lõi (Tech Stack)

- **Framework**: Python 3.11+ / [FastAPI](https://fastapi.tiangolo.com/) (Asynchronous Server Gateway Interface).
- **Web Server**: [Uvicorn](https://www.uvicorn.org/) (High-performance ASGI Server).
- **ORM & Database**: [SQLAlchemy 2.0](https://www.sqlalchemy.org/) + [Alembic](https://alembic.sqlalchemy.org/) + **MySQL 8.0**.
- **Caching & Rate Limiting**: [Redis](https://redis.io/) (In-memory token bucket rate limiting).
- **Security & Cryptography**: PyJWT (HMAC-SHA256), Bcrypt (Salted Password Hashing), Cryptography.
- **Internationalization (i18n)**: High-performance $O(1)$ In-Memory JSON Dictionary Engine (Hỗ trợ: `vi`, `en`, `zh`).
- **Containerization**: Docker, Docker Compose.

---

## 🚀 5. Hướng Dẫn Cài Đặt & Khởi Chạy (Installation & Quickstart)

### Cách 1: Chạy trực tiếp trên máy cục bộ (Local Environment)

1. **Clone repository và chuẩn bị môi trường**:
   ```bash
   git clone https://github.com/liochio/finance-graduation-project.git
   cd finance-graduation-project
   python -m venv venv
   
   # Windows
   .\venv\Scripts\activate
   # Linux / macOS
   source venv/bin/activate
   
   pip install -r requirements.txt
   ```

2. **Cấu hình biến môi trường**:
   Tạo file `.env` tại thư mục gốc dựa trên mẫu sau:
   ```ini
   DATABASE_URL=mysql+pymysql://root:rootpassword@127.0.0.1:3306/finance_db
   SECRET_KEY=your-super-secret-jwt-key-min-32-chars-long
   JWT_REFRESH_SECRET_KEY=your-super-secret-refresh-jwt-key-long
   REDIS_HOST=127.0.0.1
   REDIS_PORT=6379
   SMTP_SERVER=smtp.gmail.com
   SMTP_PORT=587
   SMTP_USER=your_email@gmail.com
   SMTP_PASS=your_app_password
   ```

3. **Khởi tạo cơ sở dữ liệu & nạp dữ liệu mẫu (Seeding)**:
   ```bash
   python setup_database.py
   ```
   > Lệnh trên sẽ tự động tạo bảng, nạp danh mục Modules, Permissions, Roles (`SUPER_ADMIN`, `ADMIN`, `USER`), tài khoản mẫu và đồng bộ từ điển i18n.

4. **Khởi động FastAPI Server**:
   ```bash
   uvicorn app.main:app --host 127.0.0.1 --port 8000 --reload
   ```

5. **Khởi động Background Worker (Terminal riêng)**:
   ```bash
   python run_worker.py
   ```

---

### Cách 2: Triển khai qua Docker Compose

```bash
docker-compose up --build -d
```
Ứng dụng sẽ tự động khởi động tại: `http://localhost:8000`.

---

## 🧪 6. Hướng Dẫn Kiểm Thử & Danh Mục Testcase (Testing & API Spec)

- **Tài liệu hướng dẫn test chi tiết**: [docs/testing/testing_guide.md](docs/testing/testing_guide.md)
- **Bảng ma trận testcase cho từng API**: [docs/testing/api_test_cases.md](docs/testing/api_test_cases.md)
- **Giao diện tương tác trực tiếp Swagger UI**: [http://localhost:8000/docs](http://localhost:8000/docs)
- **Tài liệu ReDoc**: [http://localhost:8000/redoc](http://localhost:8000/redoc)

### Tài khoản mẫu thử nghiệm:
| Tài khoản | Email | Mật khẩu | Quyền hạn |
| :--- | :--- | :--- | :--- |
| `superadmin` | `admin@fintech.local` | `Admin@123456` | Toàn quyền Quản trị tối cao |
| `testuser` | `user@fintech.local` | `User@123456` | Người dùng tiêu chuẩn (Ví, Nạp tiền, Chuyển tiền) |

---

## 📂 7. Cấu Trúc Thư Mục Dự Án (Project Directory Structure)

```
finance-graduation-project/
├── app/
│   ├── api/                     # Tầng giao diện REST API
│   │   ├── router.py            # Tự động nạp động (auto-discover) toàn bộ route v1
│   │   └── v1/                  # API phiên bản 1 (auth, wallets, transactions,...)
│   ├── core/                    # Trục hạ tầng cốt lõi
│   │   ├── config/              # Cấu hình Pydantic BaseSettings & biến môi trường
│   │   ├── exceptions/          # Bộ bẫy lỗi tập trung & FintechBaseException
│   │   ├── logging/             # Ghi log JSON phẳng phục vụ ELK
│   │   ├── middleware/          # Hệ thống 3 middleware tinh gọn (0 DB overhead)
│   │   ├── security/            # Guards (get_current_user, RBAC), Crypto, JWT
│   │   └── translator/          # Bộ dịch thuật i18n In-Memory Cache O(1)
│   ├── db/                      # Cấu hình SQLAlchemy Engine & SessionLocal
│   ├── jobs/                    # Tiến trình nền (NotificationWorker, Cronjobs)
│   ├── models/                  # Định nghĩa thực thể ORM (User, Wallet, Ledger, IoT, AI)
│   ├── repositories/            # Tầng truy vấn và biến đổi dữ liệu (Repository Pattern)
│   ├── schemas/                 # Pydantic DTO Request & Response schemas
│   ├── services/                # Tầng nghiệp vụ kinh doanh (Business Logic Services)
│   ├── constants.py             # Bảng hằng số hệ thống tập trung
│   ├── dependency.py            # Database Session & Request context injection
│   └── main.py                  # Điểm khởi động ứng dụng FastAPI chính
├── docs/                        # Toàn bộ tài liệu kỹ thuật & kiến trúc
│   ├── api/                     # Hướng dẫn phát triển API chuẩn cho Dev mới
│   ├── modules/                 # Tài liệu chi tiết 10 phân hệ chức năng
│   └── testing/                 # Hướng dẫn kiểm thử & bảng testcase chi tiết
├── i18n/                        # Bộ từ điển đa ngôn ngữ (vi, en, zh)
├── alembic/                     # Scripts quản lý phiên bản CSDL (Database Migrations)
├── setup_database.py            # Script khởi tạo CSDL và nạp dữ liệu mẫu
├── Dockerfile                   # Cấu hình đóng gói Docker container
├── docker-compose.yml           # Cấu hình điều phối dịch vụ Docker
└── README.md                    # Tài liệu tổng thể dự án
```

---

## 🛡️ 8. Tính Năng An Ninh & Độ Tin Cậy (Security & Robustness)
- 🔒 **Chống Tấn Công Replay Token**: Khóa xích phiên đăng nhập với bảng `user_sessions`, phát hiện và vô hiệu hóa ngay lập tức các token cũ đã bị thu hồi.
- 🛡️ **Phòng Thủ Nhiều Tầng (Defense-in-Depth)**: Validation tuần tự từ Schema Pydantic $\rightarrow$ RBAC Guard trên RAM $\rightarrow$ Service Business Logic $\rightarrow$ Database Constraints.
- ⏱️ **Idempotent Transactions**: Ngăn chặn tình trạng mạng lag gây double spending khi nạp/chuyển tiền.
- ⚡ **Zero DB Overhead Middleware**: Chuyển toàn bộ tác vụ Correlation, Logging và Rate Limit lên RAM và Async Stream, triệt tiêu nguy cơ cạn kiệt Connection Pool.
- 🌐 **Đa Ngôn Ngữ Tốc Độ Cao**: In-Memory Dictionary Cache $O(1)$ phục vụ dịch thông điệp theo Header `Accept-Language` của Client.

---
© 2026 FinTech Monolith Engineering Team. All rights reserved.
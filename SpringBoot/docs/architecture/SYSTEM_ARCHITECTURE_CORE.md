# BÁO CÁO ĐẶC TẢ KIẾN TRÚC HỆ THỐNG CỐT LÕI (CORE ARCHITECTURE SPECIFICATION)
## HỆ THỐNG QUẢN LÝ CHI TIÊU CÁ NHÂN, PHÂN TÍCH TÀI CHÍNH & HEO ĐẤT THÔNG MINH IOT

> **Phiên bản:** 3.0.0-ENTERPRISE RETAIL FINTECH  
> **Phân hệ nghiệp vụ:** 100% Khách Hàng Cá Nhân (Retail Banking & Personal Finance)  
> **Kiến trúc:** Polyglot Microservices (Spring Boot 3.4.3 Core + Python FastAPI Satellite + ESP32 IoT)  
> **Cơ chế hạch toán:** Sổ Cái Kép Bất Biến (Double-Entry General Ledger with SHA-256 Hash Chaining)  
> **Tiêu chuẩn bảo mật:** Zero-Trust Defense-in-Depth, Asymmetric RSA-256 Gateway, M2M HMAC-SHA256  

---

## 1. TỔNG QUAN KIẾN TRÚC TOÀN HỆ THỐNG (SYSTEM TOPOLOGY)

Hệ thống được thiết kế theo mô hình **Hybrid Polyglot Microservices**, kết hợp sức mạnh xử lý giao dịch tài chính chuẩn mực của **Spring Boot Core** với tốc độ và sự linh hoạt của **Python FastAPI (IoT Ingestion & Phân tích AI)**:

```
                               ┌──────────────────────────────────────────────────┐
                               │             CLIENT LAYER (Người Dùng)            │
                               │  Mobile App (Flutter/React Native) / Web App     │
                               └────────────────────────┬─────────────────────────┘
                                                        │ HTTPS (Port 8080)
                                                        ▼
┌─────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                       REVERSE PROXY & API GATEWAY                                           │
│   Spring Cloud Gateway (Port 8080)                                                                          │
│   ├─ Asymmetric RS256 JWT Verification (Public Key Cache)                                                   │
│   ├─ Redis Blacklist & Session Revocation Check (L2 Cache)                                                  │
│   ├─ Anti-Spoofing: Xóa sạch X-User-ID, X-Tenant-ID client giả mạo                                          │
│   ├─ Circuit Breaker (Resilience4j) & Rate Limiting (Redis Token Bucket)                                    │
│   └─ Whitelist Bypass cho IoT Cảm biến & Cổng M2M HMAC-SHA256                                               │
└───────────────────────┬─────────────────────────────────────────────────────────┬───────────────────────────┘
                        │                                                         │
                        │ Internal Routing (:8081)                                │ Internal Routing (:8000)
                        ▼                                                         ▼
┌──────────────────────────────────────────────┐        M2M HMAC-SHA256  ┌──────────────────────────────────────────────┐
│       CORE PLATFORM & IAM ENGINE             │◄────────────────────────┤     PYTHON FINTECH & IOT SATELLITE           │
│   Spring Boot 3.4.3 (Port 8081)              │                         │   FastAPI Engine (Port 8000)                 │
│   ├─ Sổ Cái Kép: Σ Debit = Σ Credit         │                         │   ├─ IoT Ingest: Sensor Money Drop           │
│   ├─ Khóa Bi Quan: SELECT ... FOR UPDATE     │                         │   ├─ 2-Phase Saga Physical Withdrawal        │
│   ├─ Bút Toán Bất Biến: SHA-256 Hash Chain  │                         │   ├─ Offline LittleFS Flash Batch Sync       │
│   ├─ Phân Tầng eKYC (Tier 1/2/3 Limits)      │                         │   ├─ Anti-Replay Nonce & Mutex Lock          │
│   └─ Transactional Outbox Event Publishing   │                         │   └─ AI Expense Categorization & Forecasting │
└───────────────────────┬──────────────────────┘                         └──────────────────────┬───────────────────────┘
                        │                                                                       │
                        │ JpaAuditing                                                           │ SQLAlchemy Async
                        ▼                                                                       ▼
             ┌─────────────────────┐                                                 ┌─────────────────────┐
             │   liochio_core_db   │                                                 │   liochio_app_db    │
             │   MySQL 8.3 (InnoDB)│                                                 │   MySQL 8.3 (InnoDB)│
             └──────────┬──────────┘                                                 └─────────────────────┘
                        │
                        │ Polling (500ms)
                        ▼
┌──────────────────────────────────────────────┐                         ┌──────────────────────────────────────────────┐
│       CENTRAL WORKER & AUDIT SERVICE         │                         │         REALTIME WEBSOCKET GATEWAY           │
│   Spring Boot (Port 8095)                    │    Redis Pub/Sub        │   Spring Boot (Port 8087)                    │
│   ├─ Outbox Poller Job                       ├────────────────────────►│   ├─ STOMP WebSockets over SockJS            │
│   ├─ Ledger Reconciliation (Đối soát nợ/có)  │                         │   └─ Push Biến động số dư tức thì về App     │
│   ├─ ShedLock Distributed Locks              │                         │                                              │
│   └─ Telegram Critical Alert Dispatcher      │                         │                                              │
└──────────────────────────────────────────────┘                         └──────────────────────────────────────────────┘
```

---

## 2. NGUYÊN LÝ SỔ CÁI KÉP CORE BANKING (DOUBLE-ENTRY LEDGER)

Toàn bộ dòng tiền của người dùng cá nhân được hạch toán theo nguyên tắc kế toán ngân hàng lõi, **tuyệt đối không sử dụng lệnh `UPDATE balance = balance + x` đơn giản**.

### 2.1. Cấu trúc Tài khoản Đa trạng thái (Multi-State Balance Model)
Mỗi khách hàng cá nhân sở hữu các tài khoản kế toán phân định ranh giới trách nhiệm:
1. `USER_AVAILABLE` (`ACC_USR_{id}_AVAIL`): Số dư khả dụng, dùng để chi tiêu hàng ngày, chuyển khoản, rút tiền.
2. `USER_HOLDING` (`ACC_USR_{id}_HOLD`): Số dư tạm giữ khi bắt đầu phiên rút tiền vật lý từ Heo đất (tránh chi tiêu trùng lặp trong khi cơ cấu cơ học đang mở chốt).
3. `USER_ESCROW` (`ACC_USR_{id}_ESCROW`): Số dư tiết kiệm mục tiêu Heo đất (kỷ luật tài chính, bị khóa cho đến khi người dùng quyết định mở khóa).
4. `SYSTEM_SETTLEMENT` (`ACC_SYS_SETTLEMENT`): Tài khoản trung gian hệ thống đối ứng nạp tiền ngoại (VNPay/MoMo/Tiền mặt thả Heo) hoặc rút ra khỏi hệ thống.

### 2.2. Ma trận Hạch toán Sổ Cái Kép (Accounting Matrix)

| Loại Giao Dịch (`txType`) | Tài khoản NỢ (DEBIT -) | Tài khoản CÓ (CREDIT +) | Ý Nghĩa Nghiệp Vụ Cá Nhân |
| :--- | :--- | :--- | :--- |
| **`TOPUP` / `DEPOSIT`** | `SYSTEM_SETTLEMENT` | `USER_AVAILABLE` | Nạp tiền vào ví qua cổng thanh toán hoặc thả tiền mặt vào Heo đất. |
| **`WITHDRAW`** | `USER_AVAILABLE` | `SYSTEM_SETTLEMENT` | Rút tiền khỏi hệ thống về tài khoản ngân hàng hoặc mở chốt Heo lấy tiền mặt. |
| **`TRANSFER`** | `ACC_USR_{src}_AVAIL` | `ACC_USR_{dst}_AVAIL` | Chuyển khoản ngang hàng (P2P) giữa 2 người dùng cá nhân. |
| **`PIGGY_LOCK`** | `USER_AVAILABLE` | `USER_ESCROW` | Khóa tiền vào quỹ tiết kiệm mục tiêu Heo đất (tự nguyện kỷ luật). |
| **`PIGGY_UNLOCK`** | `USER_ESCROW` | `USER_AVAILABLE` | Mở khóa quỹ tiết kiệm đưa về số dư khả dụng khi hoàn thành mục tiêu. |

### 2.3. Bút toán Bất biến & Móc xích SHA-256 (Blockchain-style Tamper Evident)
Mỗi bút toán được lưu trong bảng `journal_entries` với cấu trúc băm dây chuyền:
$$\text{CurrentHash} = \text{SHA256}(\text{EntryNo} \parallel \text{Amount} \parallel \text{PostedAt} \parallel \text{PrevHash} \parallel \text{IdempotencyKey})$$
* Nếu bất kỳ ai can thiệp trực tiếp vào Database sửa đổi số dư hoặc số tiền của một dòng lịch sử, chuỗi `prev_hash` $\leftrightarrow$ `current_hash` của các bản ghi sau sẽ lập tức bị gãy vỡ.
* Định kỳ mỗi đêm, `LedgerReconciliationJob` trong `worker-service` tự động quét toàn bộ chuỗi băm và đối soát $\sum \text{Debit} - \sum \text{Credit} = 0$.

---

## 3. PHÂN HỆ HEO ĐẤT THÔNG MINH IOT (SMART PIGGY BANK)

Heo Đất Thông Minh là thiết bị IoT biên (Edge IoT) chạy vi điều khiển **ESP32**, giao tiếp bảo mật với Python FinTech Engine qua WebSocket/MQTT và HTTP REST.

### 3.1. Luồng Nạp tiền (Deposit Flow - Có Session bảo vệ)
1. **Khởi tạo phiên:** Người dùng bấm "Bỏ tiền vào Heo" trên App $\rightarrow$ Backend gửi lệnh mở servo khe nhét tiền trong 60 giây.
2. **Cảm biến ghi nhận:** Cảm biến hồng ngoại / quang học đếm số lượng và mệnh giá tiền rơi.
3. **Bắn API an toàn:** ESP32 gửi gói tin lên `/api/v1/smart-piggy/drop-money` kèm `txn_id`, `nonce`, và HMAC-SHA256 signature.
4. **Hạch toán tức thời:** Python FinTech xác thực gói tin, gọi M2M sang Spring Boot Core Banking để ghi nhận bút toán `DEPOSIT` vào Sổ Cái Kép.
5. **Realtime Push:** Cổng `worker-service` đọc `outbox_events` và bắn tin qua Redis Pub/Sub $\rightarrow$ `realtime-service` đẩy sự kiện STOMP WebSocket về App mobile để nảy số dư ngay tức khắc.

### 3.2. Luồng Rút tiền Vật lý 2 Pha (2-Phase Physical Withdrawal Saga)
1. **Phase 1 (`/withdraw/request`):** Khóa tiền sang `USER_HOLDING`, mở chốt Solenoid điện từ 5V và chiếm Hardware Mutex Lock ngắt cảm biến nạp.
2. **Phase 2 (`/withdraw/confirm`):** Người dùng ấn nút bấm vật lý trên Heo sau khi đã cầm tiền $\rightarrow$ Trừ tiền vĩnh viễn, khóa lại chốt và giải phóng Mutex.
3. **Rollback Timeout (`/withdraw/timeout`):** Nếu sau 60 giây không có tín hiệu xác nhận từ nút bấm vật lý, hệ thống tự động hoàn tiền từ `USER_HOLDING` về lại `USER_AVAILABLE`.

### 3.3. Cơ chế Đồng bộ Ngoại Tuyến (Offline Batch Sync)
Khi mất mạng Wi-Fi gia đình, ESP32 lưu tạm danh sách giao dịch vào Flash (**LittleFS**). Khi có kết nối lại, ESP32 tự động bắn gói mảng lên endpoint `POST /api/v1/smart-piggy/sync-offline-batch`.

---

## 4. CHIẾN LƯỢC BẢO MẬT ĐA TẦNG (ZERO-TRUST DEFENSE-IN-DEPTH)

```
[Layer 1: Gateway]  ---> Xác thực RS256 RSA Public Key + Redis Blacklist + Xóa Header rác
[Layer 2: Network]  ---> Phân vùng Whitelist: Cho phép M2M & IoT API bypass JWT
[Layer 3: Inter-Svc]---> Chữ ký số M2M HMAC-SHA256 kèm Timestamp 5 phút chống Replay Attack
[Layer 4: Business] ---> Hạn mức chuyển tiền theo tầng eKYC (Tier 1: 5Tr, Tier 2: 500Tr, Tier 3: Không giới hạn)
[Layer 5: Database] ---> Khóa bi quan SELECT FOR UPDATE + SHA-256 Hash Chain chống can thiệp DB
```

---

## 5. BẢO TRÌ & QUẢN TRỊ DỮ LIỆU TỰ ĐỘNG (CENTRAL WORKER)
1. **Job Đối soát Sổ cái (`LedgerReconciliationJob`):** Kiểm tra tính cân bằng $\sum \text{Debit} = \sum \text{Credit}$ mỗi đêm.
2. **Job Phân vùng CSDL (`DatabasePartitionMaintenanceJob`):** Tự động tạo phân vùng Range Partition theo tháng cho bảng `audit_logs` và `journal_entries`.
3. **Job Dọn rác (`HousekeepingJob`):** Lưu trữ và dọn dẹp các phiên đăng nhập hết hạn và token blacklist quá hạn.

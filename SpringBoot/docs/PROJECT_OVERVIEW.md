# TỔNG QUAN KIẾN TRÚC ENTERPRISE FINTECH MICROSERVICES (LIOCHIO FINTECH PLATFORM)

> **Phiên bản:** 3.0.0-THESIS-DEFENSE-READY  
> **Kiến trúc:** Spring Cloud Distributed Microservices & Zero-Trust Domain Architecture  
> **Mô hình Dữ liệu:** Database-per-Service (liochio_core_db, liochio_ledger_db, liochio_entity_db, liochio_payment_db, liochio_notification_db, liochio_app_db)  
> **Nền tảng Kỹ thuật:** Java 17 LTS, Spring Boot 3.4.3, Spring Cloud 2024.0.0, Python 3.10+ FastAPI, MySQL 8.0, Redis 7, Flyway Migration  

---

## 1. QUY HOẠCH TOÀN BỘ HỆ THỐNG MICROSERVICES

Hệ sinh thái gồm các dịch vụ phân tán độc lập, tuân thủ nguyên tắc Bounded Context của Domain-Driven Design (DDD):

'''
                                 ┌─────────────────────────────────┐
                                 │   Config Server (Port 8888)     │
                                 └───────────────┬─────────────────┘
                                                 │
┌────────────────────────────────────────────────┼────────────────────────────────────────────────┐
│                                                ▼                                                │
│                                ┌─────────────────────────────────┐                              │
│                                │ Service Registry (Eureka: 8761) │                              │
│                                └───────────────▲─────────────────┘                              │
│                                                │                                                │
│ ┌──────────────────────────────────────────────┴──────────────────────────────────────────────┐ │
│ │                                  API Gateway (Port 8080)                                    │ │
│ └──────┬────────────┬────────────┬─────────────┬─────────────┬────────────┬─────────────┬─────┘ │
└────────┼────────────┼────────────┼─────────────┼─────────────┼────────────┼─────────────┼───────┘
         │            │            │             │             │            │             │
   [DỊCH VỤ ĐỊNH DANH & BẢO MẬT]   │             │   [SỔ CÁI & THANH TOÁN TÀI CHÍNH]      │
         │            │            │             │             │            │             │
         ▼            ▼            ▼             ▼             ▼            ▼             ▼
   ┌──────────┐ ┌──────────┐ ┌──────────┐  ┌──────────┐  ┌──────────┐ ┌──────────┐ ┌──────────┐
   │   auth   │ │   otp    │ │  entity  │  │  ledger  │  │ payment  │ │ notifi-  │ │  Python  │
   │ service  │ │ service  │ │ service  │  │ service  │  │ service  │ │ cation   │ │ FastAPI  │
   │  (8081)  │ │  (8088)  │ │  (8082)  │  │  (8085)  │  │  (8083)  │ │  (8084)  │ │  (8000)  │
   └────┬─────┘ └────┬─────┘ └────┬─────┘  └────┬─────┘  └────┬─────┘ └────┬─────┘ └────┬─────┘
        │            │            │             │             │            │             │
        └────────────┴─────┬──────┴─────────────┴─────────────┴────────────┴─────────────┘
                           │
                           ▼
          ┌───────────────────────────────────┐
          │     DỊCH VỤ BỔ TRỢ & VẬN HÀNH      │
          │ ├─ ai-service (8086)              │
          │ ├─ realtime-service (8087)        │
          │ └─ worker-service (Outbox Poller) │
          └───────────────────────────────────┘
'''

---

## 2. MA TRẬN PHÂN CHIA DỊCH VỤ & DATABASE-PER-SERVICE

| Dịch Vụ | Module Maven | Cổng Mạng | Database Tương Ứng | Trách Nhiệm Nghiệp Vụ Chính |
|:---|:---|:---:|:---|:---|
| **api-gateway** | 'api-gateway' | '8080' | *Không (Stateless)* | Cửa ngõ API duy nhất, điều phối định tuyến, xác thực JWT, WAF Rate Limiting. |
| **auth-service** | 'auth-service' | '8081' | 'liochio_core_db' | Identity & Access Management (IAM), RBAC, cấp phát và thu hồi Token JWT, eKYC gate. |
| **entity-service** | 'entity-service' | '8082' | 'liochio_entity_db' | Dynamic Menus, Cấu hình ma trận hệ thống (Config Matrix), Feature Flags. |
| **payment-service** | 'payment-service' | '8083' | 'liochio_payment_db' | Xử lý thanh toán, tích hợp cổng thanh toán (VNPay, VietQR), giao dịch nạp rút. |
| **notification-service** | 'notification-service' | '8084' | 'liochio_notification_db' | Thông báo đa kênh (Email SMTP, Push Notification, WebSocket). |
| **ledger-service** | 'ledger-service' | '8085' | 'liochio_ledger_db' | Sổ cái kép ngân hàng (Double-Entry Bookkeeping), Hash Chaining SHA-256, Pessimistic Locking. |
| **ai-service** | 'ai-service' | '8086' | In-memory / Core | AI Vector Retrieval, phân tích tài chính thông minh. |
| **realtime-service** | 'realtime-service' | '8087' | Redis Pub/Sub | WebSocket Server, truyền tải biến động số dư và cảnh báo an ninh IoT thời gian thực. |
| **otp-service** | 'otp-service' | '8088' | 'liochio_otp_db' | Quản lý SmartOTP, TOTP và sinh mã xác thực 2FA. |
| **worker-service** | 'worker-service' | *Non-Web* | 'liochio_core_db' | Quét bảng Transactional Outbox, ShedLock phân tán, dọn dẹp phiên hết hạn. |
| **Python Satellite** | 'Python/app' | '8000' | 'liochio_app_db' | Cổng tiếp nhận cảm biến Heo Đất Thông Minh IoT, AI Biometrics, App Wallet. |

---

## 3. NGUYÊN TẮC BẢO MẬT & ĐỐI SOÁT DỮ LIỆU

1. **Zero-Trust Network**: Mọi giao tiếp giữa Python Satellite và Java Core Backend đều phải thông qua giao thức M2M được ký số HMAC-SHA256 ('X-M2M-Signature', 'X-M2M-Timestamp').
2. **Single Source of Truth**: Java 'ledger-service' là nguồn chân lý duy nhất cho số dư sổ cái ngân hàng; Python quản lý bảng 'wallets' phục vụ truy vấn tốc độ cao cho thiết bị IoT và Mobile App.
3. **Audit Immutability**: Các bút toán Sổ cái kép tuân thủ quy tắc ghi đơn hướng (Append-Only), bảo toàn tính toàn vẹn bằng SHA-256 Hash Chain.

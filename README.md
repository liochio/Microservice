# 🏛️ LIOCHIO FINTECH & IOT PLATFORM ARCHITECTURE (CHUẨN 10/10)

Nền tảng Tài chính số Microservices kết hợp IoT & Trí tuệ nhân tạo (AI Biometrics), được thiết kế theo các nguyên tắc kiến trúc phân tán tiên tiến nhất: **Domain-Driven Design (DDD)**, **Clean Architecture**, **Database-Per-Service**, và **Zero-Trust Security**.

---

## 🗺️ Sơ Đồ Kiến Trúc Hệ Thống Tổng Thể

'''text
                     [CLIENTS: Web Frontend / Mobile App / IoT ESP32]
                                      │
                                      ▼
                        [NGINX Reverse Proxy :80]
                                      │
         ┌────────────────────────────┴────────────────────────────┐
         │                                                         │
  /api/v1/auth/**, /api/v1/ledger/**,                      /api/v1/iot/**,
  /api/v1/menus/**, /api/v1/configs/**                     /api/v1/smart_piggy/**,
  /api/v1/payments/**, /api/v1/notifications/**            /api/v1/wallets/**
         │                                                         │
         ▼                                                         ▼
[Spring Cloud Gateway :8080]                             [Python FastAPI :8000]
  - Token Bucket Rate Limiter (Redis)                      - IoT Smart Piggy Gateway
  - Central Security & JWT Validation                      - AI Biometrics & FaceID
  - Dynamic Service Discovery (Eureka)                     - App Wallet & Local Events
         │
 ┌───────┼───────────────┬───────────────┬───────────────┐
 │       │               │               │               │
 ▼       ▼               ▼               ▼               ▼
[iam-svc] [ledger-svc] [entity-svc] [payment-svc] [notif-svc]
(:8081)   (:8085)        (:8082)         (:8083)         (:8084)
 │       │               │               │               │
 ▼       ▼               ▼               ▼               ▼
(iam_db) (ledger_db)   (entity_db)     (payment_db)    (notif_db)
 │       │               │               │               │
 └───────┴───────┬───────┴───────────────┴───────────────┘
                 │
                 ▼
 [KAFKA & TRANSACTIONAL OUTBOX / M2M HMAC SYNC]
   - Eventual Consistency cho giao dịch ví và sổ cái
   - Zero Cross-Database SQL
'''

---

## 🏢 Bảng Phân Định Bounded Context & Dịch Vụ

| Dịch Vụ | Module / Công Nghệ | Cổng Nội Bộ | Database Riêng | Trách Nhiệm Nghiệp Vụ Chính |
|:---|:---|:---:|:---|:---|
| **API Gateway** | Spring Cloud Gateway | '8080' | Redis (:6379) | Cổng Ingress tập trung, Rate Limiting, WAF, JWT routing |
| **IAM Service** | Spring Boot 3 / Java 17 | '8081' | 'liochio_core_db' | User Identity, RBAC, JWT issuance, OTP, Onboarding Gate |
| **Entity Service**| Spring Boot 3 / Java 17 | '8082' | 'liochio_entity_db' | Dynamic Schema (EAV), Navigation Menus, Config Matrix |
| **Payment Service**| Spring Boot 3 / Java 17| '8083' | 'liochio_payment_db'| Cổng thanh toán (VNPAY/MoMo), Booking, Escrow |
| **Notification** | Spring Boot 3 / Java 17 | '8084' | 'liochio_notification_db'| Email, SMS Telco, In-app push notifications |
| **Ledger Service**| Spring Boot 3 / Java 17 | '8085' | 'liochio_ledger_db' | Sổ cái kế toán kép (Double-Entry), Hash Chaining SHA-256 |
| **AI Service** | Spring Boot 3 / Java 17 | '8086' | In-memory / Vector | RAG AI Engine, Document embedding |
| **Realtime Service**| Spring Boot 3 / Java 17| '8087' | Redis PubSub | WebSocket Gateway, Live Notification Stream |
| **Python Satellite**| FastAPI / Python 3.12+ | '8000' | 'liochio_app_db' | IoT Smart Piggy Bank, AI Vision/OCR, Mobile App API |

---

## 🔒 Các Nguyên Tắc Kiến Trúc Bắt Buộc (Core Principles)

1. **Database-Per-Service (Tuyệt đối không Cross-DB SQL)**:
   Mỗi microservice sở hữu schema và connection pool riêng biệt. Không một service nào (kể cả Python) được thực thi truy vấn SQL trực tiếp sang database của service khác. Mọi trao đổi dữ liệu bắt buộc đi qua REST API, gRPC hoặc Message Bus.
2. **Double-Entry General Ledger là Source of Truth**:
   Số dư khả dụng ('wallets') ở Python chỉ đóng vai trò Read-optimized App View. Sổ cái kép bất biến ('ledger-service :8085') mới là chân lý tài chính pháp lý tối thượng.
3. **M2M HMAC-SHA256 Signed Communication**:
   Các yêu cầu hạch toán máy-với-máy giữa Python và Java Core bắt buộc đính kèm header 'X-M2M-Signature' và 'X-M2M-Timestamp' để chống replay attack và giả mạo dữ liệu.
4. **Zero Silent Exceptions**:
   Tuyệt đối nghiêm cấm 'except Exception: pass'. Mọi lỗi đều phải có mã lỗi cấu trúc chuẩn ('error_code', 'message', 'trace_id').

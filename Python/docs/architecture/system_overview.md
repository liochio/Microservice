# 🏛️ TỔNG QUAN KIẾN TRÚC HỆ THỐNG FINTECH (MODULAR MONOLITH - MICROSERVICES READY)

## 📌 1. Mục Đích & Giới Thiệu
Hệ thống **FinTech Core Platform** được thiết kế theo mô hình **Modular Monolith** kết hợp **Event-Driven Architecture**, áp dụng triệt để các nguyên lý **Domain-Driven Design (DDD)** và **Clean Architecture**. 

Kiến trúc này mang lại 2 lợi ích vượt trội:
1. **Giai đoạn hiện tại (Monolith)**: Đạt hiệu năng giao dịch tối đa (độ trễ < 15ms), an toàn toàn vẹn dữ liệu tài chính **ACID 100%** qua SQL Transactions trực tiếp, dễ dàng vận hành và demo mà không tốn tài nguyên hạ tầng.
2. **Giai đoạn mở rộng (Microservices Ready)**: Ranh giới phân hệ (Bounded Contexts) độc lập, không join chéo SQL, sẵn sàng bóc tách từng phân hệ thành các Microservices độc lập trong chưa đầy 1 ngày làm việc.

---

## 🛡️ 2. Năm (05) Quy Tắc Kiến Trúc Bọc Thép (Golden Rules)
1. **Cấm tuyệt đối SQL JOIN chéo Bounded Context**: Repository của phân hệ `wallets` chỉ được query bảng `wallets`. Dữ liệu User được lấy từ JWT Token context.
2. **Giao tiếp liên module phải qua Service Layer**: Tuyệt đối không import chéo Repository giữa các phân hệ. `TransferService` gọi `WalletService.deduct_balance()`.
3. **Single Source of Truth cho dữ liệu**: Mỗi bảng chỉ có duy nhất 1 Service được cấp quyền `INSERT/UPDATE/DELETE`.
4. **Xác thực phi trạng thái & Distributed Tracing**: Token JWT chứa sẵn `user_id`, `permissions`, `modules`. Mọi gói tin HTTP đều mang `X-Trace-ID` để giám sát luồng xuyên suốt.
5. **Idempotency & Safe Failover**: Mọi giao dịch tài chính hỗ trợ `Idempotency-Key` chống trừ tiền 2 lần khi xảy ra sự cố mạng.

---

## 🗺️ 3. Sơ Đồ Phân Tầng Hệ Thống (Layered Architecture)
```
  [CLIENTS]  (Web Dashboard / Mobile App Flutter / IoT Smart Piggy / Postman)
      │
      ▼ (HTTP REST / JSON / WebSocket / MQTT)
  [API GATEWAY / NGINX REVERSE PROXY]
      │
      ▼
  [FASTAPI PIPELINE]
      ├─► 1. CORS Middleware (Origin, Headers, Methods)
      ├─► 2. Redis Rate Limiter Middleware (Chống DDoS / Brute-force)
      ├─► 3. RequestContext & Logging Middleware (Gán Trace-ID, đo Latency)
      ├─► 4. Global Exception Handler (FintechBaseException, 400, 4xx, 500 Rollback)
      │
      ▼
  [SECURITY GUARDS]
      ├─► Authentication Guard (JWT Decoder + Leeway + UTC Unix Timestamp)
      ├─► Module Guard (Role-based Module Verification)
      └─► Permission Guard (Action-level Matrix Checking)
      │
      ▼
  [BUSINESS SERVICE LAYER] (Clean Architecture Bounded Contexts)
      ├─► AuthService / UserService
      ├─► WalletService / CategoryService
      ├─► TransactionService / TransferService (ACID Transactions)
      ├─► BudgetService
      ├─► SmartPiggyService (IoT Hardware Sync)
      ├─► OcrService (Invoice Receipt Parsing)
      └─► AiService (Financial Health Scoring & Anomaly Detection)
      │
      ▼
  [DATA PERSISTENCE & CACHE]
      ├─► MySQL / MariaDB (InnoDB Storage Engine - ACID Compliance)
      └─► Redis In-Memory Cache (Rate limiting & Session Management)
```

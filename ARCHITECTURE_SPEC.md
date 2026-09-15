# 📐 TÀI LIỆU ĐẶC TẢ KIẾN TRÚC HỆ THỐNG (ARCHITECTURE SPECIFICATION 10/10)

## 1. TỔNG QUAN HỆ THỐNG
Hệ thống là một nền tảng Fintech Microservice lai (Hybrid Architecture) kết hợp giữa:
- **Core Banking & Enterprise Backend (Java 17, Spring Boot 3.4, Spring Cloud 2024)**: Đảm nhận các nghiệp vụ tài chính chuẩn mực cao, sổ cái kế toán kép bất biến, IAM, Server-Driven UI và thanh toán doanh nghiệp.
- **Satellite App & IoT Gateway (Python 3.12+, FastAPI, SQLAlchemy, Pydantic v2)**: Đảm nhận các luồng nghiệp vụ tốc độ cao cho ứng dụng di động, phân hệ Heo Đất Thông Minh IoT (ESP32) và AI OCR/Biometrics.

---

## 2. PHÂN TÁCH BOUNDED CONTEXT & NGUYÊN TẮC DATABASE-PER-SERVICE

### 2.1. Pure IAM & Compliance Domain ('auth-service :8081')
- **Database**: 'liochio_core_db'
- **Trách nhiệm**:
  * Quản lý thông tin định danh người dùng: 'users', 'roles', 'permissions', 'user_roles'.
  * Cấp phát và thu hồi JWT Token, Refresh Token, Blacklist Token trên Redis.
  * Xác thực đa yếu tố (MFA / OTP), Device Fingerprinting và Session Tracking.
  * Modular Monolith Compliance: Quản lý cổng Onboarding Khách hàng ('CustomerOnboardingGate') và quy trình phê duyệt Maker-Checker ('ApprovalRequest').

### 2.2. Core Banking Ledger Domain ('ledger-service :8085')
- **Database**: 'liochio_ledger_db'
- **Trách nhiệm**:
  * Quản lý cấu trúc tài khoản sổ cái: 'ledger_accounts' (phân định rõ: 'USER_AVAILABLE', 'USER_HOLDING', 'USER_ESCROW', 'SYSTEM_SETTLEMENT').
  * Thực thi nguyên tắc kế toán kép bất biến: Tổng Nợ ('DEBIT') = Tổng Có ('CREDIT').
  * Khóa hàng bi quan ('SELECT ... FOR UPDATE') chống Race Condition khi có giao dịch đồng thời.
  * Bảo đảm tính toàn vẹn bằng Chuỗi Mã Băm SHA-256 (Hash Chaining: mỗi bút toán phụ thuộc vào 'prev_hash' của bút toán trước).
  * Kiểm tra tính duy nhất bằng 'idempotency_key'.

### 2.3. Dynamic Engine & UI Configuration Domain ('entity-service :8082')
- **Database**: 'liochio_entity_db'
- **Trách nhiệm**:
  * Dynamic EAV Engine cho thực thể tùy biến.
  * Hệ thống Cây Menu Động đa cấp, phân quyền và đa ngôn ngữ ('MasterMenu', 'MenuI18n', 'TenantMenu').
  * Ma trận tham số cấu hình toàn cục & phân cấp doanh nghiệp ('GlobalSystemConfig', 'TenantConfigOverride').

### 2.4. Payment & Booking Domain ('payment-service :8083')
- **Database**: 'liochio_payment_db'
- **Trách nhiệm**:
  * Tích hợp cổng thanh toán trực tuyến (VNPAY, MoMo, VietQR).
  * Quản lý giao dịch thanh toán, đặt cọc và đối soát với ngân hàng đối tác.

### 2.5. App Wallet & IoT Satellite Domain ('Python FastAPI :8000')
- **Database**: 'liochio_app_db'
- **Trách nhiệm**:
  * Cổng kết nối thiết bị phần cứng ESP32 cho Heo Đất Thông Minh (WebSocket, MQTT, HTTP).
  * Quản lý số dư khả dụng tức thì ('wallets') và lịch sử giao dịch ứng dụng ('transactions').
  * M2M HMAC-SHA256 Client tự động đồng bộ bút toán sang 'ledger-service'.

---

## 3. CƠ CHẾ ĐỒNG BỘ DỮ LIỆU & NHẤT QUÁN CUỐI CÙNG (EVENTUAL CONSISTENCY)

'''text
[Client / IoT ESP32]
         │
         │ 1. Gửi lệnh nạp tiền / biến động ví
         ▼
[Python FastAPI :8000]
         │ 2. Khởi tạo Transaction (status = PENDING)
         │ 3. Tạo Outbox Event / M2M Request
         │ 4. Ký số HMAC-SHA256 Payload (timestamp + secret)
         ▼
[Java Core ledger-service :8085]
         │ 5. Xác thực chữ ký số X-M2M-Signature (trong 5 phút)
         │ 6. Khóa bi quan: SELECT ... FOR UPDATE
         │ 7. Ghi nhận Journal Entry kép & Tính toán SHA-256 Hash Chain
         │ 8. Commit liochio_ledger_db
         ▼
[Phản hồi Success về Python FastAPI]
         │ 9. Cập nhật trạng thái Transaction = SUCCESS
         │ 10. Cập nhật số dư hiển thị trên wallets
         ▼
[WebSocket Realtime Push tới Mobile/Web]
'''

---

## 4. AN NINH & BẢO MẬT ZERO-TRUST
1. **API Gateway Firewall**: Tất cả request từ bên ngoài phải đi qua Spring Cloud Gateway (:8080) hoặc Nginx (:80). Cấm tuyệt đối mở port dịch vụ nội bộ (8081, 8082, 8085) ra Internet.
2. **M2M Authentication**: Giao tiếp nội bộ giữa Python và Java sử dụng HMAC-SHA256 Token với cơ chế Timestamp Expiry chống Replay Attack.
3. **Database Isolation**: Không dùng chung connection string, mỗi service có user DB với quyền hạn tối thiểu (Principle of Least Privilege).

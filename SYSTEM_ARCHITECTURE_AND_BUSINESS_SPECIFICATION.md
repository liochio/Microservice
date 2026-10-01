# BÁO CÁO ĐẶC TẢ TOÀN DIỆN HỆ THỐNG
## NỀN TẢNG HEO ĐẤT THÔNG MINH IOT TÍCH HỢP QUẢN LÝ VÀ PHÂN TÍCH TÀI CHÍNH CÁ NHÂN
**Research and Development of an IoT-Based Smart Piggy Bank Platform for Personal Financial Management and Analytics**

* **Đơn vị đào tạo**: ĐẠI HỌC QUỐC GIA TP. HỒ CHÍ MINH – TRƯỜNG ĐẠI HỌC CÔNG NGHỆ THÔNG TIN – KHOA CÔNG NGHỆ THÔNG TIN
* **Môn học**: Chuyên đề tốt nghiệp (IE400)
* **Giảng viên hướng dẫn**: ThS. Đỗ Minh Tiến
* **Sinh viên thực hiện**: Võ Duy Lễ – MSSV: 25410080
* **Thời gian thực hiện**: 09/2026

---

## MỤC LỤC TỔNG QUAN

1. [TỔNG QUAN & BỐI CẢNH ĐỀ TÀI](#1-tổng-quan--bối-cảnh-đề-tài)
2. [MỤC TIÊU VÀ PHẠM VI NGHIÊN CỨU](#2-mục-tiêu-và-phạm-vi-nghiên-cứu)
3. [MÔ HÌNH KIẾN TRÚC HỆ THỐNG (HYBRID MICROSERVICES)](#3-mô-hình-kiến-trúc-hệ-thống-hybrid-microservices)
4. [THIẾT KẾ CƠ SỞ DỮ LIỆU CHUẨN HÓA (3 CSDL NGHIỆP VỤ)](#4-thiết-kế-cơ-sở-dữ-liệu-chuẩn-hóa-3-csdl-nghiệp-vụ)
5. [BỐN TRỤ CỘT NGHIỆP VỤ CỐT LÕI CỦA NỀN TẢNG](#5-bốn-trụ-cột-nghiệp-vụ-cốt-lõi-của-nền-tảng)
   * 5.1. Quản lý & Xác thực người dùng (IAM & RBAC)
   * 5.2. Quản lý tự động (Automation & Rule Engine)
   * 5.3. Quản lý tài chính & Sổ cái kép (Core Ledger & FinTech)
   * 5.4. Quản lý IoT & Heo đất thông minh (Smart Piggy & Edge Security)
6. [KỊCH BẢN DEMO THỰC NGHIỆM TRÊN WEB PORTAL](#6-kịch-bản-demo-thực-nghiệm-trên-web-portal)
7. [KẾT LUẬN, HẠN CHẾ VÀ ĐỊNH HƯỚNG PHÁT TRIỂN](#7-kết-luận-hạn-chế-và-định-hướng-phát-triển)

---

## 1. TỔNG QUAN & BỐI CẢNH ĐỀ TÀI

### 1.1. Tính cấp thiết của đề tài
Trong kỷ nguyên thanh toán không tiền mặt, trẻ em và người mới bắt đầu ngày càng ít có cơ hội tiếp xúc trực quan với giá trị của tiền bạc:
* **Hạn chế của heo đất cơ học truyền thống**: Hoàn toàn thụ động, dễ bị thâm hụt cơ học hoặc cạy nắp mà không có cơ chế cảnh báo, không có báo cáo tiến độ và không hỗ trợ cơ chế sinh lời hay quỹ thưởng đối ứng.
* **Hạn chế của ứng dụng ngân hàng số thuần túy**: Quá trừu tượng đối với trẻ em do thiếu hoàn toàn trải nghiệm xúc giác (tactile feedback). Trẻ em khó hình thành thói quen kỷ luật tài chính nếu chỉ nhìn thấy những con số vô hình trên màn hình điện thoại.

### 1.2. Thách thức kỹ thuật FinTech & IoT
Khi kết nối thiết bị vật lý đặt tại gia đình vào hệ thống tài chính ngân hàng, hai bài toán sống còn nảy sinh:
1. **Tính toàn vẹn dữ liệu tài chính (Data Integrity)**: Tiền mặt nạp vào heo phải được hạch toán tuyệt đối chính xác vào hệ thống, không thất thoát, chống lỗi ghi đúp và chống tạo tiền ảo từ hư vô.
2. **An toàn thiết bị biên (Edge Security & Anti-Tamper)**: Vi điều khiển IoT đặt tại nhà rất dễ bị can thiệp cơ học (cạy nắp, rung lắc, đo xung điện) hoặc can thiệp mạng (nghe lén và phát lại gói tin - Replay Attack) để trục lợi số dư.

**Giải pháp của đề tài**: Xây dựng một nền tảng lai hoàn chỉnh (End-to-End Hybrid Platform) kết hợp giữa thiết bị Heo đất thông minh IoT (ESP32), hệ thống vi dịch vụ Ngân hàng lõi chuẩn Sổ cái kép (Double-Entry Core Ledger) và Cổng ứng dụng thống nhất (Unified Web Portal).

---

## 2. MỤC TIÊU VÀ PHẠM VI NGHIÊN CỨU

### 2.1. Mục tiêu kỹ thuật cụ thể
* **Tầng Nhúng & IoT**: Thiết kế phần cứng Heo đất trên chip ESP32, tích hợp cụm cảm biến nhận diện tiền nạp, cảm biến gia tốc MPU6050 giám sát rung lắc 3 trục, công tắc hành trình chống cạy nắp và khóa chốt điện từ Solenoid 5V.
* **Tầng Backend FinTech**: Triển khai động cơ Sổ cái kép bất biến (Double-Entry Ledger Engine) bảo đảm nguyên tắc $\sum\text{Debit} = \sum\text{Credit}$; ứng dụng chuỗi băm SHA-256 Hash Chaining ngăn chặn việc chỉnh sửa lén trong Database.
* **Tầng Bảo mật M2M**: Ký số toàn bộ giao dịch máy-với-máy bằng thuật toán HMAC-SHA256, áp dụng bộ đệm Nonce trên Redis lọc sạch tấn công Replay Attack trong cửa sổ 60 giây.
* **Tầng Giao diện Người dùng**: Xây dựng 1 Source React Vite duy nhất, điều hướng phân quyền động theo RBAC cho 3 nhóm đối tượng: Quản trị viên hệ thống (/admin/*), Ngân hàng / Doanh nghiệp đối tác (/corp/*) và Gia đình / Phụ huynh (/retail/*).

### 2.2. Phạm vi đề tài
* Hỗ trợ các mệnh giá tiền giấy và tiền xu lưu hành chuẩn Việt Nam (VNĐ).
* Tương tác mô phỏng và thực nghiệm toàn diện trên giao diện Web Portal tương tác kép (`/retail/piggy-demo`), kết nối thời gian thực qua WebSocket và REST API với cụm vi dịch vụ.

---

## 3. MÔ HÌNH KIẾN TRÚC HỆ THỐNG (HYBRID MICROSERVICES)

Hệ thống được thiết kế theo kiến trúc **Hybrid Microservices** kết hợp **Clean Architecture** và **Event-Driven Pattern**, phân tách rõ ràng thành 4 tầng độc lập:

```
+========================================================================================================+
|                                1. TẦNG THIẾT BỊ VÀ NGƯỜI DÙNG (CLIENT LAYER)                           |
|  [ESP32 Smart Piggy Bank]             [Unified Web Portal (:5173)]              [Mobile PWA]          |
|  - Cảm biến xung nhét tiền            - 1 Source Code React Vite duy nhất       - Giao diện phụ huynh |
|  - Khóa chốt Solenoid 5V              - Điều hướng linh hoạt theo JWT Role:     - Giám sát hũ con     |
|  - Cảm biến rung MPU6050               + /admin/* (Quản trị hệ thống)                                 |
|  - Chữ ký số HMAC-SHA256               + /corp/*  (Doanh nghiệp / Teller)                             |
|                                        + /retail/* (Gia đình / Trẻ em)                                |
+========================================================================================================+
                                                    │
                                     (HTTPS / WSS / JWT / HMAC)
                                                    ▼
+========================================================================================================+
|                            2. TẦNG CỔNG BẢO MẬT & ĐIỀU PHỐI (GATEWAY LAYER)                            |
|  [Spring Cloud Gateway (:8080)]                     [Netflix Eureka (:8761)]   [Redis Cache (:6379)]   |
|  - Phân luồng định tuyến (Routing)                  - Service Discovery         - Nonce chống Replay   |
|  - Kiểm tra JWT Token & Rate Limit                  - Health Check & Registry   - Session & Lock       |
+========================================================================================================+
                                                    │
                                                    ▼
+========================================================================================================+
|                              3. TẦNG VI DỊCH VỤ NGHIỆP VỤ (MICROSERVICES MESH)                         |
|                                                                                                        |
|   ┌───────────────────────────┐     ┌───────────────────────────┐     ┌────────────────────────────┐  |
|   │     CORE IAM DOMAIN       │     │   CORE LEDGER DOMAIN      │     │  APP & FINTECH IOT DOMAIN  │  |
|   │  - Auth Service (:8081)   │     │  - Ledger Service (:8085) │     │  - Payment Service (:8083) │  |
|   │  - OTP Service (:8094)    │     │  - Sổ cái kép ngân hàng   │     │  - Notif Service (:8084)   │  |
|   │  - Entity Service (:8082) │     │  - Hash Chaining SHA-256  │     │  - FastAPI Satellite(:8000)│  |
|   └─────────────┬─────────────┘     └─────────────┬─────────────┘     └──────────────┬─────────────┘  |
+=================│=================================│==================================│=================+
                  │                                 │                                  │
                  ▼                                 ▼                                  ▼
+========================================================================================================+
|                     4. TẦNG CƠ SỞ DỮ LIỆU TỐI ƯU HÓA (CONSOLIDATED PERSISTENCE LAYER)                  |
|                                                                                                        |
|      [(1) liochio_core_db]              [(2) liochio_ledger_db]             [(3) liochio_app_db]       |
|      - 63 bảng hệ thống                 - 3 bảng cốt lõi                   - 111 bảng nghiệp vụ        |
|      - IAM, Users, Roles, RBAC          - journal_entries                  - smart_piggy_devices       |
|      - core_tenants, core_domains       - journal_entry_details            - smart_piggy_goals         |
|      - core_otps, approval_requests     - ledger_accounts                  - wallets, payment_orders   |
|      (Quản trị định danh lõi)           (Sổ cái kế toán bất biến)           - notifications, ai_scores  |
|                                                                            (Ứng dụng, IoT & FinTech)   |
+========================================================================================================+
```

### Vai trò từng phân tầng:
1. **Client Layer**: Cổng tương tác trực quan. Thiết bị ESP32 truyền nhận sự kiện qua giao tiếp M2M có mã hóa; Web Portal phản hồi tức thì với giao diện tối ưu tốc độ phản hồi dưới 16ms.
2. **Gateway & Registry Layer**: Spring Cloud Gateway đảm nhận xác thực tập trung JWT RSA-256, điều phối luồng và giới hạn tần suất yêu cầu (Rate Limiting). Netflix Eureka giúp các dịch vụ con tự động đăng ký và tìm thấy nhau mà không cần cấu hình cứng IP.
3. **Microservices Mesh**: Phân chia ranh giới rõ ràng: Java Spring Boot xử lý các luồng tài chính khắt khe; Python FastAPI đảm nhiệm luồng xử lý bất đồng bộ (asyncio) cho sự kiện IoT thời gian thực và phân tích trí tuệ nhân tạo.

---

## 4. THIẾT KẾ CƠ SỞ DỮ LIỆU CHUẨN HÓA (3 CSDL NGHIỆP VỤ)

Thay vì phân tán thành 6 database nhỏ gây ra bài toán phức tạp về Giao dịch phân tán (Distributed Transaction), hệ thống đã được **tinh gọn và chuẩn hóa thành 3 Cơ sở dữ liệu nghiệp vụ độc lập**:

| CSDL Thực tế | Số bảng | Dịch vụ sở hữu | Nội dung & Ranh giới nghiệp vụ (Bounded Context) |
| :--- | :---: | :--- | :--- |
| **`liochio_core_db`** | **63** | `auth-service`<br>`otp-service`<br>`entity-service` | **Quản trị định danh lõi (IAM) & Phê duyệt**: Lưu trữ toàn bộ người dùng (`core_users`), vai trò (`core_roles`), phân quyền chi tiết (`core_permissions`), phiên làm việc (`core_sessions`), mã xác thực OTP (`core_otps`), quy trình kiểm duyệt kép Maker-Checker (`approval_requests`) và cấu hình Server-Driven UI (`master_menus`). |
| **`liochio_ledger_db`** | **3** | `ledger-service` | **Sổ cái kế toán kép bất biến (Append-Only)**: Được cô lập hoàn toàn khỏi ứng dụng thông thường để đảm bảo tiêu chuẩn ngân hàng lõi. Gồm 3 bảng then chốt: `ledger_accounts` (tài khoản nguồn/đích), `journal_entries` (bút toán sổ cái tổng kèm mã băm liên kết `prev_hash` $\rightarrow$ `current_hash`) và `journal_entry_details` (chi tiết dòng Nợ/Có). |
| **`liochio_app_db`** | **111** | `payment-service`<br>`notification-service`<br>`FastAPI Satellite` | **Ứng dụng hợp nhất, IoT & FinTech**: Quản lý thiết bị Heo đất (`smart_piggy_devices`), hũ mục tiêu con (`smart_piggy_goals`), nhật ký cảm biến (`smart_piggy_coin_logs`), ví điện tử (`wallets`), cổng thanh toán (`payment_orders`, VNPay, MoMo), thông báo (`notifications`) và kết quả phân tích AI (`ai_financial_scores`). |

---

## 5. BỐN TRỤ CỘT NGHIỆP VỤ CỐT LÕI CỦA NỀN TẢNG

Hệ thống được vận hành bởi 4 trụ cột nghiệp vụ khép kín:

---

### 5.1. TRỤ CỘT 1: QUẢN LÝ VÀ XÁC THỰC NGƯỜI DÙNG (IAM & RBAC)
*Nền tảng an ninh do `auth-service`, `otp-service` và `liochio_core_db` phụ trách.*

1. **Đăng nhập một cổng & Điều hướng đa phân hệ (Unified SSO & Dynamic Routing)**:
   * Người dùng truy cập qua 1 cổng Web duy nhất (:5173). Sau khi xác thực, hệ thống giải mã JWT Payload để tự động điều hướng vào đúng không gian làm việc:
     * Quản trị viên $\rightarrow$ `/admin/*` (Quản trị hệ thống, tra soát phân tán, cấu hình Tenant).
     * Ngân hàng / Teller $\rightarrow$ `/corp/*` (Thẩm định hồ sơ Onboarding 4 cổng, duyệt yêu cầu Maker-Checker).
     * Gia đình / Phụ huynh $\rightarrow$ `/retail/*` (Quản lý ví heo đất, hũ tiết kiệm con, mô phỏng IoT).
2. **Cơ chế xác thực JWT RS256 & Quản lý phiên làm việc**:
   * Áp dụng mã hóa bất đối xứng RSA-256 qua chuẩn JWKS (`/.well-known/jwks.json`).
   * Bảng `core_sessions` giám sát danh sách thiết bị truy cập, hỗ trợ tính năng buộc đăng xuất toàn bộ thiết bị hoặc thu hồi phiên từ xa khi phát hiện dấu hiệu xâm phạm.
3. **Phân quyền ma trận RBAC & Đa đối tác (Multi-Tenancy)**:
   * Phân quyền chi tiết từng chức năng dựa trên bảng `core_roles` và `core_permissions`.
   * Hỗ trợ cô lập dữ liệu giữa các ngân hàng đối tác hoặc doanh nghiệp khác nhau (`core_tenants`, `core_domains`).
4. **Quy trình Kiểm soát kép (Maker - Checker Workflow)**:
   * Các hành động nhạy cảm (duyệt mở ví heo đất, duyệt rút tiền lớn, nâng hạn mức) bắt buộc tuân theo 2 bước:
     * *Maker (Người khởi tạo)*: Lập yêu cầu, hệ thống ghi nhận ở trạng thái `PENDING`.
     * *Checker (Người phê chuẩn)*: Thẩm định hồ sơ, nhập mã OTP xác thực và bấm `APPROVE` để kích hoạt giao dịch chính thức.

---

### 5.2. TRỤ CỘT 2: QUẢN LÝ TỰ ĐỘNG (AUTOMATION & RULE ENGINE)
*Động cơ tự động hóa do `FastAPI Satellite`, `notification-service` và `liochio_app_db` phụ trách.*

1. **Tự động phân bổ dòng tiền vào các Hũ mục tiêu con (Auto-Allocation)**:
   * Khi phát sinh một khoản nạp tiền, hệ thống tự động chia số tiền theo tỷ lệ phần trăm đã thỏa thuận trước giữa cha mẹ và con (ví dụ: 50% vào Hũ Học tập, 30% vào Hũ Mua xe đạp, 20% vào Hũ Tương lai dài hạn).
2. **Động cơ Quy tắc Thưởng đối ứng của Phụ huynh (Parent Matching Rules)**:
   * *Quy tắc phần trăm*: Phụ huynh thiết lập quy tắc thưởng thêm 20% - 50% số tiền con tự tích lũy. Khi con bỏ ống 50.000 VNĐ, hệ thống tự động trích ví cha mẹ cộng thêm 10.000 VNĐ vào hũ của con.
   * *Quy tắc cột mốc*: Tự động cộng thưởng thành tựu khi hũ con đạt 100% mục tiêu trước hạn.
3. **Tự động phản ứng khẩn cấp & Đóng băng ví (Auto-Protection Trigger)**:
   * Khi cảm biến IoT phát hiện lực cạy phá hoặc rung lắc mạnh vượt ngưỡng an toàn ($a > 2.5g$), hệ thống tự động đổi trạng thái Heo đất và Ví sang `FROZEN`, khóa tính năng rút tiền và bắn tin nhắn Push Notification về điện thoại phụ huynh.
4. **Tự động hóa thông báo sự kiện (Event-Driven Notification)**:
   * Tự động phát thông báo qua WebSocket, Webhook và SSE cập nhật số dư thời gian thực lên Dashboard mà người dùng không cần tải lại trang.

---

### 5.3. TRỤ CỘT 3: QUẢN LÝ TÀI CHÍNH & SỔ CÁI KÉP (CORE LEDGER & FINTECH)
*Động cơ tài chính ngân hàng lõi do `ledger-service`, `payment-service` và `liochio_ledger_db` phụ trách.*

1. **Nguyên lý Sổ cái kép Bất biến (Double-Entry Bookkeeping)**:
   * Hệ thống tuyệt đối **không dùng lệnh `UPDATE` đè số dư**. Mọi giao dịch tài chính đều được hạch toán bằng cặp bút toán Nợ/Có đối ứng:
     $$\sum \text{Amount (DEBIT - Nợ)} = \sum \text{Amount (CREDIT - Có)}$$
   * Khi nạp 50.000 VNĐ từ heo đất: Nợ Tài khoản Tiền mặt Heo đất (+50.000đ), Có Tài khoản Hũ mục tiêu của bé (+50.000đ).
2. **Chuỗi băm SHA-256 bảo vệ toàn vẹn (Tamper-Evident Hash Chaining)**:
   * Mỗi bút toán (`journal_entries`) được tính toán mã băm liên kết:
     $$\text{current\_hash} = \text{SHA256}(\text{prev\_hash} + \text{entry\_no} + \text{amount} + \text{idempotency\_key} + \text{posted\_at})$$
   * Nếu bất kỳ ai can thiệp trực tiếp vào MySQL để sửa số dư, chuỗi xích băm sẽ bị đứt gãy tức thì khi chạy đối soát EOD, giúp hệ thống phát hiện gian lận ngay lập tức.
3. **Khóa chống tranh chấp & Chống gửi lặp (Concurrency & Idempotency)**:
   * Áp dụng `idempotency_key` duy nhất cho mỗi giao dịch để loại bỏ triệt để nguy cơ vi điều khiển bị mất mạng rồi gửi lại cùng một khoản tiền 2 lần.
   * Sử dụng khóa bi quan (`SELECT FOR UPDATE`) tại thời điểm hạch toán để ngăn chặn lỗi Race Condition khi có nhiều giao dịch xảy ra trong cùng một mili-giây.
4. **Báo cáo & Phân tích Tài chính Cá nhân (Financial Analytics)**:
   * Cung cấp các biểu đồ dòng tiền vào/ra, cơ cấu ngân sách theo quy tắc 50-30-20 và chấm điểm kỷ luật tài chính AI (`ai_financial_scores`).

---

### 5.4. TRỤ CỘT 4: QUẢN LÝ IOT & HEO ĐẤT THÔNG MINH (SMART PIGGY & HARDWARE INTEGRATION)
*Hệ sinh thái thiết bị nhúng do vi điều khiển `ESP32`, firmware và `FastAPI Satellite` phụ trách.*

1. **Vòng đời thiết bị & Ghép nối an toàn Zero-Trust**:
   * Heo đất khi xuất xưởng chưa có bất kỳ quyền hạn nào.
   * Quá trình ghép nối diễn ra thông qua việc quét mã QR định danh phần cứng (`device_uuid`) dán dưới đáy heo, cấp cấu hình WiFi và trao đổi cặp khóa bí mật (`Device_Secret_Key`).
   * Máy chủ cấp phát `Hardware_Token` lưu trữ vĩnh viễn vào bộ nhớ Flash NVS của chip ESP32.
2. **Nhận diện và thu nhận tiền mặt tại biên (Edge Sensing)**:
   * Cụm cảm biến quang học kết hợp cơ học tại khe nhét tiền đo bề rộng xung tín hiệu để xác định mệnh giá nạp hợp lệ.
3. **Giao tiếp M2M có ký số & Chống phát lại (Anti-Replay Attack)**:
   * ESP32 ký số mọi gói tin gửi lên bằng thuật toán `HMAC-SHA256`.
   * Sử dụng mã ngẫu nhiên `Nonce` lưu tạm trên Redis với thời gian sống TTL 60 giây. Nếu phát hiện Nonce trùng lặp hoặc dấu thời gian bị lệch, giao dịch bị hủy bỏ ngay lập tức.
4. **Hệ thống phòng vệ vật lý đa tầng (Anti-Tamper)**:
   * Cảm biến gia tốc MPU6050 giám sát rung lắc 3 trục và công tắc hành trình (Limit Switch) chống cạy mở nắp.
   * Khi phát hiện nguy cơ: Hú còi báo động 100dB tại chỗ, chốt Solenoid khóa cứng và gửi ngắt khẩn cấp lên Cloud để đóng băng tài khoản ví.
5. **Rút tiền cam kết 2 pha phối hợp Khóa chốt cơ điện Solenoid (Two-Phase Saga Withdrawal)**:
   * *Pha 1 (Giữ chỗ)*: Nhập Smart OTP xác thực hợp lệ $\rightarrow$ Máy chủ tạm khóa số dư (Hold) và gửi lệnh mở khóa xuống ESP32.
   * *Pha 2 (Mở chốt & Commit)*: Kích xung điện 5V mở chốt Solenoid trong 5 giây $\rightarrow$ Cảm biến hành trình báo nắp mở thành công $\rightarrow$ Máy chủ chính thức ghi giảm số dư trên sổ cái kép. Nếu kẹt cơ học hoặc mất nguồn, hệ thống tự động hoàn nguyên số dư (Compensating Rollback).

---

## 6. KỊCH BẢN DEMO THỰC NGHIỆM TRÊN WEB PORTAL

Toàn bộ hệ thống được chứng minh hoạt động thực tế trên giao diện **Mô phỏng Heo đất tương tác kép** (`http://localhost:5173/retail/piggy-demo`), với thiết kế chia đôi màn hình độc đáo:

```
+-------------------------------------------------------------------------------------------------------+
|                                    GIAO DIỆN DEMO WEB PORTAL (:5173)                                  |
+---------------------------------------------------+---------------------------------------------------+
|     CỘT TRÁI: MÔ PHỎNG PHẦN CỨNG HEO ĐẤT ESP32    |       CỘT PHẢI: ỨNG DỤNG PHỤ HUYNH & QUẢN TRỊ     |
+---------------------------------------------------+---------------------------------------------------+
| [Công tắc Nguồn] Bật / Tắt nguồn điện ảo          | [Bắt sóng Beacon] Tự động dò thấy Heo đất mới    |
| [Màn hình OLED SSD1306] Hiện WiFi, số dư, cảm xúc | [Nút Ghép Đôi] Cấp phát Token và liên kết ví con  |
| [Nút Bấm Ghép Đôi] Phát sóng Radar BLE Beacon     | [Quản lý Hũ Con] Xem tiến độ sách vở, xe đạp      |
| [Khe Nhét Tiền] Chọn mệnh giá 10k - 500k          | [Đập Heo Tất Toán] Búa thần tài vung xuống        |
| [Công tắc Cạy Nắp & Rung Lắc] Test Anti-Tamper   | [Duyệt Smart OTP] Ký duyệt giải ngân an toàn      |
| [Chốt Solenoid 5V] Hiển thị trạng thái Khóa / Mở  | [Terminal Logs] Dòng log kiểm toán Real-time      |
+---------------------------------------------------+---------------------------------------------------+
```

### 4 Bước thực nghiệm trình diễn trực tiếp trước Hội đồng:
1. **Bước 1: Ghép nối thiết bị thời gian thực**:
   * Tại Cột Trái, bật nguồn và bấm nút "Tìm thiết bị liên kết" $\rightarrow$ Hiệu ứng sóng Radar BLE phát xung.
   * Tại Cột Phải, thẻ Heo đất mới xuất hiện kèm địa chỉ MAC. Bấm nút "Ghép Đôi Ngay" $\rightarrow$ Màn hình OLED ảo tại Cột Trái lập tức đổi sang trạng thái "WiFi: Đã kết nối", số dư ban đầu 0 VNĐ.
2. **Bước 2: Nạp tiền vật lý & Cập nhật Real-time WebSocket**:
   * Tại Cột Trái, chọn tờ tiền 50.000 VNĐ và bấm nạp $\rightarrow$ Hoạt họa tiền trượt vào khe kèm âm thanh Web Audio sinh động.
   * Số dư trên màn hình OLED và trên Cột Phải nhảy lên tức thì qua WebSocket mà không cần F5 tải lại trang; quy tắc thưởng đối ứng của phụ huynh tự động trích ví cộng thêm cho bé.
3. **Bước 3: Thử nghiệm Chống phá hoại (Anti-Tamper)**:
   * Tại Cột Trái, gạt công tắc rung lắc chấn động $\rightarrow$ Còi báo động phát âm thanh cảnh báo, OLED chuyển biểu cảm sốc.
   * Cột Phải lập tức hiện hộp thoại cảnh báo xâm phạm màu đỏ, trạng thái Heo đất tự động chuyển sang `FROZEN`, vô hiệu hóa tính năng rút tiền.
4. **Bước 4: Đập heo tất toán & Kích hoạt Khóa Solenoid**:
   * Phụ huynh bấm mở khóa ví, sau đó bấm biểu tượng Búa thần tài để tất toán hũ con.
   * Hoạt họa búa vung xuống đập vỡ heo đất $\rightarrow$ Phụ huynh nhập mã Smart OTP $\rightarrow$ Chốt Solenoid tại Cột Trái nảy mở sang màu xanh và giao dịch sổ cái kép chính thức hoàn tất.

---

## 7. KẾT LUẬN, HẠN CHẾ VÀ ĐỊNH HƯỚNG PHÁT TRIỂN

### 7.1. Kết luận đề tài
* **Về mặt Hạ tầng**: Vận hành thành công hệ sinh thái vi dịch vụ tốc độ cao với 189 API chuẩn hóa, thời gian phản hồi trung bình dưới 85 mili-giây. Dữ liệu được tinh gọn hiệu quả vào 3 cơ sở dữ liệu chuyên biệt (`liochio_core_db`, `liochio_ledger_db`, `liochio_app_db`).
* **Về mặt FinTech**: Ứng dụng thành công chuẩn mực Sổ cái kép ngân hàng lõi và Chuỗi mã băm SHA-256 bảo đảm tính bất biến, không thể sửa đổi lén dữ liệu tài chính.
* **Về mặt IoT**: Chế tạo và mô phỏng hoàn chỉnh cơ chế nhận diện nạp tiền, phòng vệ chống cạy nắp và cơ chế rút tiền 2 pha kết hợp khóa điện từ Solenoid.
* **Về mặt Trải nghiệm**: 1 Source Web Portal duy nhất đáp ứng trọn vẹn 3 phân hệ Quản trị viên, Ngân hàng và Gia đình.

### 7.2. Hạn chế còn tồn tại
* Cơ chế nhận diện tiền mặt cơ quang học còn nhạy cảm với độ phẳng của tờ tiền và ánh sáng môi trường.
* Thiết bị hiện sử dụng nguồn điện cắm dây trực tiếp, chưa tích hợp mạch sạc và pin dự phòng khi mất điện đột ngột.

### 7.3. Định hướng phát triển tương lai
1. **Trí tuệ nhân tạo (AI/ML)**: Huấn luyện các mô hình Machine Learning phân tích chuỗi thời gian thói quen chi tiêu của gia đình để tự động đưa ra các lời khuyên tiết kiệm được cá nhân hóa.
2. **Thị giác máy tính biên (TinyML)**: Nâng cấp camera mini ESP32-CAM nhận diện chính xác hoa văn tiền polymer và phát hiện tiền giả ngay tại khe nhét.
3. **Mở rộng Open Banking**: Kết nối trực tiếp cổng thanh toán VietQR liên ngân hàng, hỗ trợ phụ huynh nạp tiền không tiếp xúc từ tài khoản ngân hàng thực vào ví heo đất.

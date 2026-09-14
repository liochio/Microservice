# DANH MỤC API TOÀN HỆ THỐNG (ENTERPRISE API SPECIFICATION CATALOG)
## HỆ SINH THÁI KHÁCH HÀNG CÁ NHÂN & HEO ĐẤT THÔNG MINH IOT (64 ENDPOINTS)

> **Điểm đón tiếp chung toàn hệ thống (NGINX Reverse Proxy):** `http://localhost` (Port 80)  
> **Spring Cloud Gateway (Cổng Microservices Java):** `http://localhost:8080`  
> **Swagger UI Java Core Platform (IAM & Ledger):** `http://localhost:8081/swagger-ui.html`  
> **Swagger UI Python FinTech (IoT & AI Satellite):** `http://localhost:8000/docs`  
> **Headers bắt buộc khi gọi Protected API:**
> - `Authorization: Bearer <accessToken>`
> - `Accept-Language: vi | en`
> - `X-Idempotency-Key: <unique-uuid>` (bắt buộc đối với các giao dịch tài chính nạp/rút/chuyển tiền)

---

## 1. PHÂN HỆ XÁC THỰC & VÒNG ĐỜI TÀI KHOẢN (`auth-service` : 8081)

| Phương Thức | Tuyến Đường (Endpoint) | Quyền Hạn | Mô Tả Chức Năng Nghiệp Vụ |
| :--- | :--- | :---: | :--- |
| `POST` | `/api/v1/auth/login` | Public | Đăng nhập tài khoản cá nhân (Username/Password), hỗ trợ 2FA |
| `POST` | `/api/v1/auth/register` | Public | Đăng ký tài khoản cá nhân mới ở trạng thái `PENDING_VERIFY` |
| `POST` | `/api/v1/auth/verify-otp` | Public | Xác thực OTP 6 số để kích hoạt tài khoản thành `ACTIVE` trước khi login |
| `POST` | `/api/v1/auth/verify-device-otp` | Public | Thách thức 2FA khi đăng nhập từ thiết bị lạ để nhận cặp Token |
| `GET` | `/api/v1/users/me` | Authenticated | Xem hồ sơ cá nhân, cấp độ eKYC và hạn mức giao dịch ngày |
| `POST` | `/api/v1/auth/refresh-token` | Public | Làm mới Access Token (Token Rotation & Reuse Detection) |
| `POST` | `/api/v1/auth/change-password` | Authenticated | Thay đổi mật khẩu người dùng với chính sách kiểm tra độ mạnh |
| `GET` | `/api/v1/auth/devices` | Authenticated | Danh sách các thiết bị di động, máy tính đã đăng nhập tài khoản |
| `DELETE` | `/api/v1/auth/devices/{deviceId}` | Authenticated | Thu hồi quyền tin cậy và xóa thiết bị khỏi tài khoản |
| `GET` | `/api/v1/auth/sessions` | Authenticated | Danh sách các phiên đăng nhập đang hoạt động trên hệ thống |
| `POST` | `/api/auth/security/sessions/{id}/revoke`| Authenticated | Thu hồi từ xa một phiên làm việc đáng ngờ |
| `POST` | `/api/v1/auth/logout-all-devices` | Authenticated | Cưỡng chế đăng xuất trên toàn bộ các thiết bị |
| `POST` | `/api/v1/auth/logout` | Authenticated | Đăng xuất phiên làm việc hiện tại, đưa Token vào Redis Blacklist |
| `DELETE` | `/api/v1/users/{userId}` | Authenticated | Xóa tài khoản cá nhân tuân thủ chính sách App Store / Google Play |

---

## 2. PHÂN HỆ ĐĂNG NHẬP KHÔNG MẬT KHẨU BẰNG QR CODE (`auth-service` : 8081)

| Phương Thức | Tuyến Đường (Endpoint) | Giao Diện Gọi | Mô Tả Chức Năng Nghiệp Vụ |
| :--- | :--- | :---: | :--- |
| `POST` | `/api/v1/auth/qr/init` | Trình duyệt Web | Khởi tạo session QR code (hạn 120s) và lắng nghe sự kiện WebSocket |
| `POST` | `/api/v1/auth/qr/scan` | App Mobile | Quét mã QR trên màn hình Web, đổi trạng thái session sang `SCANNED` |
| `POST` | `/api/v1/auth/qr/confirm` | App Mobile | Xác nhận đăng nhập bằng FaceID/PIN, sinh `exchangeAuthCode` cho Web |
| `POST` | `/api/v1/auth/qr/exchange` | Trình duyệt Web | Gửi `exchangeAuthCode` đổi lấy Access Token và Refresh Token chính thức |

---

## 3. PHÂN HỆ BẢO MẬT GIAO DỊCH & SMART OTP (`auth-service` & `otp-service`)

| Phương Thức | Tuyến Đường (Endpoint) | Quyền Hạn | Mô Tả Chức Năng Nghiệp Vụ |
| :--- | :--- | :---: | :--- |
| `POST` | `/api/v1/auth/smart-otp/setup` | Authenticated | Khởi tạo Base32 Secret & Barcode QR chuẩn TOTP RFC 6238 |
| `POST` | `/api/v1/auth/smart-otp/verify` | Authenticated | Xác thực mã 6 số TOTP và cài mã PIN bảo vệ Smart OTP gắn với thiết bị |
| `POST` | `/api/v1/auth/smart-otp/action-token`| Authenticated| Xác thực Smart OTP cấp `action_token` (120s) trước giao dịch nhạy cảm |
| `POST` | `/api/v1/otp/generate` | Public / Authed | Yêu cầu sinh hoặc gửi lại mã xác thực OTP 6 số qua Email/SMS (Redis TTL) |
| `POST` | `/api/v1/otp/verify` | Public / Authed | Xác thực tính hợp lệ của mã OTP 6 số từ Dedicated OTP Service |

---

## 4. PHÂN HỆ ĐỊNH DANH EKYC CÁ NHÂN (`auth-service` : 8081)

| Phương Thức | Tuyến Đường (Endpoint) | Quyền Hạn | Mô Tả Chức Năng Nghiệp Vụ |
| :--- | :--- | :---: | :--- |
| `GET` | `/api/v1/ekyc/status` | Authenticated | Tra cứu trạng thái định danh CCCD và hạn mức chuyển tiền ngày |
| `POST` | `/api/v1/ekyc/submit` | Authenticated | Nộp / Cập nhật lại hồ sơ CCCD (khi bị mờ/từ chối) nâng hạn mức Tier 2 (500Tr) |
| `POST` | `/api/v1/ekyc/approve/{userId}` | Admin | Phê duyệt hồ sơ eKYC cho người dùng, nâng cấp hạn mức giao dịch |

---

## 5. PHÂN HỆ SỔ CÁI KÉP CORE BANKING (`auth-service` : 8081)

| Phương Thức | Tuyến Đường (Endpoint) | Quyền Hạn | Mô Tả Chức Năng Nghiệp Vụ |
| :--- | :--- | :---: | :--- |
| `GET` | `/api/v1/ledger/balance` | Authenticated | Tra cứu số dư đa trạng thái (`Available`, `Holding`, `Escrow`) |
| `GET` | `/api/v1/ledger/history/{userId}` | Authenticated | Xem sao kê bút toán sổ cái kèm chuỗi băm bất biến SHA-256 |
| `GET` | `/api/v1/ledger/account/{accountNo}`| Public / Core | Tra cứu số dư theo mã định danh tài khoản sổ cái (`ACC_USR_...`) |
| `POST` | `/api/v1/ledger/m2m/transaction` (TOPUP) | M2M | Hạch toán Nạp tiền: DEBIT System Settlement -> CREDIT User Available |
| `POST` | `/api/v1/ledger/m2m/transaction` (PIGGY_LOCK) | M2M | Hạch toán Khóa tiền Heo đất: DEBIT User Available -> CREDIT User Escrow |
| `POST` | `/api/v1/ledger/m2m/transaction` (PIGGY_UNLOCK)| M2M | Hạch toán Mở khóa Heo đất: DEBIT User Escrow -> CREDIT User Available |
| `POST` | `/api/v1/ledger/m2m/transaction` (WITHDRAW) | M2M | Hạch toán Rút tiền: DEBIT User Available -> CREDIT System Settlement |

---

## 6. PHÂN HỆ HỒ SƠ & BẢO MẬT PYTHON FINTECH (`Python Satellite` : 8000)

| Phương Thức | Tuyến Đường (Endpoint) | Quyền Hạn | Mô Tả Chức Năng Nghiệp Vụ |
| :--- | :--- | :---: | :--- |
| `POST` | `/api/v1/auth/login` | Public | Đăng nhập tài khoản cá nhân trên Python FinTech |
| `GET` | `/api/v1/users/me` | Authenticated | Tra cứu thông tin cá nhân, ngày sinh, giới tính và danh sách ví |
| `PUT` | `/api/v1/users/me` | Authenticated | Cập nhật thông tin cá nhân (Họ tên, Ngày sinh, Giới tính) chống XSS |
| `POST` | `/api/v1/users/change-password` | Authenticated | Đổi mật khẩu tài khoản có xác thực mật khẩu cũ |
| `POST` | `/api/v1/users/forgot-password` | Public | Quên mật khẩu: Yêu cầu gửi mã OTP 6 số qua Email |
| `POST` | `/api/v1/users/reset-password` | Public | Đặt lại mật khẩu mới bằng mã OTP xác thực |
| `POST` | `/api/v1/auth/verify-otp` | Public | Xác thực OTP hoàn tất kích hoạt tài khoản trên Python Resource |

---

## 7. PHÂN HỆ HEO ĐẤT THÔNG MINH IOT (`Python Satellite` : 8000)

| Phương Thức | Tuyến Đường (Endpoint) | Quyền Hạn | Mô Tả Chức Năng Nghiệp Vụ |
| :--- | :--- | :---: | :--- |
| `GET` | `/api/v1/smart-piggy/devices` | Authenticated | Danh sách Heo Đất IoT thuộc quyền sở hữu của người dùng |
| `GET` | `/api/v1/smart-piggy/devices/{deviceId}`| Authenticated| Xem chi tiết trạng thái trực tuyến, địa chỉ MAC, số dư và pin |
| `POST` | `/api/v1/smart-piggy/pair` | Authenticated | Ghép đôi Heo Đất ESP32 mới vào tài khoản cá nhân |
| `POST` | `/api/v1/smart-piggy/drop-money` | Device Ingest | ESP32 gửi xung cảm biến khi đút tiền -> Tự động cộng ví và ghi Sổ Cái Kép |
| `POST` | `/api/v1/smart-piggy/sync-offline-batch` | Device Ingest | Đồng bộ mảng các lần bỏ tiền ngoại tuyến từ Flash LittleFS khi có lại Wi-Fi |
| `POST` | `/api/v1/smart-piggy/withdraw/request` | Authenticated | **Phase 1:** Khóa tiền sang HOLDING, mở chốt Solenoid 5V, đếm ngược 60s |
| `POST` | `/api/v1/smart-piggy/withdraw/confirm` | Authenticated | **Phase 2:** Xác nhận đã lấy tiền qua nút vật lý -> Trừ tiền vĩnh viễn, đóng chốt |
| `POST` | `/api/v1/smart-piggy/withdraw/timeout` | Authenticated | Tự động rollback: Hoàn tiền từ HOLDING về AVAILABLE nếu quá 60s không bấm nút |
| `POST` | `/api/v1/smart-piggy/tamper-alert` | Device / Sensor| Cảm biến gia tốc MPU6050 cảnh báo va đập rung lắc hoặc cạy đáy Heo |
| `POST` | `/api/v1/smart-piggy/led-control` | Authenticated | Điều khiển màu sắc đèn LED RGB và chuông báo Heo Đất |
| `GET` | `/api/v1/smart-piggy/simulator/drop-coin`| Public | Giả lập sự kiện cảm biến thả tiền phục vụ kiểm thử không cần phần cứng |

---

## 8. PHÂN HỆ TÀI CHÍNH CÁ NHÂN, NGÂN SÁCH & AI (`Python Satellite` : 8000)

| Phương Thức | Tuyến Đường (Endpoint) | Quyền Hạn | Mô Tả Chức Năng Nghiệp Vụ |
| :--- | :--- | :---: | :--- |
| `GET` | `/api/v1/goals/` | Authenticated | Danh sách các mục tiêu tiết kiệm cá nhân |
| `POST` | `/api/v1/goals/` | Authenticated | Tạo mục tiêu tiết kiệm cá nhân mới (Tên mục tiêu, số tiền, hạn chót) |
| `GET` | `/api/v1/budgets/` | Authenticated | Danh sách hạn mức ngân sách chi tiêu theo danh mục |
| `POST` | `/api/v1/budgets/` | Authenticated | Thiết lập hạn mức ngân sách chi tiêu tháng cho danh mục |
| `GET` | `/api/v1/smart-piggy/ai/forecast` | Authenticated | Mô hình AI dự báo ngày hoàn thành mục tiêu dựa trên tốc độ đút tiền |
| `GET` | `/api/v1/smart-piggy/ai/behavior` | Authenticated | Phân tích thói quen tiết kiệm Heo Đất và đề xuất giải pháp tối ưu |
| `GET` | `/api/v1/smart-piggy/gamification/status`| Authenticated| Xem cấp bậc Heo Đất (Level), chuỗi ngày tiết kiệm (Streak) và điểm tích lũy |
| `GET` | `/api/v1/wallets` | Authenticated | Danh sách các ví chi tiêu và ví tiết kiệm Heo Đất của người dùng |
| `GET` | `/api/v1/reports/overview` | Authenticated | Báo cáo phân tích thu chi cá nhân tổng hợp theo tháng |

---

## 9. PHÂN HỆ CỔNG THANH TOÁN & WORKER GIÁM SÁT (`SpringBoot Core`)

| Phương Thức | Tuyến Đường (Endpoint) | Quyền Hạn | Mô Tả Chức Năng Nghiệp Vụ |
| :--- | :--- | :---: | :--- |
| `POST` | `/api/payments/create-url` | Authenticated | Khởi tạo URL chuyển hướng thanh toán nạp tiền VNPay / MoMo |
| `GET` | `/api/payments/orders/{orderId}` | Authenticated | Tra cứu trạng thái đơn hàng thanh toán theo Order ID |
| `GET` | `/api/v1/worker/reconciliation/report`| Authenticated | Báo cáo đối soát Sổ Cái Kép tự động kiểm tra Σ Debit = Σ Credit |
| `GET` | `/api/v1/worker/status` | Authenticated | Giám sát trạng thái Central Worker, Outbox Poller và ShedLock |

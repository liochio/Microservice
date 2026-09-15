# 📚 DANH MỤC TỔNG HỢP API TOÀN HỆ THỐNG (FULL API CATALOG 10/10)

Tất cả API công khai được định tuyến qua **API Gateway: 'http://localhost:8080'** hoặc **Nginx Ingress: 'http://localhost:80'**.

---

## 1. PHÂN HỆ XÁC THỰC & IAM ('auth-service :8081')

| STT | Phương thức | Endpoint | Mô tả nghiệp vụ | Phân quyền |
|:---:|:---:|:---|:---|:---:|
| 1 | 'POST' | '/api/v1/auth/register' | Đăng ký tài khoản người dùng mới | Public |
| 2 | 'POST' | '/api/v1/auth/login' | Đăng nhập tài khoản & Nhận JWT KeyPair | Public |
| 3 | 'POST' | '/api/v1/auth/token/refresh' | Cấp mới Access Token từ Refresh Token | Public |
| 4 | 'POST' | '/api/v1/auth/logout' | Đăng xuất & Đưa Token vào Redis Blacklist | Authenticated |
| 5 | 'POST' | '/api/v1/auth/otp/verify' | Xác thực mã OTP kích hoạt tài khoản | Public |
| 6 | 'POST' | '/api/v1/auth/otp/resend' | Gửi lại mã xác thực OTP qua Email/SMS | Public |
| 7 | 'POST' | '/api/v1/auth/password/forgot'| Yêu cầu cấp lại mật khẩu | Public |
| 8 | 'POST' | '/api/v1/auth/password/reset' | Cập nhật mật khẩu mới bằng token | Public |
| 9 | 'GET'  | '/api/v1/auth/me' | Lấy thông tin cá nhân của người dùng hiện tại | Authenticated |
| 10| 'POST' | '/api/v1/onboarding/gate1/review' | Phê duyệt Gate 1 (eKYC) của khách hàng | Maker-Checker |
| 11| 'POST' | '/api/v1/onboarding/gate2/tier' | Phân cấp hạn mức & Tier giao dịch (Gate 2) | Admin |
| 12| 'POST' | '/api/v1/onboarding/gate3/provision' | Hoàn tất phê duyệt mở ví (Gate 3) | Admin |

---

## 2. PHÂN HỆ SỔ CÁI KÉP CORE BANKING ('ledger-service :8085')

| STT | Phương thức | Endpoint | Mô tả nghiệp vụ | Phân quyền |
|:---:|:---:|:---|:---|:---:|
| 1 | 'GET' | '/api/v1/ledger/balance' | Tra cứu số dư 3 trạng thái (Avail, Hold, Escrow) của chính mình | Authenticated |
| 2 | 'GET' | '/api/v1/ledger/balance/{userId}'| Tra cứu số dư tài khoản của một người dùng | Admin / Core |
| 3 | 'GET' | '/api/v1/ledger/account/{accountNo}'| Tra cứu số dư theo mã tài khoản sổ cái | Authenticated |
| 4 | 'GET' | '/api/v1/ledger/history/{userId}' | Lấy lịch sử sao kê bút toán sổ cái kép (có phân trang) | Authenticated |
| 5 | 'POST'| '/api/v1/ledger/m2m/transaction' | Cổng M2M hạch toán dòng tiền (HMAC-SHA256 Signed) | M2M Service |

---

## 3. PHÂN HỆ CẤU HÌNH & MENU ĐỘNG ('entity-service :8082')

| STT | Phương thức | Endpoint | Mô tả nghiệp vụ | Phân quyền |
|:---:|:---:|:---|:---|:---:|
| 1 | 'GET' | '/api/v1/menus/tree' | Lấy cây Menu Động phân cấp theo Portal & Role | Authenticated |
| 2 | 'GET' | '/api/v1/system/configs' | Lấy ma trận cấu hình tham số có hiệu lực | Authenticated |
| 3 | 'POST'| '/api/v1/system/configs/global' | Cập nhật cấu hình toàn cục nền tảng | SuperAdmin |
| 4 | 'POST'| '/api/v1/corp/configs/{key}/override' | Doanh nghiệp ghi đè cấu hình (Maker-Checker) | TenantAdmin |

---

## 4. PHÂN HỆ HEO ĐẤT THÔNG MINH IOT & VÍ APP ('Python FastAPI :8000')

| STT | Phương thức | Endpoint | Mô tả nghiệp vụ | Phân quyền |
|:---:|:---:|:---|:---|:---:|
| 1 | 'GET'  | '/api/v1/smart_piggy/device/{id}/status' | Đọc trạng thái cảm biến, khóa chốt, mức pin | Authenticated |
| 2 | 'POST' | '/api/v1/smart_piggy/device/command' | Gửi lệnh khóa/mở chốt điện từ Heo đất | Authenticated |
| 3 | 'POST' | '/api/v1/smart_piggy/coin-drop' | Ghi nhận tiền đút heo & Đồng bộ Sổ cái M2M | IoT / M2M |
| 4 | 'GET'  | '/api/v1/wallets/my-wallets' | Lấy danh sách ví tiêu dùng của người dùng | Authenticated |
| 5 | 'POST' | '/api/v1/wallets/topup' | Nạp tiền vào ví tiêu dùng ứng dụng | Authenticated |
| 6 | 'POST' | '/api/v1/wallets/transfer' | Chuyển tiền nội bộ giữa các ví | Authenticated |
| 7 | 'GET'  | '/api/v1/wallets/transactions' | Lịch sử giao dịch ví ứng dụng | Authenticated |

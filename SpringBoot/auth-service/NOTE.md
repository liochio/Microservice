# PHÂN HỆ XÁC THỰC, PHÂN QUYỀN RBAC & BẢO MẬT ZERO TRUST (AUTH SERVICE)

> **Cổng dịch vụ:** '8081' (Qua Gateway '8080')  
> **Cơ sở dữ liệu:** 'liochio_core_db' (Core IAM DB)  
> **Phụ trách:** Quản lý danh tính (IAM), phân quyền động (RBAC/ABAC), bảo mật 2FA, thiết bị tin cậy, QR Login và kiểm toán Audit Logging.

---

## 1. KIẾN TRÚC & NGUYÊN TẮC BẢO MẬT LÕI

1. **Mô hình Đa khách thuê Phân tầng (Multi-Tenancy):**
   - Cô lập triệt để dữ liệu người dùng theo 'tenant_id'.
   - Hỗ trợ Tenant Hệ thống ('SYSTEM'), Tenant Doanh nghiệp ('corp_vietravel') và Tenant Cá nhân ('tenant_indiv_01').
2. **Dual-Token & Token Reuse Detection (Family Revocation):**
   - Access Token (JWT 15 phút - 1 giờ) + Refresh Token xoay vòng (14 ngày).
   - Lưu vết băm SHA-256 của Refresh Token trong bảng 'user_sessions'.
   - Khi phát hiện một Refresh Token đã từng bị thu hồi được gửi lại, hệ thống kích hoạt **Family Revocation** lập tức thu hồi toàn bộ các phiên hoạt động của tài khoản đó.
3. **Quản trị Thiết bị Tin cậy & Thách thức 2FA (Step-up Auth):**
   - Gắn phiên với dấu vân tay thiết bị ('device_id'), IP và User-Agent.
   - Khi người dùng đăng nhập trên thiết bị lạ/mới ('is_trusted = false'), hệ thống chặn cấp Token và trả về 'requires2Fa = true', đồng thời phát sinh OTP 6 số để xác thực thiết bị trước khi cấp quyền.
4. **Đăng nhập Không mật khẩu quét mã QR (Passwordless QR Login):**
   - Web khởi tạo QR Session (UUID TTL 120s).
   - Mobile quét mã QR $\rightarrow$ chuyển trạng thái 'SCANNED'.
   - Mobile xác thực sinh trắc học / PIN $\rightarrow$ chuyển 'CONFIRMED' và sinh 'exchange_auth_code' (dùng 1 lần, TTL 10s).
   - Web đổi code lấy cặp Token hoàn tất.
5. **SmartOTP chuẩn RFC 6238 (TOTP):**
   - Khởi tạo Base32 Secret Key (160-bit), tạo mã QR quét Authenticator.
   - Xác thực mã 6 số TOTP và mã hóa PIN bảo vệ.

---

## 2. DANH MỤC 18 API ENDPOINTS CHUẨN DOANH NGHIỆP

| HTTP Method | Endpoint | Mô tả chức năng | Quyền hạn / Header |
| :--- | :--- | :--- | :--- |
| 'POST' | '/api/v1/auth/register' | Đăng ký tài khoản (trạng thái 'PENDING_VERIFY') | Public |
| 'POST' | '/api/v1/auth/verify-otp' | Xác thực OTP 6 số kích hoạt tài khoản 'ACTIVE' | Public |
| 'POST' | '/api/v1/auth/login' | Đăng nhập (Kiểm soát Brute-force & 2FA thiết bị lạ) | Public |
| 'POST' | '/api/v1/auth/verify-device-otp'| Xác thực OTP cho thiết bị mới sau thách thức 2FA | Public |
| 'POST' | '/api/v1/auth/refresh' | Làm mới Token (Xoay vòng Token & Chống Replay) | Public |
| 'POST' | '/api/v1/auth/logout' | Đăng xuất phiên hiện tại của thiết bị | Bearer Token |
| 'POST' | '/api/v1/auth/logout-all-devices' | Cưỡng chế đăng xuất tất cả các thiết bị | Bearer Token |
| 'POST' | '/api/v1/auth/change-password' | Đổi mật khẩu (Kiểm soát Password Policy) | Bearer Token |
| 'POST' | '/api/v1/auth/qr/init' | Khởi tạo phiên quét mã QR Login (Web) | Public |
| 'POST' | '/api/v1/auth/qr/scan' | Quét mã QR đăng nhập (App Mobile) | Bearer Token |
| 'POST' | '/api/v1/auth/qr/confirm' | Xác nhận đăng nhập QR (App Mobile) | Bearer Token |
| 'POST' | '/api/v1/auth/qr/exchange' | Đổi mã xác nhận QR lấy Token (Web) | Public |
| 'GET' | '/api/v1/auth/devices' | Xem danh sách thiết bị đã đăng nhập | Bearer Token |
| 'DELETE' | '/api/v1/auth/devices/{deviceId}' | Thu hồi quyền truy cập của một thiết bị | Bearer Token |
| 'GET' | '/api/v1/auth/sessions' | Xem danh sách phiên làm việc đang hoạt động | Bearer Token |
| 'POST' | '/api/v1/auth/smart-otp/setup' | Khởi tạo Base32 Secret & QR Barcode SmartOTP | Bearer Token |
| 'POST' | '/api/v1/auth/smart-otp/verify'| Kích hoạt SmartOTP với mã 6 số và PIN | Bearer Token |
| 'GET' | '/api/v1/auth/me' | Xem thông tin tài khoản người dùng hiện tại | Bearer Token |
| 'GET' | '/api/v1/roles' | Danh sách vai trò RBAC | 'user:read' |
| 'GET' | '/api/v1/roles/permissions' | Danh mục quyền hạn hệ thống | 'user:read' |
| 'PUT' | '/api/v1/roles/{id}/permissions'| Gán quyền hạn cho vai trò | 'user:assign_role' |
| 'GET' | '/api/v1/users' | Danh sách người dùng có phân trang | 'user:read' |
| 'DELETE' | '/api/v1/users/{id}' | Xóa mềm tài khoản người dùng | 'user:delete' |





Dưới đây là **Bộ Tài Liệu Test Case Toàn Diện (Full Scenarios: Happy Path, Negative, Boundary & Security Cases)** cho toàn bộ **23 API Endpoints** trong Phân hệ Xác thực, Phân quyền RBAC, Bảo mật Zero Trust và Quản trị người dùng.

---

# BẢNG TỔNG HỢP TEST CASES CHO TOÀN BỘ 23 API

---

## PHẦN 1: NHÓM API XÁC THỰC & ĐĂNG NHẬP CƠ BẢN (IAM)

### 1. 'POST /api/v1/auth/register' (Đăng ký tài khoản)
* **Quyền:** Public | **Dữ liệu sinh:** Trạng thái 'PENDING_VERIFY', mã OTP 6 số lưu trong 'user_otp_verifications'.

| TC ID | Kịch bản kiểm thử | Input / Request Body | HTTP Status | Kết quả mong đợi (Expected Result) |
| :--- | :--- | :--- | :---: | :--- |
| **REG_01** | Đăng ký thành công tài khoản hợp lệ (Happy Path) | 'username': '"user_valid"', 'email': '"user_valid@domain.com"', 'password': '"Admin@1234"', 'fullName': '"Nguyen Van A"' | **201 Created** | 'status: 201', 'data.status: "PENDING_VERIFY"', 'data.isEmailVerified: false'. Sinh 1 bản ghi OTP 'status: 'PENDING'' trong DB. |
| **REG_02** | Trùng 'username' đã tồn tại | 'username': '"admin"' (đã có), các trường khác hợp lệ | **409 Conflict** | 'status: 409', 'errorCode: 1002', 'message: "Tên đăng nhập đã tồn tại"'. |
| **REG_03** | Trùng 'email' đã tồn tại | 'email': '"admin@portfolio.local"' (đã có) | **409 Conflict** | 'status: 409', 'errorCode: 1002', 'message: "Email đã được sử dụng"'. |
| **REG_04** | Vi phạm Password Policy (Mật khẩu yếu < 8 ký tự) | 'password': '"123456"' | **400 Bad Request** | 'status: 400', 'errorCode: 1012', 'message: "Mật khẩu phải chứa ít nhất 8 ký tự..."'. |
| **REG_05** | Vi phạm Password Policy (Thiếu ký tự hoa/đặc biệt) | 'password': '"password123"' | **400 Bad Request** | 'status: 400', 'errorCode: 1012', 'message: "Mật khẩu phải bao gồm chữ hoa, chữ thường, chữ số và ký tự đặc biệt"'. |
| **REG_06** | Sai định dạng Email | 'email': '"user_invalid_email_format"' | **400 Bad Request** | 'status: 400', 'errorCode: 1005', Validation error trên field 'email'. |
| **REG_07** | Bỏ trống các trường bắt buộc ('username', 'password', 'email') | '{}' (Body rỗng) | **400 Bad Request** | 'status: 400', Validation error danh sách field không được null. |

---

### 2. 'POST /api/v1/auth/verify-otp' (Xác thực OTP kích hoạt tài khoản)
* **Quyền:** Public | **Dữ liệu tác động:** Chuyển 'users.status = 'ACTIVE'', 'is_email_verified = true'.

| TC ID | Kịch bản kiểm thử | Input / Request Body | HTTP Status | Kết quả mong đợi (Expected Result) |
| :--- | :--- | :--- | :---: | :--- |
| **VOTP_01** | Nhập OTP chính xác còn hạn (Happy Path) | 'email': '"user_valid@domain.com"', 'otpCode': '"<mã_6_số_đúng>"' | **200 OK** | 'status: 200', 'data.status: "ACTIVE"', 'data.isEmailVerified: true'. OTP DB đổi 'status: 'VERIFIED''. |
| **VOTP_02** | Nhập sai mã OTP lần 1 và lần 2 | 'otpCode': '"999999"' | **400 Bad Request** | 'status: 400', 'errorCode: 1010', 'message: "Mã OTP không chính xác. Bạn còn 2 lần thử"'. DB 'attempt_count = 1'. |
| **VOTP_03** | Nhập sai OTP quá 3 lần (Brute-Force OTP) | Nhập sai lần thứ 3 liên tiếp | **400 Bad Request** | 'status: 400', 'errorCode: 1011', 'message: "Đã vượt quá số lần nhập OTP cho phép"'. OTP DB 'status = 'BLOCKED''. |
| **VOTP_04** | Nhập mã OTP đã quá hạn (> 5 phút / 300 giây) | OTP được tạo trước 6 phút | **400 Bad Request** | 'status: 400', 'errorCode: 1009', 'message: "Mã xác thực OTP đã hết hạn"'. |
| **VOTP_05** | Nhập OTP cho User/Email không tồn tại | 'email': '"not_exist@domain.com"', 'otpCode': '"123456"' | **404 Not Found** | 'status: 404', 'errorCode: 1001', 'message: "Không tìm thấy thông tin tài khoản"'. |

---

### 3. 'POST /api/v1/auth/login' (Đăng nhập hệ thống)
* **Quyền:** Public | **Kiểm soát:** Brute-force lockout (5 lần $\rightarrow$ 15 phút), Thách thức 2FA thiết bị lạ.

| TC ID | Kịch bản kiểm thử | Header / Body | HTTP Status | Kết quả mong đợi (Expected Result) |
| :--- | :--- | :--- | :---: | :--- |
| **LOG_01** | Đăng nhập đúng trên Thiết bị Tin cậy ('is_trusted = true') | 'X-Device-Id: dev_trusted_01'<br>'{"username": "admin", "password": "Password@123!"}' | **200 OK** | 'status: 200', 'data.requires2Fa: false', 'data.deviceTrusted: true', trả về 'accessToken' và 'refreshToken'. Ghi 'security_login_histories' status 'SUCCESS'. |
| **LOG_02** | Đăng nhập đúng trên Thiết bị Mới/Lạ ('is_trusted = false') | 'X-Device-Id: dev_brand_new_999'<br>'{"username": "admin", "password": "Password@123!"}' | **200 OK** | 'status: 200', 'data.requires2Fa: true', 'data.challengeToken: "DEVICE_2FA_REQUIRED"', 'data.accessToken: null'. Sinh OTP 'DEVICE_TRUST' gửi về email. |
| **LOG_03** | Đăng nhập sai mật khẩu lần 1 đến lần 4 | '{"username": "admin", "password": "WrongPassword"}' | **400 Bad Request** | 'status: 400', 'errorCode: 1003', 'message: "Mật khẩu không chính xác. Bạn còn 4 lần thử"'. DB 'failed_login_attempts = 1'. |
| **LOG_04** | Đăng nhập sai mật khẩu lần 5 liên tiếp (Brute-Force Lockout) | Nhập sai lần thứ 5 | **400 Bad Request** | 'status: 400', 'errorCode: 1016', 'message: "Đăng nhập sai 5 lần liên tiếp. Tài khoản đã bị tạm khóa 15 phút"'. DB 'lockout_until = now + 15m'. |
| **LOG_05** | Tiếp tục thử đăng nhập khi tài khoản đang bị khóa | Nhập đúng mật khẩu nhưng trong khoảng thời gian 15 phút khóa | **400 Bad Request** | 'status: 400', 'errorCode: 1016', 'message: "Tài khoản tạm thời bị khóa... Vui lòng thử lại sau X giây"'. |
| **LOG_06** | Đăng nhập với Username không tồn tại | '{"username": "ghost_user", "password": "Password@123!"}' | **404 Not Found** | 'status: 404', 'errorCode: 1001'. Ghi log 'security_login_histories' status 'WRONG_PASSWORD'. |

---

### 4. 'POST /api/v1/auth/verify-device-otp' (Xác thực 2FA thiết bị mới)
* **Quyền:** Public | **Dữ liệu tác động:** Gắn nhãn 'is_trusted = true' nếu chọn ghi nhớ.

| TC ID | Kịch bản kiểm thử | Input / Request Body | HTTP Status | Kết quả mong đợi (Expected Result) |
| :--- | :--- | :--- | :---: | :--- |
| **VDOTP_01**| Nhập đúng OTP thiết bị & 'rememberDevice = true' | 'username': '"admin"', 'deviceId': '"dev_brand_new_999"', 'otpCode': '"<mã_đúng>"', 'rememberDevice': 'true' | **200 OK** | Cấp 'accessToken' + 'refreshToken'. DB 'user_devices.is_trusted = true'. |
| **VDOTP_02**| Nhập đúng OTP thiết bị & 'rememberDevice = false' | 'rememberDevice': 'false' | **200 OK** | Cấp 'accessToken' + 'refreshToken'. Thiết bị không đổi thành tin cậy ('is_trusted = false'). |
| **VDOTP_03**| Nhập sai OTP thiết bị | 'otpCode': '"000000"' | **400 Bad Request** | 'status: 400', 'errorCode: 1010', 'message: "Mã OTP không chính xác"'. |
| **VDOTP_04**| OTP xác thực thiết bị đã quá hạn | Gửi request sau 5 phút | **400 Bad Request** | 'status: 400', 'errorCode: 1009', 'message: "Mã xác thực OTP đã hết hạn"'. |

---

### 5. 'POST /api/v1/auth/refresh' (Xoay vòng Token & Chống Replay Attack)
* **Quyền:** Public | **Cơ chế:** Token Rotation & Family Revocation.

| TC ID | Kịch bản kiểm thử | Input / Request Body | HTTP Status | Kết quả mong đợi (Expected Result) |
| :--- | :--- | :--- | :---: | :--- |
| **REF_01** | Gửi Refresh Token hợp lệ (Happy Path) | '{"refreshToken": "<valid_active_refresh_token>"}' | **200 OK** | Trả về cặp 'accessToken' + 'refreshToken' mới. Session cũ đánh dấu 'is_revoked = true, revoked_reason = 'TOKEN_ROTATED''. |
| **REF_02** | **Tấn công Replay / Dùng lại Refresh Token cũ đã bị xoay vòng** | Gửi lại chính '<old_refresh_token>' vừa được xoay vòng ở TC REF_01 | **401 Unauthorized** | 'status: 401', 'errorCode: 1014', 'message: "Phát hiện dấu hiệu rò rỉ mã bảo mật... Toàn bộ phiên bị thu hồi"'. **Family Revocation:** Toàn bộ session của user bị revoke. |
| **REF_03** | Gửi Refresh Token giả mạo / Sai chữ ký HMAC | '{"refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.fake.signature"}' | **400 Bad Request** | 'status: 400', 'errorCode: 1008', 'message: "Mã Refresh Token không hợp lệ"'. |
| **REF_04** | Gửi Refresh Token đã hết hạn (> 14 ngày) | '{"refreshToken": "<expired_refresh_token>"}' | **400 Bad Request** | 'status: 400', 'errorCode: 1007', 'message: "Phiên đăng nhập đã hết hạn"'. |

---

### 6. 'POST /api/v1/auth/logout' (Đăng xuất phiên hiện tại)
* **Quyền:** Bearer Token.

| TC ID | Kịch bản kiểm thử | Header / Request | HTTP Status | Kết quả mong đợi (Expected Result) |
| :--- | :--- | :--- | :---: | :--- |
| **OUT_01** | Đăng xuất với Token hợp lệ (Happy Path) | 'Authorization: Bearer <valid_token>' | **204 No Content** | 'status: 204'. Session tương ứng của thiết bị đổi thành 'is_revoked = true, revoked_reason = 'LOGOUT''. |
| **OUT_02** | Đăng xuất không truyền Token | Không truyền header 'Authorization' | **401 Unauthorized** | 'status: 401', 'message: "Chưa gửi Authorization Bearer Token"'. |

---

### 7. 'POST /api/v1/auth/logout-all-devices' (Cưỡng chế đăng xuất tất cả thiết bị)
* **Quyền:** Bearer Token.

| TC ID | Kịch bản kiểm thử | Header / Request | HTTP Status | Kết quả mong đợi (Expected Result) |
| :--- | :--- | :--- | :---: | :--- |
| **OUTALL_01**| Cưỡng chế đăng xuất toàn bộ thiết bị (Happy Path)| 'Authorization: Bearer <valid_token>' | **204 No Content** | 'status: 204'. 100% active sessions của user này đều chuyển thành 'is_revoked = true, revoked_reason = 'FORCE_LOGOUT''. |

---

### 8. 'POST /api/v1/auth/change-password' (Đổi mật khẩu tài khoản)
* **Quyền:** Bearer Token | **Kiểm soát:** Password Policy, thu hồi các phiên khác.

| TC ID | Kịch bản kiểm thử | Header & Request Body | HTTP Status | Kết quả mong đợi (Expected Result) |
| :--- | :--- | :--- | :---: | :--- |
| **CHPASS_01**| Đổi mật khẩu hợp lệ (Happy Path) | 'oldPassword': '"Password@123!"', 'newPassword': '"NewPassword@456!"' | **204 No Content** | 'status: 204'. Cập nhật mật khẩu băm mới, 'password_changed_at = now()'. Toàn bộ phiên trên thiết bị khác bị thu hồi. |
| **CHPASS_02**| Nhập sai mật khẩu cũ ('oldPassword') | 'oldPassword': '"WrongOldPass@123"', 'newPassword': '"NewPassword@456!"' | **400 Bad Request** | 'status: 400', 'errorCode: 1003', 'message: "Mật khẩu cũ không chính xác"'. |
| **CHPASS_03**| Mật khẩu mới không đạt chuẩn Password Policy | 'newPassword': '"1234"' | **400 Bad Request** | 'status: 400', 'errorCode: 1012', 'message: "Mật khẩu phải chứa ít nhất 8 ký tự..."'. |

---

## PHẦN 2: NHÓM API ĐĂNG NHẬP KHÔNG MẬT KHẨU BẰNG MÃ QR (PASSWORDLESS QR)

### 9. 'POST /api/v1/auth/qr/init' (Khởi tạo phiên QR trên Web)
* **Quyền:** Public | **TTL:** 120 giây.

| TC ID | Kịch bản kiểm thử | Header / Request | HTTP Status | Kết quả mong đợi (Expected Result) |
| :--- | :--- | :--- | :---: | :--- |
| **QR_01** | Web khởi tạo QR Session thành công | 'X-Device-Id: web_chrome_01' | **200 OK** | Trả về 'sessionId' dạng 'qr_xxx', 'qrCodeUri: "portfolio://qr-login?session=qr_xxx"', 'wsTopic: "/topic/qr-login/qr_xxx"', 'expiresInSeconds: 120'. |

---

### 10. 'POST /api/v1/auth/qr/scan' (Mobile quét mã QR)
* **Quyền:** Bearer Token (App Mobile đã đăng nhập).

| TC ID | Kịch bản kiểm thử | Header & Request Body | HTTP Status | Kết quả mong đợi (Expected Result) |
| :--- | :--- | :--- | :---: | :--- |
| **QR_02** | Quét mã QR hợp lệ còn hiệu lực | 'Authorization: Bearer <mobile_token>'<br>'{"sessionId": "qr_xxx", "mobileDeviceId": "iphone_15"}' | **204 No Content** | 'status: 204'. Phiên trong DB đổi 'status = 'SCANNED'', 'mobile_user_id = user_id'. |
| **QR_03** | Quét mã QR đã hết hạn (> 120 giây) | 'sessionId' đã tạo trước 3 phút | **400 Bad Request** | 'status: 400', 'errorCode: 1017', 'message: "Mã QR đã hết hạn, vui lòng làm mới"'. |
| **QR_04** | Quét mã QR không tồn tại | 'sessionId': '"qr_not_found"' | **400 Bad Request** | 'status: 400', 'errorCode: 1018', 'message: "Phiên quét mã QR không tồn tại"'. |

---

### 11. 'POST /api/v1/auth/qr/confirm' (Mobile xác nhận đăng nhập QR)
* **Quyền:** Bearer Token (App Mobile).

| TC ID | Kịch bản kiểm thử | Header & Request Body | HTTP Status | Kết quả mong đợi (Expected Result) |
| :--- | :--- | :--- | :---: | :--- |
| **QR_05** | Mobile xác nhận FaceID/PIN thành công | 'Authorization: Bearer <mobile_token>'<br>'{"sessionId": "qr_xxx", "pin": "123456"}' | **200 OK** | Trả về 'data: "exc_xxx"' ('exchangeAuthCode', hiệu lực 10s). DB đổi 'status = 'CONFIRMED''. |
| **QR_06** | Mobile xác nhận mã QR đã hết hạn | Session quá 120s | **400 Bad Request** | 'status: 400', 'errorCode: 1017', 'message: "Mã QR đã hết hạn"'. |

---

### 12. 'POST /api/v1/auth/qr/exchange' (Web đổi code lấy Token)
* **Quyền:** Public | **TTL:** 10 giây (Single-Use).

| TC ID | Kịch bản kiểm thử | Request Body | HTTP Status | Kết quả mong đợi (Expected Result) |
| :--- | :--- | :--- | :---: | :--- |
| **QR_07** | Đổi 'exchangeAuthCode' hợp lệ trong 10s (Happy Path) | '{"sessionId": "qr_xxx", "exchangeAuthCode": "exc_xxx", "webDeviceId": "web_chrome_01"}' | **200 OK** | Trả về cặp 'accessToken' + 'refreshToken' của User Mobile cho Web. 'exchangeAuthCode' bị hủy (Single-Use). |
| **QR_08** | Gửi lại chính mã 'exchangeAuthCode' vừa đổi (Replay) | Gửi lại 'exc_xxx' lần 2 | **400 Bad Request** | 'status: 400', 'errorCode: 1018', 'message: "Mã xác thực đổi Token không hợp lệ hoặc đã sử dụng"'. |
| **QR_09** | Đổi mã sau khi quá 10 giây | Gửi request sau 15 giây kể từ lúc confirm | **400 Bad Request** | 'status: 400', 'errorCode: 1017', 'message: "Mã đổi Token đã hết hạn"'. |

---

## PHẦN 3: NHÓM API QUẢN TRỊ THIẾT BỊ, PHIÊN LÀM VIỆC & SMARTOTP

### 13. 'GET /api/v1/auth/devices' (Xem danh sách thiết bị)
* **Quyền:** Bearer Token.

| TC ID | Kịch bản kiểm thử | Header | HTTP Status | Kết quả mong đợi (Expected Result) |
| :--- | :--- | :--- | :---: | :--- |
| **DEV_01** | Lấy danh sách thiết bị của tài khoản | 'Authorization: Bearer <valid_token>' | **200 OK** | Trả về danh sách JSON gồm: 'deviceId', 'deviceName', 'platform', 'isTrusted', 'lastActiveAt', 'status'. |

---

### 14. 'DELETE /api/v1/auth/devices/{deviceId}' (Thu hồi thiết bị)
* **Quyền:** Bearer Token | **Audit:** Tự động ghi bản ghi 'DELETE' vào 'audit_logs'.

| TC ID | Kịch bản kiểm thử | Param / Header | HTTP Status | Kết quả mong đợi (Expected Result) |
| :--- | :--- | :--- | :---: | :--- |
| **DEV_02** | Thu hồi quyền của một thiết bị | 'deviceId': '"dev_brand_new_999"' | **204 No Content** | 'status: 204'. Thiết bị chuyển 'status = 'REVOKED'', 'isTrusted = false'. Ghi 1 bản ghi vào 'audit_logs'. |
| **DEV_03** | Thu hồi thiết bị không tồn tại | 'deviceId': '"dev_fake_xyz"' | **404 Not Found** | 'status: 404', 'errorCode: 1004', 'message: "Không tìm thấy thiết bị"'. |

---

### 15. 'GET /api/v1/auth/sessions' (Xem danh sách Active Sessions)
* **Quyền:** Bearer Token.

| TC ID | Kịch bản kiểm thử | Header | HTTP Status | Kết quả mong đợi (Expected Result) |
| :--- | :--- | :--- | :---: | :--- |
| **SESS_01**| Lấy danh sách các phiên hoạt động | 'Authorization: Bearer <valid_token>'<br>'X-Session-ID: sess_current_123' | **200 OK** | Trả về danh sách session 'isRevoked = false'. Session hiện tại có 'isCurrentSession: true'. |

---

### 16. 'POST /api/v1/auth/smart-otp/setup' (Khởi tạo SmartOTP RFC 6238)
* **Quyền:** Bearer Token.

| TC ID | Kịch bản kiểm thử | Header | HTTP Status | Kết quả mong đợi (Expected Result) |
| :--- | :--- | :--- | :---: | :--- |
| **SOTP_01**| Khởi tạo Base32 Secret Key thành công | 'Authorization: Bearer <valid_token>' | **200 OK** | Trả về 'secret' (chuỗi Base32 160-bit), 'qrBarcodeUri: "otpauth://totp/..."', 'issuer: "PortfolioEngine"'. |

---

### 17. 'POST /api/v1/auth/smart-otp/verify' (Kích hoạt SmartOTP)
* **Quyền:** Bearer Token.

| TC ID | Kịch bản kiểm thử | Request Body | HTTP Status | Kết quả mong đợi (Expected Result) |
| :--- | :--- | :--- | :---: | :--- |
| **SOTP_02**| Nhập đúng mã 6 số TOTP và mã PIN bảo vệ | '{"otpCode": "<6_so_authenticator>", "pin": "123456"}' | **204 No Content** | 'status: 204'. Cập nhật 'is_smart_otp_enrolled = true' trên thiết bị và mã hóa PIN bằng BCrypt trong DB. |
| **SOTP_03**| Nhập sai mã 6 số TOTP | '{"otpCode": "000000", "pin": "123456"}' | **400 Bad Request** | 'status: 400', 'errorCode: 1018', 'message: "Mã SmartOTP không chính xác"'. |

---

### 18. 'GET /api/v1/auth/me' (Xem thông tin tài khoản hiện tại)
* **Quyền:** Bearer Token.

| TC ID | Kịch bản kiểm thử | Header | HTTP Status | Kết quả mong đợi (Expected Result) |
| :--- | :--- | :--- | :---: | :--- |
| **ME_01** | Lấy thông tin tài khoản đang đăng nhập | 'Authorization: Bearer <valid_token>' | **200 OK** | Trả về 'id', 'username', 'email', 'tenantId', danh sách 'roles' và 'permissions'. |
| **ME_02** | Gọi không truyền Token | Không truyền header | **401 Unauthorized** | 'status: 401', 'message: "Chưa gửi Authorization Bearer Token"'. |

---

## PHẦN 4: NHÓM API QUẢN TRỊ VAI TRÒ & PHÂN QUYỀN RBAC (ROLES & PERMISSIONS)

### 19. 'GET /api/v1/roles' (Danh sách vai trò)
* **Quyền yêu cầu:** 'user:read' hoặc 'auth:login'.

| TC ID | Kịch bản kiểm thử | Role / Header | HTTP Status | Kết quả mong đợi (Expected Result) |
| :--- | :--- | :--- | :---: | :--- |
| **ROLE_01**| Super Admin hoặc Quản trị viên gọi API | Token có quyền 'user:read' | **200 OK** | Trả về danh sách vai trò: 'ROLE_SUPER_ADMIN', 'ROLE_CORP_ADMIN', 'ROLE_SALE_MANAGER', 'ROLE_CUSTOMER'... |
| **ROLE_02**| Tài khoản không có quyền gọi API | Token thiếu quyền 'user:read' | **403 Forbidden** | 'status: 403', 'errorCode: 1006', 'message: "Bạn không có quyền thực hiện thao tác này"'. |

---

### 20. 'GET /api/v1/roles/permissions' (Danh mục quyền hạn hệ thống)
* **Quyền yêu cầu:** 'user:read'.

| TC ID | Kịch bản kiểm thử | Role / Header | HTTP Status | Kết quả mong đợi (Expected Result) |
| :--- | :--- | :--- | :---: | :--- |
| **PERM_01**| Lấy danh mục 24 permissions hệ thống | Token có quyền 'user:read' | **200 OK** | Trả về danh sách quyền hạn: 'tour:read', 'tour:create', 'payment:charge', 'user:assign_role'... |

---

### 21. 'PUT /api/v1/roles/{roleId}/permissions' (Gán quyền hạn cho vai trò)
* **Quyền yêu cầu:** 'user:assign_role' | **Audit:** Ghi bản ghi 'UPDATE' vào 'audit_logs'.

| TC ID | Kịch bản kiểm thử | Header & Request Body | HTTP Status | Kết quả mong đợi (Expected Result) |
| :--- | :--- | :--- | :---: | :--- |
| **ASSIGN_01**| Gán danh sách quyền cho vai trò (Happy Path) | Token 'ROLE_SUPER_ADMIN'<br>'["tour:read", "tour:create", "tour:update"]' | **200 OK** | 'status: 200', 'message: "Cập nhật quyền hạn vai trò thành công"'. Bảng 'role_permissions' được cập nhật. |
| **ASSIGN_02**| User thường ('ROLE_CUSTOMER') cố tình gán quyền | Token 'ROLE_CUSTOMER' | **403 Forbidden** | 'status: 403', 'errorCode: 1006'. Aspect 'SecurityAuthorizationAspect' chặn lại lập tức. |

---

## PHẦN 5: NHÓM API QUẢN LÝ NGƯỜI DÙNG (USER MANAGEMENT)

### 22. 'GET /api/v1/users' (Danh sách người dùng có phân trang)
* **Quyền yêu cầu:** 'user:read'.

| TC ID | Kịch bản kiểm thử | Param & Header | HTTP Status | Kết quả mong đợi (Expected Result) |
| :--- | :--- | :--- | :---: | :--- |
| **USER_01**| Lấy danh sách user phân trang mặc định | Token có quyền 'user:read'<br>'page=0&size=10' | **200 OK** | Trả về 'PageResponse' gồm 'content', 'pageNumber: 0', 'pageSize: 10', 'totalElements', 'totalPages'. |
| **USER_02**| User không có quyền truy cập danh sách | Token không có 'user:read' | **403 Forbidden** | 'status: 403', 'errorCode: 1006'. |

---

### 23. 'DELETE /api/v1/users/{id}' (Xóa mềm người dùng)
* **Quyền yêu cầu:** 'user:delete' | **Cơ chế:** Soft Delete ('deleted_at = now()') | **Audit:** Ghi bản ghi 'DELETE' vào 'audit_logs'.

| TC ID | Kịch bản kiểm thử | Param & Header | HTTP Status | Kết quả mong đợi (Expected Result) |
| :--- | :--- | :--- | :---: | :--- |
| **UDEL_01**| Xóa người dùng tồn tại (Happy Path) | Token có quyền 'user:delete'<br>'id = 4' | **204 No Content** | 'status: 204', 'message: "Xóa người dùng thành công"'. User được đánh dấu 'deleted_at', không thể đăng nhập tiếp. |
| **UDEL_02**| Xóa người dùng không tồn tại | 'id = 999999' | **404 Not Found** | 'status: 404', 'errorCode: 1001', 'message: "Không tìm thấy thông tin tài khoản"'. |
| **UDEL_03**| User không có quyền thực hiện xóa | Token thiếu quyền 'user:delete' | **403 Forbidden** | 'status: 403', 'errorCode: 1006'. |

---

# 🛡️ KIỂM TRA BẢO MẬT XUYÊN SUỐT (CROSS-CUTTING SECURITY ASSERTIONS)

Sau khi chạy xong các kịch bản kiểm thử trên, kiểm tra lại 2 bảng cơ sở dữ liệu để xác nhận tính toàn vẹn:

1. **Bảng 'security_login_histories':**
   * Mọi lượt đăng nhập thành công ghi 'login_status = 'SUCCESS', risk_score = 0'.
   * Mọi lượt đăng nhập sai mật khẩu ghi 'login_status = 'WRONG_PASSWORD', risk_score = 50'.
   * Thách thức thiết bị lạ ghi 'login_status = 'UNTRUSTED_DEVICE_CHALLENGE''.
   * Lượt đăng nhập khi bị khóa ghi 'login_status = 'LOCKED''.

2. **Bảng 'audit_logs':**
   * Mọi API có gắn nhãn '@AuditLog' (như 'REGISTER', 'LOGIN', 'LOGOUT', 'UPDATE', 'DELETE') đều sinh 1 dòng log chứa đầy đủ 26 trường: 'trace_id', 'tenant_id', 'user_id', 'client_ip', 'platform', 'device_id', 'execution_time_ms'.
   * Các trường nhạy cảm như 'password', 'secret', 'smartOtpPin' **đều được tự động làm mờ thành '***'**.
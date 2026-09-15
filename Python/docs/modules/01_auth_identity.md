# 🔐 Phân Hệ 01: Xác Thực & Quản Lý Danh Tính (Auth & Identity Management)

## 1. Tổng Quan & Mục Tiêu
Phân hệ **Auth & Identity** đảm nhận nhiệm vụ kiểm soát danh tính, bảo mật phiên làm việc và cấp phát chứng thư số (JWT Token) cho toàn bộ người dùng trong hệ thống FinTech.

### Trạng thái triển khai:
- ✅ **Đã hoàn thiện**: Đăng ký tài khoản, Đăng nhập cấp cặp bài trùng JWT, Xoay vòng Refresh Token (Token Rotation), Thu hồi phiên (Logout/Revoke), Kích hoạt tài khoản qua Email Link & OTP 6 số.
- ⏳ **Kế hoạch tương lai (Roadmap)**: Đăng nhập sinh trắc học (Passkeys/WebAuthn), Xác thực 2 bước (TOTP Google Authenticator), Đăng nhập OAuth2 (Google, Apple).

---

## 2. Kiến Trúc Bảo Mật & Luồng Xử Lý (Security Architecture)

### 2.1. Chu trình Token & Quản lý Phiên
1. **Access Token**:
   - Thuật toán: 'HS256'
   - Thời hạn: **30 phút** ('ACCESS_TOKEN_EXPIRE_MINUTES = 30')
   - Chứa Claims: 'sub' (User ID), 'username', 'permissions' (Mảng quyền hạn), 'modules' (Phân hệ được phép truy cập), 'jti' (Mã định danh phiên).
   - Kiểm tra chữ ký: Giải mã trực tiếp trên RAM, 'leeway = 0s' (chống replay token quá hạn).
2. **Refresh Token & Token Rotation**:
   - Thuật toán: 'HS256' (Secret key độc lập 'JWT_REFRESH_SECRET_KEY')
   - Thời hạn: **7 ngày** ('REFRESH_TOKEN_EXPIRE_DAYS = 7')
   - Lưu trữ: Lưu hash SHA-256 trong bảng 'user_sessions'.
   - Cơ chế xoay vòng: Mỗi lần gọi '/refresh-token', JTI cũ bị vô hiệu hóa ('is_revoked = 1'), hệ thống nạp lại ma trận quyền mới nhất từ DB và cấp Token mới.
   - **Chống Replay Attack**: Nếu phát hiện client gửi Refresh Token có JTI đã bị thu hồi trước đó, hệ thống lập tức khóa toàn bộ phiên của User đó.

### 2.2. Quy trình Đăng Ký & Kích Hoạt 2 Lớp (Double Verification)
1. **Bước 1 ('POST /auth/register')**: 
   - Kiểm tra trùng lặp Email, SĐT, Username.
   - Hash mật khẩu bằng **Bcrypt**.
   - Tạo User ở trạng thái 'PENDING'.
   - Sinh 'link_token' thời hạn 15 phút, gửi email thông báo.
2. **Bước 2 ('GET /auth/activate?token=...')**:
   - Kiểm tra tính hợp lệ và thời hạn token.
   - Đánh dấu token 'USED', chuyển User sang 'OTP_PENDING'.
   - Sinh mã OTP 6 số ngẫu nhiên thời hạn 5 phút, gửi tới SMS/Email.
   - **Bảo mật**: Tuyệt đối không trả về 'otp_code' trong JSON response.
3. **Bước 3 ('POST /auth/verify-otp')**:
   - Đối chiếu OTP 6 số.
   - Khóa chặn sau 3 lần nhập sai liên tiếp.
   - Kích hoạt User sang 'ACTIVE'.

---

## 3. Danh Sách API Endpoints

| Method | Endpoint | Bảo vệ (Guard) | Mô tả |
| :--- | :--- | :--- | :--- |
| 'POST' | '/api/v1/auth/register' | Public | Đăng ký tài khoản mới (trạng thái PENDING) |
| 'POST' | '/api/v1/auth/login' | Public | Đăng nhập nhận cặp Access & Refresh Token |
| 'POST' | '/api/v1/auth/refresh-token' | Public | Xoay vòng Refresh Token (Token Rotation) |
| 'POST' | '/api/v1/auth/logout' | 'Bearer Token' | Thu hồi phiên làm việc hiện tại |
| 'GET' | '/api/v1/auth/activate' | Public | Xác thực link token từ email, cấp OTP |
| 'POST' | '/api/v1/auth/verify-otp' | Public | Xác thực OTP 6 số, kích hoạt tài khoản |

---

## 4. Cấu Trúc Bảng Dữ Liệu Liên Quan
- 'users': Thông tin định danh người dùng.
- 'user_sessions': Lưu trữ JTI, hash refresh token, IP, User-Agent, trạng thái thu hồi 'is_revoked'.
- 'user_otps': Quản lý OTP 6 số, số lần thử sai 'attempts_count', thời gian hết hạn 'expires_at'.
- 'notifications' & 'notification_logs': Lưu vết email kích hoạt và trạng thái gửi SMTP.
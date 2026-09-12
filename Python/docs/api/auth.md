# 🔐 API HƯỚNG DẪN: PHÂN HỆ XÁC THỰC & PHIÊN LÀM VIỆC (AUTHENTICATION)

## 📌 1. Tổng Quan & Mục Đích
Phân hệ **Authentication** chịu trách nhiệm quản lý vòng đời định danh của người dùng trong hệ thống FinTech:
- **Đăng ký tài khoản (Register)**: Tiếp nhận thông tin, khởi tạo tài khoản ở trạng thái `PENDING`, gửi Link Token kích hoạt.
- **Kích hoạt Link Token (Activate Link)**: Xác thực link token hợp lệ, chuyển tài khoản sang `OTP_PENDING` và sinh mã OTP 6 số.
- **Xác thực OTP (Verify OTP)**: Kích hoạt tài khoản lên `ACTIVE`, tự động gán Role `USER` và khởi tạo Ví tiền mặt mặc định (`CASH_VND`).
- **Đăng nhập (Login)**: Xác thực mật khẩu Bcrypt, nạp ma trận quyền (Permissions & Modules), cấp cặp bài trùng Token JTI (Access Token 30m + Refresh Token 7d).
- **Làm mới Token (Refresh Token)**: Cơ chế **Token Rotation** an toàn, thu hồi session cũ, cấp cặp token mới.
- **Đăng xuất (Logout)**: Hủy phiên làm việc trong `user_sessions`, vô hiệu hóa Refresh Token.

---

## 🚀 2. Chi Tiết Các Endpoint

### 2.1 Đăng Ký Tài Khoản Mới
- **Endpoint**: `POST /api/v1/auth/register`
- **Mục đích**: Người dùng đăng ký tài khoản thành viên mới.
- **Khi nào dùng**: Tại màn hình Đăng ký trên Web / Mobile App.
- **Payload Request**:
```json
{
  "username": "nguyen_van_a",
  "email": "vana@gmail.com",
  "phone_number": "0912345678",
  "password": "SecurePassword123!",
  "confirm_password": "SecurePassword123!",
  "full_name": "Nguyễn Văn A",
  "date_of_birth": "1998-05-15",
  "gender": "MALE"
}
```
- **Response Thành Công (201 Created)**:
```json
{
  "success": true,
  "error_code": "001",
  "message": "Đăng ký tài khoản thành công! Vui lòng kiểm tra email để kích hoạt.",
  "data": {
    "user_id": "bfe20456-36ac-4239-b8cc-7da285e86b70",
    "username": "nguyen_van_a",
    "email": "vana@gmail.com",
    "status": "PENDING"
  }
}
```

---

### 2.2 Kích Hoạt Link Token
- **Endpoint**: `GET /api/v1/auth/activate?token={LINK_TOKEN}`
- **Mục đích**: Xác thực đường link kích hoạt được gửi qua email người dùng.
- **Khi nào dùng**: Khi người dùng nhấn vào nút "Kích hoạt tài khoản" trong Email.
- **Response Thành Công (200 OK)**:
```json
{
  "success": true,
  "error_code": "TOKEN_VALID_OTP_GENERATED",
  "message": "Liên kết hợp lệ! Vui lòng nhập mã OTP được gửi tới số điện thoại/email.",
  "context": {
    "notification_id": "notif-uuid-123",
    "user_id": "bfe20456-36ac-4239-b8cc-7da285e86b70"
  }
}
```

---

### 2.3 Xác Thực Mã OTP Kích Hoạt
- **Endpoint**: `POST /api/v1/auth/verify-otp`
- **Mục đích**: Xác nhận mã OTP 6 số để hoàn tất kích hoạt tài khoản.
- **Khi nào dùng**: Người dùng nhập 6 chữ số OTP trên giao diện kích hoạt.
- **Payload Request**:
```json
{
  "user_id": "bfe20456-36ac-4239-b8cc-7da285e86b70",
  "notification_id": "notif-uuid-123",
  "otp_code": "771625"
}
```
- **Response Thành Công (200 OK)**:
```json
{
  "success": true,
  "error_code": "ACCOUNT_ACTIVATION_SUCCESS",
  "message": "Kích hoạt tài khoản thành công! Bạn có thể đăng nhập ngay.",
  "context": {}
}
```

---

### 2.4 Đăng Nhập Hệ Thống
- **Endpoint**: `POST /api/v1/auth/login`
- **Mục đích**: Xác thực người dùng và cấp JWT Access Token + Refresh Token.
- **Khi nào dùng**: Tại màn hình Đăng nhập.
- **Payload Request**:
```json
{
  "email": "vana@gmail.com",
  "password": "SecurePassword123!"
}
```
- **Response Thành Công (200 OK)**:
```json
{
  "success": true,
  "error_code": "002",
  "message": "Đăng nhập thành công.",
  "data": {
    "user_id": "bfe20456-36ac-4239-b8cc-7da285e86b70",
    "username": "nguyen_van_a",
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "token_type": "Bearer",
    "permissions": ["WALLET_LIST", "WALLET_CREATE", "TRANSACTION_VIEW"],
    "modules": ["WALLET_MGMT", "TRANSACTION_MGMT", "USER_MGMT"],
    "wallets": [
      {
        "id": "wallet-uuid-001",
        "name": "Ví Tiền Mặt Chính",
        "wallet_code": "CASH_BFE20456",
        "wallet_type": "CASH",
        "balance": "0.0000",
        "currency": "VND"
      }
    ]
  }
}
```

---

### 2.5 Làm Mới Access Token (Refresh Token)
- **Endpoint**: `POST /api/v1/auth/refresh-token`
- **Mục đích**: Cấp mới Access Token khi Access Token cũ hết hạn (30 phút).
- **Payload Request**:
```json
{
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

### 2.6 Đăng Xuất (Logout)
- **Endpoint**: `POST /api/v1/auth/logout`
- **Mục đích**: Hủy phiên làm việc hiện tại và thu hồi Refresh Token trong DB.
- **Header**: `Authorization: Bearer <ACCESS_TOKEN>`

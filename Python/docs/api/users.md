# 👤 API HƯỚNG DẪN: PHÂN HỆ HỒ SƠ NGƯỜI DÙNG & BẢO MẬT (USERS)

## 📌 Mục đích & Nghiệp vụ
Cung cấp các API quản lý thông tin cá nhân, cập nhật hồ sơ, đổi mật khẩu và khôi phục mật khẩu quên qua mã OTP.

---

## 1. Lấy Thông Tin Cá Nhân (Profile)
- **Endpoint**: 'GET /api/v1/users/me'
- **Khi nào dùng**: Khi mở trang Profile hoặc hiển thị tên/avatar người dùng trên Header ứng dụng.
- **Response Mẫu**:
'''json
{
  "success": true,
  "error_code": "USER_PROFILE_FETCH_SUCCESS",
  "message": "Lấy thông tin hồ sơ thành công.",
  "data": {
    "user_id": "bfe20456-36ac-4239-b8cc-7da285e86b70",
    "username": "user_demo",
    "email": "user_demo@gmail.com",
    "phone_number": "0988888888",
    "full_name": "Nguyễn Văn Demo",
    "date_of_birth": "1998-05-15",
    "gender": "MALE",
    "status": "ACTIVE",
    "is_active": true,
    "roles": ["USER"]
  }
}
'''

---

## 2. Cập Nhật Hồ Sơ Cá Nhân
- **Endpoint**: 'PUT /api/v1/users/me'
- **Payload**:
'''json
{
  "full_name": "Nguyễn Văn Đã Sửa",
  "date_of_birth": "1998-10-20",
  "gender": "MALE"
}
'''

---

## 3. Đổi Mật Khẩu
- **Endpoint**: 'POST /api/v1/users/change-password'
- **Payload**:
'''json
{
  "old_password": "OldPassword123!",
  "new_password": "NewPassword456!",
  "confirm_password": "NewPassword456!"
}
'''

---

## 4. Quên Mật Khẩu (Gửi OTP)
- **Endpoint**: 'POST /api/v1/users/forgot-password'
- **Payload**: '{"email": "user_demo@gmail.com"}'

---

## 5. Đặt Lại Mật Khẩu Bằng OTP
- **Endpoint**: 'POST /api/v1/users/reset-password'
- **Payload**:
'''json
{
  "email": "user_demo@gmail.com",
  "otp_code": "123456",
  "new_password": "ResetPassword789!",
  "confirm_password": "ResetPassword789!"
}
'''

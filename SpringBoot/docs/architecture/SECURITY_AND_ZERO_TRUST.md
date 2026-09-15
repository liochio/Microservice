# KIẾN TRÚC BẢO MẬT & ZERO-TRUST SECURITY

> **Tiêu chuẩn áp dụng:** OWASP Top 10, Zero-Trust Architecture, NIST 800-63B  
> **Cơ chế xác thực:** Stateless JWT (Access Token 1h) + Stateful Refresh Token (7 days, Database Hash & Rotation)  
> **Cơ chế 2FA:** SmartOTP (TOTP Time-based OTP RFC 6238) + SMS/Email OTP  
> **Quản trị thiết bị:** Device Fingerprinting (SHA-256 canvas + platform hash)  

---

## 1. MÔ HÌNH BẢO MẬT 2 LỚP (TWO-TIER ACCESS CONTROL)

Hệ thống áp dụng cơ chế kiểm soát 2 cổng độc lập:

'''
[ Request Đến Endpoint Nghiệp Vụ ]
                │
                ▼
┌────────────────────────────────────────────────────────┐
│ CỔNG 1: KIỂM TRA TÍNH NĂNG TENANT (FEATURE GATING)     │
│ - Super Admin cấp phép qua bảng 'tenant_features'       │
│ - Tenant có được bật module này không?                 │
│ - Quota limit và hạn sử dụng (expired_at) còn hiệu lực?│
└────────────────────────────────────────────────────────┘
                │ (Hợp lệ)
                ▼
┌────────────────────────────────────────────────────────┐
│ CỔNG 2: PHÂN QUYỀN VAI TRÒ NGƯỜI DÙNG (DYNAMIC RBAC)   │
│ - Tenant Admin phân quyền qua bảng 'role_permissions'  │
│ - User thuộc vai trò nào?                              │
│ - Có quyền hành động tài nguyên (@RequirePermission)?  │
└────────────────────────────────────────────────────────┘
                │ (Hợp lệ)
                ▼
      [ Thực Thi Logic Nghiệp Vụ ]
'''

---

## 2. QUẢN LÝ THIẾT BỊ TIN CẬY & PHÂN TÍCH RỦI RO IP

1. **Device Fingerprint Identification ('user_devices')**:
   - Khi Client gửi request, API Gateway tạo chữ ký thiết bị 'device_id' từ IP, User-Agent, Screen resolution và OS.
   - Nếu đăng nhập từ một thiết bị mới chưa được đánh dấu 'is_trusted = true', hệ thống lập tức kích hoạt thử thách 2FA (SmartOTP / Email OTP Challenge).

2. **Theo vết & Khóa Bruteforce ('security_login_histories')**:
   - Mỗi lần đăng nhập thất bại tăng 'failed_login_attempts' trong bảng 'users'.
   - Nếu vượt quá 5 lần liên tiếp, tài khoản tự động bị khóa tạm thời trong 15 phút ('lockout_until').

3. **Thu hồi phiên làm việc tức thì (Remote Session Revoke - 'user_sessions')**:
   - Người dùng có thể xem danh sách tất cả các thiết bị đang đăng nhập của mình và bấm "Đăng xuất thiết bị này" từ xa qua API 'POST /api/auth/security/sessions/{sessionId}/revoke'.

---

## 3. LÀM SẠCH DỮ LIỆU ĐẦU VÀO ĐỘNG (DYNAMIC SANITIZATION)

- Cơ chế '@DynamicSanitize' loại bỏ nguy cơ tấn công XSS, Script Injection và SQL Injection ngay từ tầng Jackson Deserializer.
- Các quy tắc Regex ('input_sanitization_rules') được lưu trữ tại DB và nạp tự động vào bộ nhớ đệm L1 Cache (Caffeine) với thời gian làm mới định kỳ.

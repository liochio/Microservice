# TÀI LIỆU THIẾT KẾ HOÀN CHỈNH: PHÂN HỆ XÁC THỰC, BẢO MẬT, PHÂN QUYỀN & AUDIT LOGGING (ENTERPRISE STANDARD)

---

## 1. NGUYÊN TẮC THIẾT KẾ VÀ KIẾN TRÚC BẢO MẬT LÕI

* **Mô hình Đa người thuê Phân tầng (Hierarchical Multi-Tenancy):** Phân lập triệt để dữ liệu giữa các khách thuê qua 'tenant_id'. Hỗ trợ 2 nhóm tài khoản chính: Tenant Doanh nghiệp ('ENTERPRISE') quản lý cây tài khoản con ('SUB_ACCOUNT'), và Tenant Cá nhân ('INDIVIDUAL').
* **Kiến trúc Zero Trust & Defense-in-Depth:** Mọi request đều không mặc định tin cậy. Xác thực và phân quyền qua 3 chốt chặn: 'API Gateway' $\rightarrow$ 'AOP Security Filter/Aspect' tại từng Microservice $\rightarrow$ 'Row-Level Tenant Filtering' tại Database Layer.
* **Phân quyền Đa tầng (Two-Tier RBAC/ABAC):**
  * *Tầng 1 (Feature Gating - Super Admin):* Kiểm tra Tenant ('tenant_id') đã kích hoạt gói tính năng tương ứng trong 'system_features' và 'tenant_features' hay chưa.
  * *Tầng 2 (RBAC / ABAC - Tenant Scope):* Kiểm tra người dùng có đủ vai trò ('roles') và quyền hạn cụ thể ('permissions' theo chuẩn 'resource:action') trên tài nguyên thao tác.
* **Quản trị Thiết bị & Session Tracking:** Gắn chặt phiên với dấu vân tay thiết bị ('device_id'), User-Agent và IP. Khi phát hiện thiết bị lạ/mới, hệ thống kích hoạt thách thức 2FA ('UNTRUSTED_DEVICE_CHALLENGE') trước khi cấp phiên đăng nhập chính thức.
* **Xoay vòng Token & Token Reuse Detection:** Cặp Dual-Token (JWT 15 phút + Refresh Token xoay vòng 14 ngày). Nếu phát hiện Refresh Token cũ đã từng bị thu hồi được gửi lên (dấu hiệu tấn công Replay), hệ thống kích hoạt **Family Revocation** lập tức thu hồi toàn bộ các phiên của tài khoản đó.
* **Audit Logging Toàn diện & Không thể chối bỏ (Non-Repudiation):**
  * Khởi tạo 'trace_id' tại API Gateway, truyền xuyên suốt qua header 'X-Trace-Id'.
  * Tự động ghi nhận toàn bộ hoạt động đăng nhập/bảo mật vào 'security_login_histories'.
  * Bắt trọn vẹn ngữ cảnh thao tác (IP, User, Tenant, Thiết bị, App/Web, Hành động, Thời gian, Dữ liệu cũ/mới, Trạng thái) lưu vào 'audit_logs' theo thời gian thực (hỗ trợ đẩy Async).

---

## 2. QUY TRÌNH NGHIỆP VỤ & SƠ ĐỒ VẬN HÀNH

'''text
                               SƠ ĐỒ TỔNG QUAN XÁC THỰC, BẢO VỆ & AUDIT
                              
[Client HTTP Request]
         │
         ▼
[API Gateway (8080)] ──► Kiểm tra Blacklist IP / Rate Limiting
         │           ──► Giải mã JWT, so khớp X-Tenant-ID
         │           ──► Khởi tạo X-Trace-Id
         │           ──► Tiêm Headers: X-User-Id, X-Tenant-Id, X-User-Roles, X-User-Permissions
         ▼
[Microservice (tour/payment...)]
         │
         ├──► [AOP @RequirePermission / @RequireRole] ──► Kiểm tra Header Quyền ──► [403 nếu thiếu]
         │
         ├──► [Business Logic Execution] ──► Đọc / Ghi Database theo Tenant Scope
         │
         └──► [AOP @AuditLog Interceptor]
                     │
                     ▼ (Async Event / Direct DB)
             [Ghi bản ghi toàn diện vào db_core.audit_logs]
'''

### Các Luồng Nghiệp Vụ Chuẩn:

1. **Luồng 1: Đăng ký & Kích hoạt qua OTP:**
   - Đăng ký tài khoản $\rightarrow$ trạng thái 'PENDING_VERIFY'.
   - Sinh OTP 6 số $\rightarrow$ Xác thực qua endpoint '/api/v1/auth/verify-otp' $\rightarrow$ Chuyển 'ACTIVE'.
2. **Luồng 2: Đăng nhập Mật khẩu & Thách thức Thiết bị Tin cậy (Step-up Auth):**
   - Đăng nhập từ thiết bị tin cậy ('is_trusted = true') $\rightarrow$ Nhận Access Token + Refresh Token.
   - Đăng nhập từ thiết bị lạ ('is_trusted = false') $\rightarrow$ Trả 'requires2Fa = true', 'challengeToken = "DEVICE_2FA_REQUIRED"', phát OTP $\rightarrow$ Xác thực qua '/api/v1/auth/verify-device-otp' $\rightarrow$ Nhận Token và gắn nhãn Tin cậy.
3. **Luồng 3: Làm mới Token & Chống Đánh cắp (Rotation & Reuse Detection):**
   - Gửi 'refresh_token' lên '/api/v1/auth/refresh' $\rightarrow$ Cấp cặp Token mới, thu hồi token cũ.
   - Gửi lại Refresh Token cũ đã thu hồi $\rightarrow$ Kích hoạt **Family Revocation** thu hồi 100% phiên của user.
4. **Luồng 4: Đăng nhập Bằng Quét mã QR App Mobile (Passwordless):**
   - Web gọi '/api/v1/auth/qr/init' $\rightarrow$ Nhận 'session_id' (TTL 120s).
   - Mobile gọi '/api/v1/auth/qr/scan' $\rightarrow$ Chuyển 'SCANNED'.
   - Mobile gọi '/api/v1/auth/qr/confirm' $\rightarrow$ Chuyển 'CONFIRMED' và nhận 'exchange_auth_code' (TTL 10s).
   - Web gọi '/api/v1/auth/qr/exchange' $\rightarrow$ Nhận Access Token + Refresh Token.
5. **Luồng 5: Phân quyền Đa tầng & Bảo vệ Method (RBAC / ABAC):**
   - Gateway tiêm headers: 'X-User-Id', 'X-Tenant-Id', 'X-User-Roles', 'X-User-Permissions'.
   - AOP '@RequireRole' và '@RequirePermission' kiểm tra trực tiếp.
6. **Luồng 6: Tự động Ghi vết Toàn diện (Audit Logging Pipeline):**
   - '@AuditLog' và 'AuditLoggingFilter' thu thập đầy đủ 26 trường thông tin lưu vào bảng 'audit_logs'.

# THƯ VIỆN LÕI & TIỆN ÍCH DÙNG CHUNG (COMMON-LIB)

> **Phụ trách:** Cung cấp Core Security Filters, AOP Security Aspects, Mã lỗi hệ thống, Đa ngôn ngữ (i18n), JWT Token, TOTP RFC 6238, Khóa phân tán ShedLock và Audit Logging.

---

## 1. CÁC THÀNH PHẦN BẢO MẬT & TIỆN ÍCH CHÍNH

1. **Bảo mật & Phân quyền Hai tầng (Two-Tier RBAC/ABAC):**
   - '@RequireRole(String[] value)': Giới hạn theo danh sách vai trò cho phép.
   - '@RequirePermission(String[] value, Mode mode = ALL/ANY, boolean superAdminOnly)': Kiểm soát quyền hạn hạt nhân trên tài nguyên ('resource:action').
   - 'SecurityAuthorizationAspect': AOP Aspect tự động đọc 'X-User-Roles' và 'X-User-Permissions' từ Gateway, đối soát và ném '403 Forbidden' ('ErrorCode.UNAUTHORIZED').
2. **Kiểm toán Toàn diện (Audit Logging Pipeline):**
   - '@AuditLog(module = "...", action = ActionType.CREATE, description = "...")': Bắt Request/Response, tính 'execution_time_ms', tự động che các trường nhạy cảm (password, secret, token, cvv, smartOtpPin).
   - 'AuditLoggingFilter': Bộ lọc ghi vết HTTP Request/Response toàn diện.
3. **Tiện ích Xác thực & Thiết bị:**
   - 'JwtUtils': Tạo và giải mã JWT Token chứa 'userId', 'username', 'tenantId', 'roles', 'permissions', 'deviceId', 'sessionId'.
   - 'TotpProvider': Triển khai chuẩn TOTP RFC 6238 Base32 Secret và xác thực mã 6 số cho SmartOTP.
   - 'DeviceFingerprintExtractor': Bóc tách thông tin IP thực, Device ID, Platform, OS, Browser từ HTTP Request.
   - 'PasswordPolicyValidator': Kiểm tra độ mạnh mật khẩu (min 8 ký tự, chữ hoa, chữ thường, số, ký tự đặc biệt).
4. **Hạ tầng & Đa ngôn ngữ:**
   - 'ErrorCodeConstants', 'ErrorCode' enum, 'MessageConstants'.
   - Resource Bundle đa ngôn ngữ: 'messages_vi.properties', 'messages_en.properties', 'messages_zh.properties'.
   - 'ShedLockConfig': Khóa phân tán dựa trên MySQL 'shedlock' ('JdbcTemplateLockProvider'), vận hành an toàn ngay cả khi Redis offline.

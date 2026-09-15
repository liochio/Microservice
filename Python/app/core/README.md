# ⚙️ Package: 'app.core'

Package chứa các module hạ tầng cốt lõi (Infrastructure, Security, Configuration, Translation, Exceptions) của hệ thống.

## 📋 Danh sách các thành phần:

| Thư mục/File | Vai trò & Chức năng chi tiết |
| :--- | :--- |
| 'config/settings.py' | Quản lý cấu hình toàn cục từ '.env' (Database URL, Redis URL, JWKS URL, Mail, AI). |
| 'security/guard/guards.py' | 'get_current_user' (Xác thực RS256 JWT qua Public Key), 'RoleBasedGuard', 'PermissionGuard', 'RequireActionTokenGuard'. |
| 'security/jwt/jwt_service.py' | Quản lý tải và cache RSA Public Key từ JWKS của 'liochio-core', giải mã Token RS256 cục bộ không gọi HTTP. |
| 'translator/i18n_loader.py' | Engine đa ngôn ngữ thời gian thực ($O(1)$ In-Memory JSON Dictionary Engine) hỗ trợ 'vi', 'en', 'zh'. |
| 'exceptions/' | 'FintechBaseException', 'GlobalExceptionHandler' bắt và chuẩn hóa mã lỗi trả về cho Client. |
| 'idempotency/' | Dịch vụ chống trùng lặp giao dịch dựa trên header 'Idempotency-Key' lưu trong Redis. |
| 'events/' | Dispatcher và Event Listeners xử lý bất đồng bộ các sự kiện tài chính (ví dụ: số dư biến động). |

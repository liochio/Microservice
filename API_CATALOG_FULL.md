# 📚 DANH MỤC TOÀN BỘ API HỆ THỐNG LIOCHIO (FULL API SPECIFICATION)

> **Tổng cộng**: 131 API Spring Boot Core & 58 API Python FinTech & AI Core

---

## 🏛️ Dịch Vụ: `AI-SERVICE` (3 APIs)

| Phương Thức | Đường Dẫn API (Endpoint) | Chức Năng & Vai Trò | Controller |
| :---: | :--- | :--- | :--- |
| `POST` | `/api/v1/ai/chat/sessions` | Khởi tạo phiên hội thoại mới với Chatbot AI | `AiChatbotController.java` |
| `GET` | `/api/v1/ai/chat/sessions/{sessionId}/messages` | Lấy lịch sử tin nhắn của một phiên hội thoại | `AiChatbotController.java` |
| `POST` | `/api/v1/ai/chat/sessions/{sessionId}/messages` | Gửi tin nhắn và nhận phản hồi từ AI Assistant | `AiChatbotController.java` |

---

## 🏛️ Dịch Vụ: `AUTH-SERVICE` (59 APIs)

| Phương Thức | Đường Dẫn API (Endpoint) | Chức Năng & Vai Trò | Controller |
| :---: | :--- | :--- | :--- |
| `GET` | `/.well-known/jwks.json` | Lấy danh sách khóa công khai JWKS | `JwksController.java` |
| `GET` | `/api/v1/app/onboarding-status` | getMyOnboardingStatus | `CustomerGateController.java` |
| `GET` | `/api/v1/auth/.well-known/jwks.json` | Lấy danh sách khóa công khai JWKS | `JwksController.java` |
| `POST` | `/api/v1/auth/change-password` | Đổi mật khẩu tài khoản | `AuthController.java` |
| `GET` | `/api/v1/auth/devices` | Danh sách thiết bị truy cập | `AuthController.java` |
| `DELETE` | `/api/v1/auth/devices/{deviceId` | Thu hồi quyền thiết bị | `AuthController.java` |
| `POST` | `/api/v1/auth/force-logout` | Đăng xuất toàn bộ thiết bị | `AuthController.java` |
| `POST` | `/api/v1/auth/login` | Đăng nhập hệ thống | `AuthController.java` |
| `POST` | `/api/v1/auth/logout` | Đăng xuất tài khoản | `AuthController.java` |
| `POST` | `/api/v1/auth/logout-all-devices` | Đăng xuất toàn bộ thiết bị | `AuthController.java` |
| `GET` | `/api/v1/auth/me` | Lấy thông tin tài khoản hiện tại | `AuthController.java` |
| `POST` | `/api/v1/auth/qr/confirm` | Xác nhận đăng nhập QR (Mobile) | `AuthController.java` |
| `POST` | `/api/v1/auth/qr/exchange` | Đổi mã xác nhận QR lấy Token (Web) | `AuthController.java` |
| `POST` | `/api/v1/auth/qr/init` | Khởi tạo QR Login (Web) | `AuthController.java` |
| `POST` | `/api/v1/auth/qr/scan` | Quét mã QR (Mobile) | `AuthController.java` |
| `POST` | `/api/v1/auth/refresh` | Làm mới mã xác thực (Token Rotation) | `AuthController.java` |
| `POST` | `/api/v1/auth/register` | Đăng ký tài khoản | `AuthController.java` |
| `GET` | `/api/v1/auth/security/devices` | Lấy danh sách các thiết bị đã đăng nhập của người dùng | `SecurityController.java` |
| `GET` | `/api/v1/auth/security/sessions` | Lấy danh sách các phiên làm việc (Sessions) đang hoạt động | `SecurityController.java` |
| `POST` | `/api/v1/auth/security/sessions/{sessionId}/revoke` | Thu hồi từ xa một phiên làm việc (Revoke Session) | `SecurityController.java` |
| `GET` | `/api/v1/auth/sessions` | Danh sách phiên hoạt động | `AuthController.java` |
| `POST` | `/api/v1/auth/smart-otp/action-token` | Xác thực Step-up SmartOTP và cấp Action Token | `AuthController.java` |
| `POST` | `/api/v1/auth/smart-otp/setup` | Khởi tạo thiết lập SmartOTP | `AuthController.java` |
| `POST` | `/api/v1/auth/smart-otp/verify` | Kích hoạt SmartOTP | `AuthController.java` |
| `POST` | `/api/v1/auth/verify-device-otp` | Xác thực 2FA thiết bị mới | `AuthController.java` |
| `POST` | `/api/v1/auth/verify-otp` | Xác thực OTP kích hoạt | `AuthController.java` |
| `GET` | `/api/v1/corp/approvals` | listRequests | `ApprovalController.java` |
| `POST` | `/api/v1/corp/approvals/submit` | submitRequest | `ApprovalController.java` |
| `GET` | `/api/v1/corp/approvals/{id` | getRequestById | `ApprovalController.java` |
| `POST` | `/api/v1/corp/approvals/{id}/action` | actionRequest | `ApprovalController.java` |
| `POST` | `/api/v1/corp/configs/{configKey}/override` | setTenantOverride | `ConfigMatrixController.java` |
| `GET` | `/api/v1/corp/customer-gates` | listCustomerGates | `CustomerGateController.java` |
| `GET` | `/api/v1/corp/customer-gates/{userId` | getCustomerGate | `CustomerGateController.java` |
| `POST` | `/api/v1/corp/customer-gates/{userId}/gate1-review` | reviewGate1 | `CustomerGateController.java` |
| `POST` | `/api/v1/corp/customer-gates/{userId}/gate2-tier` | assignGate2 | `CustomerGateController.java` |
| `POST` | `/api/v1/corp/customer-gates/{userId}/gate3-wallets` | provisionGate3 | `CustomerGateController.java` |
| `POST` | `/api/v1/corp/customer-gates/{userId}/gate4-pair` | pairGate4 | `CustomerGateController.java` |
| `POST` | `/api/v1/ekyc/approve/{userId` | Duyệt hồ sơ eKYC và nâng cấp Tier hạn mức (Dành cho Admin) | `EkycController.java` |
| `GET` | `/api/v1/ekyc/status` | Xem trạng thái định danh eKYC và hạn mức giao dịch hiện tại | `EkycController.java` |
| `POST` | `/api/v1/ekyc/submit` | Gửi hồ sơ định danh CCCD/Hộ chiếu để nâng cấp Tier | `EkycController.java` |
| `GET` | `/api/v1/ledger/account/{accountNo` | Tra cứu số dư tài khoản sổ cái Core Banking theo mã tài khoản (Dành cho Corp và Retail) | `LedgerController.java` |
| `GET` | `/api/v1/ledger/balance` | Tra cứu số dư tài khoản đa trạng thái (Available, Holding, Escrow) của chính mình | `LedgerController.java` |
| `GET` | `/api/v1/ledger/balance/{userId` | Tra cứu số dư tài khoản của một người dùng theo User ID (Dành cho Admin/Core) | `LedgerController.java` |
| `GET` | `/api/v1/ledger/history/{userId` | Tra cứu sao kê lịch sử sổ cái kép của một người dùng | `LedgerController.java` |
| `POST` | `/api/v1/ledger/m2m/transaction` | Cổng Machine-to-Machine (M2M) nhận lệnh hạch toán từ Python FinTech có kiểm tra chữ ký HMAC-SHA256 | `LedgerController.java` |
| `GET` | `/api/v1/menus/tree` | getMenuTree | `DynamicMenuController.java` |
| `GET` | `/api/v1/roles` | Lấy danh sách tất cả các vai trò | `RoleController.java` |
| `GET` | `/api/v1/roles/permissions` | Lấy danh sách tất cả các quyền hạn trong hệ thống | `RoleController.java` |
| `PUT` | `/api/v1/roles/{roleId}/permissions` | Gán danh sách quyền hạn cho một vai trò | `RoleController.java` |
| `GET` | `/api/v1/system/configs` | getConfigs | `ConfigMatrixController.java` |
| `POST` | `/api/v1/system/configs/global` | updateGlobalConfig | `ConfigMatrixController.java` |
| `GET` | `/api/v1/tenants` | Lấy danh sách tất cả các Tenant trong hệ thống | `TenantManagementController.java` |
| `POST` | `/api/v1/tenants` | Khởi tạo một Tenant mới (Cá nhân / Doanh nghiệp) | `TenantManagementController.java` |
| `GET` | `/api/v1/tenants/features/catalog` | Lấy danh mục tính năng hệ thống (Feature Catalog) | `TenantManagementController.java` |
| `GET` | `/api/v1/tenants/{tenantId}/features` | Lấy danh sách tính năng được cấp phát của Tenant | `TenantManagementController.java` |
| `GET` | `/api/v1/users` | Lấy danh sách người dùng có phân trang | `UserController.java` |
| `GET` | `/api/v1/users/{id` | Lấy chi tiết người dùng theo ID | `UserController.java` |
| `DELETE` | `/api/v1/users/{id` | Xóa mềm người dùng theo ID | `UserController.java` |
| `GET` | `/produces = MediaType.APPLICATION_JSON_VALUE` | Lấy danh sách khóa công khai JWKS | `JwksController.java` |

---

## 🏛️ Dịch Vụ: `ENTITY-SERVICE` (17 APIs)

| Phương Thức | Đường Dẫn API (Endpoint) | Chức Năng & Vai Trò | Controller |
| :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/entities` | Tìm kiếm danh sách dynamic entities có phân trang | `DynamicEntityController.java` |
| `POST` | `/api/v1/entities` | Tạo mới dynamic entity kèm schema validation & versioning | `DynamicEntityController.java` |
| `GET` | `/api/v1/entities/templates` | Lấy danh sách các mẫu giao diện Website có sẵn (Web Templates) | `TemplateController.java` |
| `GET` | `/api/v1/entities/templates/types` | Lấy danh sách tất cả các loại thực thể động (Entity Types) | `TemplateController.java` |
| `GET` | `/api/v1/entities/{entityType}/{slug` | Lấy chi tiết dynamic entity theo entityType và slug | `DynamicEntityController.java` |
| `PUT` | `/api/v1/entities/{id` | Cập nhật dynamic entity kèm snapshot revision mới | `DynamicEntityController.java` |
| `DELETE` | `/api/v1/entities/{id` | Xóa mềm dynamic entity | `DynamicEntityController.java` |
| `GET` | `/api/v1/entities/{id}/revisions` | Xem lịch sử các phiên bản revision của thực thể | `DynamicEntityController.java` |
| `POST` | `/api/v1/entities/{id}/rollback/{revisionNumber` | Hoàn tác (Rollback) thực thể về một phiên bản revision cụ thể trong quá khứ | `DynamicEntityController.java` |
| `PATCH` | `/api/v1/entities/{id}/status` | Chuyển đổi trạng thái workflow của thực thể (DRAFT -> PENDING_REVIEW -> PUBLISHED -> ARCHIVED) | `DynamicEntityController.java` |
| `POST` | `/api/v1/forms` | Lưu hoặc cập nhật định nghĩa form | `FormDefinitionController.java` |
| `GET` | `/api/v1/forms/{formCode` | Lấy cấu trúc định nghĩa form theo formCode | `FormDefinitionController.java` |
| `POST` | `/api/v1/menus` | Lưu hoặc cập nhật cấu trúc menu | `NavigationMenuController.java` |
| `GET` | `/api/v1/menus/{menuCode` | Lấy cấu trúc menu theo menuCode | `NavigationMenuController.java` |
| `GET` | `/api/v1/system/databases/health` | Kiểm tra kết nối toàn bộ Database | `DatabaseHealthController.java` |
| `POST` | `/api/v1/ui-configs` | Lưu hoặc cập nhật cấu trúc layout UI của trang | `UiConfigController.java` |
| `GET` | `/api/v1/ui-configs/{pageCode` | Lấy cấu trúc layout JSON của một trang theo pageCode | `UiConfigController.java` |

---

## 🏛️ Dịch Vụ: `FILM-SERVICE` (3 APIs)

| Phương Thức | Đường Dẫn API (Endpoint) | Chức Năng & Vai Trò | Controller |
| :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/films` | Lấy danh sách các bộ phim (phân trang) | `FilmController.java` |
| `POST` | `/api/v1/films` | Thêm bộ phim mới vào kho dữ liệu | `FilmController.java` |
| `GET` | `/api/v1/films/{slug` | Lấy thông tin chi tiết phim theo slug | `FilmController.java` |

---

## 🏛️ Dịch Vụ: `MEDIA-SERVICE` (4 APIs)

| Phương Thức | Đường Dẫn API (Endpoint) | Chức Năng & Vai Trò | Controller |
| :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/media` | Lấy danh sách tất cả các tệp tin media của tenant | `MediaController.java` |
| `POST` | `/api/v1/media/chunk-upload` | Tải lên tệp tin dung lượng lớn theo từng phần (Chunk Upload) | `MediaController.java` |
| `POST` | `/api/v1/media/consumes = MediaType.MULTIPART_FORM_DATA_VALUE` | Tải lên tệp tin dung lượng lớn theo từng phần (Chunk Upload) | `MediaController.java` |
| `POST` | `/api/v1/media/upload` | Tải lên một tệp tin (ảnh / video / tài liệu) | `MediaController.java` |

---

## 🏛️ Dịch Vụ: `MUSIC-SERVICE` (3 APIs)

| Phương Thức | Đường Dẫn API (Endpoint) | Chức Năng & Vai Trò | Controller |
| :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/music/songs` | Lấy danh sách các bài hát (phân trang) | `MusicController.java` |
| `POST` | `/api/v1/music/songs` | Thêm bài hát mới vào thư viện | `MusicController.java` |
| `GET` | `/api/v1/music/songs/{slug` | Lấy chi tiết bài hát theo slug kèm audio streaming URL | `MusicController.java` |

---

## 🏛️ Dịch Vụ: `NOTIFICATION-SERVICE` (5 APIs)

| Phương Thức | Đường Dẫn API (Endpoint) | Chức Năng & Vai Trò | Controller |
| :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/notifications/history` | Lấy lịch sử thông báo đã gửi của tenant | `NotificationController.java` |
| `GET` | `/api/v1/notifications/my-inbox` | Lấy danh sách thông báo hộp thư cá nhân | `NotificationController.java` |
| `PATCH` | `/api/v1/notifications/read-all` | Đánh dấu tất cả thông báo trong hộp thư là đã đọc | `NotificationController.java` |
| `POST` | `/api/v1/notifications/send` | Gửi thông báo tới người nhận qua kênh chỉ định (Hỗ trợ Template & Idempotency Key) | `NotificationController.java` |
| `PATCH` | `/api/v1/notifications/{id}/read` | Đánh dấu thông báo là đã đọc | `NotificationController.java` |

---

## 🏛️ Dịch Vụ: `OTP-SERVICE` (6 APIs)

| Phương Thức | Đường Dẫn API (Endpoint) | Chức Năng & Vai Trò | Controller |
| :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/otp/config` | Xem trạng thái cấu hình và cờ Bật/Tắt OTP Service | `OtpConfigController.java` |
| `PUT` | `/api/v1/otp/config` | Cập nhật cờ Bật/Tắt OTP Service (isEnabled, bypassInDev, devBypassCode) | `OtpConfigController.java` |
| `POST` | `/api/v1/otp/generate` | Sinh mã xác thực OTP 6 số (Tự động hỗ trợ cờ bypass) | `OtpController.java` |
| `POST` | `/api/v1/otp/smart-otp/setup` | Khởi tạo Base32 Secret & Barcode URI chuẩn TOTP RFC 6238 | `OtpController.java` |
| `POST` | `/api/v1/otp/smart-otp/verify` | Xác thực và kích hoạt SmartOTP với mã TOTP 6 số & mã PIN | `OtpController.java` |
| `POST` | `/api/v1/otp/verify` | Xác thực mã OTP 6 số | `OtpController.java` |

---

## 🏛️ Dịch Vụ: `PAYMENT-SERVICE` (9 APIs)

| Phương Thức | Đường Dẫn API (Endpoint) | Chức Năng & Vai Trò | Controller |
| :---: | :--- | :--- | :--- |
| `POST` | `/api/v1/payments/bookings` | Tạo một đơn hàng / đặt chỗ mới (chống trùng lặp Idempotency) | `BookingController.java` |
| `GET` | `/api/v1/payments/bookings` | Lấy danh sách các đơn đặt chỗ của người dùng | `BookingController.java` |
| `POST` | `/api/v1/payments/compensate` | Kích hoạt giao dịch bù trừ Saga (Saga Compensating Transaction) | `PaymentController.java` |
| `POST` | `/api/v1/payments/create-url` | Khởi tạo URL thanh toán chuyển hướng tới cổng (VNPay, MoMo, Stripe) với Idempotency Key | `PaymentController.java` |
| `POST` | `/api/v1/payments/ipn/momo` | Tiếp nhận IPN từ MoMo | `IpnWebhookController.java` |
| `POST` | `/api/v1/payments/ipn/stripe` | Tiếp nhận Webhook từ Stripe | `IpnWebhookController.java` |
| `GET` | `/api/v1/payments/ipn/vnpay` | Tiếp nhận IPN callback từ cổng VNPay | `IpnWebhookController.java` |
| `GET` | `/api/v1/payments/orders/{orderId` | Xem trạng thái đơn hàng thanh toán theo Order ID | `PaymentController.java` |
| `POST` | `/api/v1/payments/orders/{orderId}/reconcile` | Chủ động đối soát trạng thái đơn hàng (Active Status Reconciliation) | `PaymentController.java` |

---

## 🏛️ Dịch Vụ: `REALTIME-SERVICE` (3 APIs)

| Phương Thức | Đường Dẫn API (Endpoint) | Chức Năng & Vai Trò | Controller |
| :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/realtime/presence/online-count` | Xem tổng số lượng kết nối WebSocket đang hoạt động | `RealtimeMessageController.java` |
| `POST` | `/api/v1/realtime/push` | Phát tán thông điệp quảng bá (Broadcast) qua kênh Topic | `RealtimeMessageController.java` |
| `POST` | `/api/v1/realtime/push-user` | Đẩy thông điệp riêng tư (P2P Private Push) tới người dùng đích danh | `RealtimeMessageController.java` |

---

## 🏛️ Dịch Vụ: `TOUR-SERVICE` (3 APIs)

| Phương Thức | Đường Dẫn API (Endpoint) | Chức Năng & Vai Trò | Controller |
| :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/tours` | Lấy danh sách các tour du lịch (phân trang) | `TourController.java` |
| `POST` | `/api/v1/tours` | Tạo mới tour du lịch | `TourController.java` |
| `GET` | `/api/v1/tours/{slug` | Lấy chi tiết tour du lịch theo slug | `TourController.java` |

---

## 🏛️ Dịch Vụ: `WORKER-SERVICE` (16 APIs)

| Phương Thức | Đường Dẫn API (Endpoint) | Chức Năng & Vai Trò | Controller |
| :---: | :--- | :--- | :--- |
| `POST` | `/api/v1/worker/alerts/send-test` | Kiểm thử phát tin nhắn cảnh báo bảo mật & sự cố tức thì qua Telegram/Slack | `WorkerController.java` |
| `GET` | `/api/v1/worker/audit/errors` | Tra cứu danh sách lỗi hệ thống và Request thất bại gần nhất | `WorkerController.java` |
| `GET` | `/api/v1/worker/audit/summary` | Báo cáo tổng quan số liệu log và kiểm toán toàn hệ thống | `WorkerController.java` |
| `GET` | `/api/v1/worker/audit/trace/{traceId` | Tra soát toàn trình phân tán (End-to-End Trace) từ Gateway, Core, Python tới Worker | `WorkerController.java` |
| `POST` | `/api/v1/worker/cleanup/trigger` | Kích hoạt dọn dẹp dữ liệu rác (Housekeeping) ngay lập tức | `WorkerController.java` |
| `GET` | `/api/v1/worker/dlq` | Xem danh sách thông điệp lỗi trong Dead Letter Queue | `WorkerController.java` |
| `POST` | `/api/v1/worker/dlq/{id}/retry` | Thử lại một thông điệp trong hàng đợi Dead Letter Queue | `WorkerController.java` |
| `GET` | `/api/v1/worker/health` | Kiểm tra trạng thái sẵn sàng của Worker Service | `WorkerController.java` |
| `POST` | `/api/v1/worker/mail/dispatch` | Cổng nhận lệnh phát thư tín / email / SMS từ các Microservices khác | `WorkerController.java` |
| `GET` | `/api/v1/worker/mail/logs` | Tra cứu nhật ký gửi email/SMS có phân trang và lọc theo trạng thái | `WorkerController.java` |
| `POST` | `/api/v1/worker/mail/trigger` | Kích hoạt xử lý hàng đợi phát thư tín ngay lập tức | `WorkerController.java` |
| `POST` | `/api/v1/worker/outbox/trigger` | Kích hoạt quét hàng đợi Outbox Events ngay lập tức | `WorkerController.java` |
| `POST` | `/api/v1/worker/partitions/maintain` | Kích hoạt bảo trì và phân vùng tự động (Automated DB Partition Maintenance) | `WorkerController.java` |
| `GET` | `/api/v1/worker/partitions/status` | Xem trạng thái phân mảnh bảng (Table Partitioning) của cơ sở dữ liệu | `WorkerController.java` |
| `POST` | `/api/v1/worker/reconciliation/trigger` | Kích hoạt đối soát Sổ cái kép (EOD Audit) ngay lập tức | `WorkerController.java` |
| `GET` | `/api/v1/worker/status` | Xem tổng quan số liệu hàng đợi Mail, Outbox, DLQ và Scheduler | `WorkerController.java` |

---

## 🐍 DỊCH VỤ: `PYTHON FINTECH & AI CORE (:8089)` (58 APIs)

### 📦 Phân Hệ Python: `AI` (3 APIs)

| Phương Thức | Đường Dẫn API (Endpoint) | Chức Năng & Vai Trò | File Handler |
| :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/ai/spending-score` | Số ngày dự báo dòng tiền tương lai | `ai.py` |
| `GET` | `/api/v1/ai/budget-50-30-20` | Số ngày kể từ lần nạp tiền gần nhất | `ai.py` |
| `GET` | `/api/v1/ai/habit/kde` | get_gaussian_kde_model | `ai.py` |


### 📦 Phân Hệ Python: `AUTH` (4 APIs)

| Phương Thức | Đường Dẫn API (Endpoint) | Chức Năng & Vai Trò | File Handler |
| :---: | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | register_account | `auth.py` |
| `POST` | `/api/v1/auth/login` | login_account | `auth.py` |
| `POST` | `/api/v1/auth/refresh-token` | refresh_access_token | `auth.py` |
| `POST` | `/api/v1/auth/logout` | logout_account | `auth.py` |


### 📦 Phân Hệ Python: `BUDGET` (2 APIs)

| Phương Thức | Đường Dẫn API (Endpoint) | Chức Năng & Vai Trò | File Handler |
| :---: | :--- | :--- | :--- |
| `PUT` | `/api/v1/budget/{budget_id}` | update_budget | `budget.py` |
| `DELETE` | `/api/v1/budget/{budget_id}` | delete_budget | `budget.py` |


### 📦 Phân Hệ Python: `GOALS` (3 APIs)

| Phương Thức | Đường Dẫn API (Endpoint) | Chức Năng & Vai Trò | File Handler |
| :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/goals/{goal_id}` | get_financial_goal_detail | `goals.py` |
| `PUT` | `/api/v1/goals/{goal_id}` | update_financial_goal | `goals.py` |
| `POST` | `/api/v1/goals/{goal_id}/lock` | lock_unlock_goal_piggy | `goals.py` |


### 📦 Phân Hệ Python: `LEDGER` (6 APIs)

| Phương Thức | Đường Dẫn API (Endpoint) | Chức Năng & Vai Trò | File Handler |
| :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/ledger/trial-balance` | get_trial_balance_audit | `ledger.py` |
| `GET` | `/api/v1/ledger/statement/{wallet_id}` | get_wallet_ledger_statement | `ledger.py` |
| `POST` | `/api/v1/ledger/double-entry` | execute_double_entry_booking | `ledger.py` |
| `GET` | `/api/v1/ledger/export/trial-balance-csv` | export_trial_balance_csv_report | `ledger.py` |
| `GET` | `/api/v1/ledger/export/statement-csv/{wallet_id}` | export_wallet_statement_csv_report | `ledger.py` |
| `GET` | `/api/v1/ledger/entries` | list_ledger_entries | `ledger.py` |


### 📦 Phân Hệ Python: `NOTIFICATIONS` (2 APIs)

| Phương Thức | Đường Dẫn API (Endpoint) | Chức Năng & Vai Trò | File Handler |
| :---: | :--- | :--- | :--- |
| `PUT` | `/api/v1/notifications/{notification_id}/read` | mark_notification_as_read | `notifications.py` |
| `PUT` | `/api/v1/notifications/read-all` | mark_all_notifications_as_read | `notifications.py` |


### 📦 Phân Hệ Python: `OCR` (2 APIs)

| Phương Thức | Đường Dẫn API (Endpoint) | Chức Năng & Vai Trò | File Handler |
| :---: | :--- | :--- | :--- |
| `POST` | `/api/v1/ocr/scan` | scan_receipt | `ocr.py` |
| `POST` | `/api/v1/ocr/create-transaction` | create_transaction_from_ocr | `ocr.py` |


### 📦 Phân Hệ Python: `PARENT_MATCHING` (3 APIs)

| Phương Thức | Đường Dẫn API (Endpoint) | Chức Năng & Vai Trò | File Handler |
| :---: | :--- | :--- | :--- |
| `POST` | `/api/v1/parent_matching/matching-rules` | create_or_update_parent_matching_rule | `parent_matching.py` |
| `GET` | `/api/v1/parent_matching/matching-rules` | get_parent_matching_rule | `parent_matching.py` |
| `GET` | `/api/v1/parent_matching/family-dashboard` | get_family_savings_dashboard | `parent_matching.py` |


### 📦 Phân Hệ Python: `PAYMENT` (3 APIs)

| Phương Thức | Đường Dẫn API (Endpoint) | Chức Năng & Vai Trò | File Handler |
| :---: | :--- | :--- | :--- |
| `POST` | `/api/v1/payment/create-vietqr` | create_vietqr_deposit | `payment.py` |
| `POST` | `/api/v1/payment/webhook` | process_payment_webhook | `payment.py` |
| `GET` | `/api/v1/payment/history` | get_payment_history | `payment.py` |


### 📦 Phân Hệ Python: `REPORT` (1 APIs)

| Phương Thức | Đường Dẫn API (Endpoint) | Chức Năng & Vai Trò | File Handler |
| :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/report/revenue` | get_revenue_report | `report.py` |


### 📦 Phân Hệ Python: `REPORTS` (4 APIs)

| Phương Thức | Đường Dẫn API (Endpoint) | Chức Năng & Vai Trò | File Handler |
| :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/reports/cash-flow` | get_cash_flow_report | `reports.py` |
| `GET` | `/api/v1/reports/category-breakdown` | get_category_breakdown | `reports.py` |
| `GET` | `/api/v1/reports/summary` | get_financial_summary | `reports.py` |
| `GET` | `/api/v1/reports/export-csv` | export_transactions_csv | `reports.py` |


### 📦 Phân Hệ Python: `SMART_PIGGY` (6 APIs)

| Phương Thức | Đường Dẫn API (Endpoint) | Chức Năng & Vai Trò | File Handler |
| :---: | :--- | :--- | :--- |
| `POST` | `/api/v1/smart_piggy/pair` | Mệnh giá tiền thả vào heo | `smart_piggy.py` |
| `POST` | `/api/v1/smart_piggy/coin-drop` | handle_coin_drop_frontend | `smart_piggy.py` |
| `POST` | `/api/v1/smart_piggy/withdrawal/request` | request_withdrawal_saga_frontend | `smart_piggy.py` |
| `POST` | `/api/v1/smart_piggy/withdrawal/settle` | settle_withdrawal_saga_frontend | `smart_piggy.py` |
| `POST` | `/api/v1/smart_piggy/withdrawal/timeout` | timeout_withdrawal_saga_frontend | `smart_piggy.py` |
| `POST` | `/api/v1/smart_piggy/unfreeze` | unfreeze_savings_wallet | `smart_piggy.py` |


### 📦 Phân Hệ Python: `SMART_PIGGY_SIMULATOR` (6 APIs)

| Phương Thức | Đường Dẫn API (Endpoint) | Chức Năng & Vai Trò | File Handler |
| :---: | :--- | :--- | :--- |
| `POST` | `/api/v1/smart_piggy_simulator/drop-coin` | simulate_coin_drop | `smart_piggy_simulator.py` |
| `POST` | `/api/v1/smart_piggy_simulator/tamper-alarm` | simulate_tamper_alarm | `smart_piggy_simulator.py` |
| `POST` | `/api/v1/smart_piggy_simulator/withdraw-flow` | simulate_withdraw_flow | `smart_piggy_simulator.py` |
| `POST` | `/api/v1/smart_piggy_simulator/security-replay-test` | simulate_security_replay | `smart_piggy_simulator.py` |
| `POST` | `/api/v1/smart_piggy_simulator/sensor-deception-test` | simulate_sensor_deception | `smart_piggy_simulator.py` |
| `POST` | `/api/v1/smart_piggy_simulator/heartbeat-watchdog-test` | simulate_heartbeat_watchdog | `smart_piggy_simulator.py` |


### 📦 Phân Hệ Python: `TRANSACTION` (2 APIs)

| Phương Thức | Đường Dẫn API (Endpoint) | Chức Năng & Vai Trò | File Handler |
| :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/transaction/{transaction_id}` | get_transaction_detail | `transaction.py` |
| `DELETE` | `/api/v1/transaction/{transaction_id}` | delete_transaction | `transaction.py` |


### 📦 Phân Hệ Python: `TRANSFER` (1 APIs)

| Phương Thức | Đường Dẫn API (Endpoint) | Chức Năng & Vai Trò | File Handler |
| :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/transfer/{transfer_id}` | get_transfer_detail | `transfer.py` |


### 📦 Phân Hệ Python: `USER_PROFILE` (5 APIs)

| Phương Thức | Đường Dẫn API (Endpoint) | Chức Năng & Vai Trò | File Handler |
| :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/user_profile/me` | get_my_profile | `user_profile.py` |
| `PUT` | `/api/v1/user_profile/me` | update_my_profile | `user_profile.py` |
| `POST` | `/api/v1/user_profile/change-password` | change_password | `user_profile.py` |
| `POST` | `/api/v1/user_profile/forgot-password` | forgot_password | `user_profile.py` |
| `POST` | `/api/v1/user_profile/reset-password` | reset_password | `user_profile.py` |


### 📦 Phân Hệ Python: `VERIFICATION` (2 APIs)

| Phương Thức | Đường Dẫn API (Endpoint) | Chức Năng & Vai Trò | File Handler |
| :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/verification/activate` | activate_link_endpoint | `verification.py` |
| `POST` | `/api/v1/verification/verify-otp` | verify_otp_endpoint | `verification.py` |


### 📦 Phân Hệ Python: `WALLET` (3 APIs)

| Phương Thức | Đường Dẫn API (Endpoint) | Chức Năng & Vai Trò | File Handler |
| :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/wallet/user/{user_id}` | list_wallets_endpoint | `wallet.py` |
| `POST` | `/api/v1/wallet/transfer` | transfer_wallets_endpoint | `wallet.py` |
| `GET` | `/api/v1/wallet/{wallet_id}` | get_wallet_detail_endpoint | `wallet.py` |


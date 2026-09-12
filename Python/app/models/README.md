# 🧱 Package: `app.models`

Package chứa các Model SQLAlchemy 2.0 định nghĩa cấu trúc dữ liệu cho cơ sở dữ liệu `liochio_fintech_db`.

## 📌 Nguyên tắc Kiến trúc:
- **Logical Foreign Key**: Tất cả các bảng nghiệp vụ liên kết với người dùng bằng cột `user_id = Column(String(64), nullable=False, index=True)`. Tuyệt đối không tạo Foreign Key vật lý sang cơ sở dữ liệu `liochio_auth_db` của Java.
- **BaseEntity**: Tự động quản lý `id` (UUIDv4), `created_at`, `updated_at`.

## 📋 Danh mục Models chính:

| Thư mục Model | Các Bảng CSDL | Trách nhiệm dữ liệu |
| :--- | :--- | :--- |
| `wallet/` | `wallets` | Lưu trữ số dư, loại ví (CASH, BANK, SAVINGS, SMART_PIGGY), trạng thái ví. |
| `finance/` | `transactions`, `transfers`, `budgets`, `financial_goals`, `categories` | Sổ cái giao dịch tài chính, chuyển tiền, danh mục phân loại, mục tiêu tích lũy. |
| `smart_piggy/` | `smart_piggy_devices`, `smart_piggy_gamification` | Cấu hình thiết bị IoT heo đất, huy hiệu gamification, mốc mở khóa. |
| `ledger/` | `journal_entries`, `ledger_accounts`, `accounting_periods` | Sổ cái kép Double-Entry kế toán (Nợ/Có) phục vụ kiểm toán tài chính bất biến. |
| `notification/`| `notifications`, `push_devices`, `notification_logs` | Thông báo đa kênh (FCM Push, Email SMTP, In-app). |
| `ai/` & `ocr/`  | `ai_financial_scores`, `ocr_results` | Điểm tín nhiệm tài chính AI và kết quả quét bóc tách hóa đơn. |

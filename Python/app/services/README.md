# 💼 Package: `app.services`

Package chứa toàn bộ Logic Nghiệp vụ (Business Application Services) của nền tảng FinTech.

## 📋 Danh sách các Service cốt lõi:

| Thư mục Service | Vai trò & Xử lý nghiệp vụ |
| :--- | :--- |
| `wallet/` | `WalletService`, `WalletTopupService`: Tính toán biến động số dư an toàn, khóa/mở khóa ví, nạp rút tiền. |
| `finance/` | `TransactionService`, `BudgetService`, `GoalService`: Tạo giao dịch, kiểm tra hạn mức ngân sách, cập nhật tiến độ mục tiêu. |
| `transfer/` | `TransferService`: Xử lý giao dịch chuyển khoản 2 chiều với cơ chế kiểm tra tính toàn vẹn và Rollback tự động. |
| `ledger/` | `GeneralLedgerService`: Ghi nhận các bút toán kế toán Nợ - Có (Debit - Credit) tự động cân bằng. |
| `smart_piggy/` | `SmartPiggyService`: Điều khiển phần cứng IoT qua MQTT, kiểm tra điều kiện mở khóa heo kèm Smart OTP Action Token. |
| `payment/` | `PaymentService`, `VietQRService`: Sinh mã VietQR chuẩn NAPAS 247 và xử lý Webhook ngân hàng. |
| `notification/` | `NotificationService`: Điều phối gửi email thông báo qua background task. |

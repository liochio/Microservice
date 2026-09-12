# 🗄️ Package: `app.repositories`

Package chứa các Repositories trừu tượng hóa thao tác truy vấn dữ liệu từ MySQL qua SQLAlchemy Session.

## 📋 Danh sách Repositories chính:

| File Repository | Entity quản lý | Chức năng truy vấn |
| :--- | :--- | :--- |
| `wallet/wallet_repository.py` | `Wallet` | Truy vấn ví theo `user_id`, kiểm tra số dư, cập nhật số dư với cơ chế khóa hàng (row-level locking). |
| `finance/transaction_repository.py` | `Transaction` | Thêm mới giao dịch, phân trang lịch sử giao dịch theo khoảng thời gian và danh mục. |
| `finance/financial_goal_repository.py` | `FinancialGoal` | Quản lý tiến độ mục tiêu tiết kiệm, tính toán tỷ lệ hoàn thành. |
| `finance/category_repository.py` | `Category` | Nạp danh mục hệ thống và danh mục riêng của từng `user_id`. |
| `ledger/ledger_repository.py` | `JournalEntry` | Ghi nhận bút toán kép kế toán, đảm bảo tính toàn vẹn tài chính. |
| `smart_piggy/smart_piggy_repository.py` | `SmartPiggyDevice` | Truy vấn thông tin thiết bị heo đất IoT theo `user_id` hoặc `device_uid`. |

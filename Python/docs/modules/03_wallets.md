# 👛 Phân Hệ 03: Quản Lý Ví Tài Chính (Wallet Management)

## 1. Tổng Quan & Mục Tiêu
Phân hệ **Wallet Management** quản lý số dư, tiền tệ và các loại tài khoản tiền tệ của người dùng.

### Trạng thái triển khai:
- ✅ **Đã hoàn thiện**: Tạo ví mới, Truy vấn danh sách & chi tiết số dư an toàn, Cập nhật cấu hình ví, Xóa mềm ví (Soft Delete), Kiểm tra quyền sở hữu ví (chống IDOR).
- ⏳ **Kế hoạch tương lai (Roadmap)**: Ví đa tiền tệ tự động quy đổi tỷ giá thời gian thực (FX Exchange), Ví mục tiêu tự động trích tiền theo lịch, Hạn mức giao dịch ngày/tháng per-wallet.

---

## 2. Các Loại Ví Hỗ Trợ ('wallet_type')
| Loại Ví | Mã Enum | Mục đích sử dụng |
| :--- | :--- | :--- |
| **Tiền mặt** | 'CASH' | Quản lý tiền mặt chi tiêu hàng ngày |
| **Ngân hàng** | 'BANK' | Liên kết và theo dõi tài khoản ngân hàng |
| **Ví điện tử** | 'EWALLET' | Quản lý số dư MoMo, ZaloPay, VNPay |
| **Tiết kiệm** | 'SAVINGS' | Tài khoản tiết kiệm tích lũy sinh lời |
| **Heo đất thông minh** | 'SMART_PIGGY' | Ví liên kết thiết bị Heo đất IoT nhận tiền xu |

---

## 3. Danh Sách API Endpoints

| Method | Endpoint | Quyền hạn yêu cầu | Mô tả |
| :--- | :--- | :--- | :--- |
| 'POST' | '/api/v1/wallets' | 'WALLET_CREATE' | Khởi tạo ví tài khoản mới |
| 'GET' | '/api/v1/wallets' | 'WALLET_LIST' | Lấy danh sách toàn bộ ví của User |
| 'GET' | '/api/v1/wallets/{wallet_id}' | 'WALLET_DETAIL' | Lấy thông tin số dư chi tiết ví |
| 'PUT' | '/api/v1/wallets/{wallet_id}' | 'WALLET_UPDATE' | Cập nhật cấu hình (tên, màu, icon, mô tả) |
| 'DELETE' | '/api/v1/wallets/{wallet_id}' | 'WALLET_DELETE' | Xóa mềm ví ('is_deleted = 1') |

---

## 4. Cấu Trúc Bảng Dữ Liệu
- 'wallets': Bảng ví chính ('id', 'user_id', 'wallet_code', 'name', 'balance', 'currency', 'wallet_type', 'status', 'is_deleted').
- 'wallet_history': Lịch sử biến động số dư ví (Balance History Snapshot).
- 'wallet_limits': Hạn mức chi tiêu và nạp tiền theo ngày/tháng.
# 🛣️ Package: `app.api`

Package điều phối và định tuyến toàn bộ các REST API Endpoints và WebSocket của **Resource Server (`liochio-fintech`)**.

## 📋 Danh mục Phân hệ API:

| Phân hệ | Đường dẫn Router | Chức năng nghiệp vụ chi tiết |
| :--- | :--- | :--- |
| **Wallets** | `app/api/v1/wallets/` | Quản lý ví tiền đa nguồn (Tiền mặt, Ngân hàng, Ví điện tử, Heo đất), nạp/rút tiền, khóa ví. |
| **Transactions** | `app/api/v1/transactions/` | Ghi nhận giao dịch thu chi, gắn danh mục, chống trùng lặp bằng Idempotency-Key. |
| **Transfers** | `app/api/v1/transfers/` | Chuyển tiền P2P nội bộ giữa các ví, bảo đảm giao dịch 2 pha (2-phase lock). |
| **Budgets & Goals** | `app/api/v1/budgets/`, `app/api/v1/goals/` | Thiết lập ngân sách chi tiêu, cảnh báo vượt ngưỡng, theo dõi mục tiêu tích lũy. |
| **Smart Piggy IoT** | `app/api/v1/smart_piggy/` | Kết nối thiết bị phần cứng heo đất IoT qua MQTT, mở khóa heo với `X-Action-Token`. |
| **Parent Matching** | `app/api/v1/parent/` | Thiết lập luật nhân đôi tiền tiết kiệm của con, phê duyệt và cấp tiền thưởng. |
| **VietQR & Webhook**| `app/api/v1/payment/` | Tạo mã VietQR NAPAS 247 động, tiếp nhận Webhook ngân hàng với cơ chế ACID. |
| **Reports** | `app/api/v1/reports/` | Báo cáo dòng tiền, phân tích thu chi theo kỳ, xuất file CSV/Excel. |
| **WebSocket** | `app/api/v1/ws/` | Kênh WebSocket đẩy thông báo Realtime (Hiệu ứng pháo hoa chúc mừng khi đạt mục tiêu tiết kiệm). |

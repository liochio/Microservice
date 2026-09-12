# Background Jobs Package

## Chức năng
Gói pp/jobs chứa các tác vụ chạy ngầm định kỳ (Cron / Scheduled Tasks) phục vụ quản lý tài chính tự động:
1. **Lãi suất tiết kiệm định kỳ**: Tính toán lãi suất tích lũy hàng ngày/tháng cho các tài khoản heo đất hoặc ví tiết kiệm.
2. **Quét cảnh báo ngân sách**: Kiểm tra các danh mục chi tiêu vượt ngưỡng 80%, 100% ngân sách để gửi thông báo.
3. **Dọn dẹp phiên & Cache**: Quét và dọn dẹp idempotency keys, action tokens hết hạn trong Redis.
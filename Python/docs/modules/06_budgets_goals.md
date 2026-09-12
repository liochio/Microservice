# 📊 Phân Hệ 06: Quản Lý Ngân Sách & Mục Tiêu Tài Chính (Budgets & Goals)

## 1. Tổng Quan & Mục Tiêu
Phân hệ **Budgets & Goals** hỗ trợ người dùng lập kế hoạch tài chính cá nhân, đặt hạn mức chi tiêu theo danh mục (Ăn uống, Mua sắm, Di chuyển) và theo dõi tiến độ hoàn thành mục tiêu tiết kiệm (Mua nhà, Du lịch, Quỹ khẩn cấp).

### Trạng thái triển khai:
- ⏳ **Đang hoàn thiện**: Mô hình bảng `budgets`, `categories`, `financial_goals`.
- ⏳ **Kế hoạch tương lai (Roadmap)**: Cảnh báo tự động qua Email/Push khi chi tiêu đạt 80% và 100% ngân sách, Tự động trích tiền lẻ từ Heo đất vào mục tiêu tài chính, Biểu đồ tiến độ trực quan.

---

## 2. Các Tính Năng Cốt Lõi
1. **Quản lý Ngân sách (`budgets`)**:
   - Thiết lập hạn mức chi tiêu theo tháng hoặc theo tuần.
   - Gắn ngân sách với từng Danh mục chi tiêu (`category_id`) hoặc toàn bộ Ví.
   - Tự động cộng dồn chi tiêu thực tế từ các giao dịch `EXPENSE`.
2. **Mục tiêu Tài chính (`financial_goals`)**:
   - Thiết lập số tiền mục tiêu (`target_amount`) và ngày dự kiến hoàn thành (`target_date`).
   - Theo dõi số tiền đã tích lũy (`current_amount`) và tỷ lệ hoàn thành (%).

---

## 3. Cấu Trúc Bảng Dữ Liệu
- `categories`: Danh mục thu chi (Ăn uống, Giải trí, Lương thưởng,...).
- `budgets`: Hạn mức chi tiêu theo kỳ.
- `financial_goals`: Kế hoạch và mục tiêu tài chính cá nhân.
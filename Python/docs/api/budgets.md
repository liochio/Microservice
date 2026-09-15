# 📊 API HƯỚNG DẪN: PHÂN HỆ QUẢN LÝ NGÂN SÁCH (BUDGETS)

## 📌 Mục đích & Nghiệp vụ
Thiết lập hạn mức chi tiêu cho từng danh mục theo tháng hoặc tuần. Hệ thống tự động tính toán tổng số tiền đã chi tiêu thực tế từ các giao dịch và tính '%' tiến độ vượt ngân sách.

---

## 1. Lấy Danh Sách Ngân Sách
- **Endpoint**: 'GET /api/v1/budgets'
- **Response**: Trả về danh sách ngân sách kèm 'amount_limit', 'current_spent', 'spent_percentage' và cờ cảnh báo 'is_exceeded'.

---

## 2. Tạo Ngân Sách Chi Tiêu Mới
- **Endpoint**: 'POST /api/v1/budgets'
- **Payload**:
'''json
{
  "category_id": "cat-food-uuid",
  "amount_limit": 3000000.0,
  "start_date": "2026-09-01T00:00:00",
  "end_date": "2026-09-30T23:59:59"
}
'''

---

## 3. Điều Chỉnh Hạn Mức Ngân Sách
- **Endpoint**: 'PUT /api/v1/budgets/{budget_id}'
- **Payload**: '{"amount_limit": 4000000.0}'

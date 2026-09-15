# 💸 API HƯỚNG DẪN: PHÂN HỆ GIAO DỊCH THU / CHI (TRANSACTIONS)

## 📌 Mục đích & Nghiệp vụ
Quản lý toàn bộ lịch sử biến động số dư ví. Khi tạo giao dịch:
- Giao dịch **EXPENSE (Chi tiêu)**: Kiểm tra số dư ví $
ightarrow$ Trừ tiền ví $
ightarrow$ Tạo bản ghi giao dịch.
- Giao dịch **INCOME (Thu nhập)**: Cộng tiền ví $
ightarrow$ Tạo bản ghi giao dịch.
- Khi xóa giao dịch: Tự động hoàn lại số dư ví ban đầu.

---

## 1. Lấy Lịch Sử Giao Dịch
- **Endpoint**: 'GET /api/v1/transactions'
- **Query Params**:
  - 'wallet_id': Lọc theo ví
  - 'category_id': Lọc theo danh mục
  - 'type': 'EXPENSE' hoặc 'INCOME'
  - 'start_date', 'end_date': Khoảng ngày 'YYYY-MM-DD'
  - 'limit', 'offset': Phân trang

---

## 2. Tạo Giao Dịch Thu / Chi
- **Endpoint**: 'POST /api/v1/transactions'
- **Payload**:
'''json
{
  "wallet_id": "7e503f4a-4f12-4a86-af0d-ab069152dfdf",
  "category_id": "cat-uuid-001",
  "amount": 150000.0,
  "transaction_type": "EXPENSE",
  "description": "Ăn trưa cơm văn phòng"
}
'''

---

## 3. Xóa / Hoàn Tác Giao Dịch
- **Endpoint**: 'DELETE /api/v1/transactions/{transaction_id}'
- **Mục đích**: Hủy giao dịch nhầm và hoàn lại tiền vào ví tài khoản.

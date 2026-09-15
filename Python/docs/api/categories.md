# 🏷️ API HƯỚNG DẪN: PHÂN HỆ DANH MỤC THU / CHI (CATEGORIES)

## 📌 Mục đích & Nghiệp vụ
Quản lý cây danh mục thu chi (Ăn uống, Mua sắm, Tiền lương, v.v.). Phân hệ này là nền tảng để phân loại các giao dịch chi tiêu và thiết lập ngân sách.

---

## 1. Lấy Danh Sách Danh Mục
- **Endpoint**: 'GET /api/v1/categories'
- **Mục đích**: Lấy toàn bộ danh mục hệ thống mặc định và danh mục do chính người dùng tự tạo.
- **Khi nào dùng**: Khi hiển thị dropdown chọn danh mục lúc tạo giao dịch hoặc tạo ngân sách.
- **Quyền yêu cầu**: Đã đăng nhập ('USER').

### Response Mẫu (200 OK):
'''json
{
  "success": true,
  "error_code": "CATEGORY_FETCH_SUCCESS",
  "message": "Lấy danh mục thành công.",
  "data": [
    {
      "id": "cat-uuid-001",
      "user_id": null,
      "name": "Ăn uống",
      "type": "EXPENSE",
      "icon": "utensils",
      "color": "#FF5722",
      "status": "ACTIVE"
    },
    {
      "id": "cat-uuid-002",
      "user_id": null,
      "name": "Tiền lương",
      "type": "INCOME",
      "icon": "dollar-sign",
      "color": "#4CAF50",
      "status": "ACTIVE"
    }
  ]
}
'''

---

## 2. Tạo Danh Mục Mới
- **Endpoint**: 'POST /api/v1/categories'
- **Mục đích**: Cho phép người dùng tự định nghĩa danh mục chi tiêu cá nhân.
- **Payload**:
'''json
{
  "name": "Nuôi thú cưng",
  "type": "EXPENSE",
  "icon": "paw",
  "color": "#8E24AA"
}
'''

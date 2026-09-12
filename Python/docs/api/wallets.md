# 💼 API HƯỚNG DẪN: PHÂN HỆ VÍ TÀI CHÍNH (WALLETS)

## 📌 1. Tổng Quan & Mục Đích
Phân hệ **Wallet Management** quản lý toàn bộ các ví tài chính cá nhân của người dùng:
- **Ví Tiền Mặt (`CASH`)**: Ví mặc định được tạo tự động khi kích hoạt tài khoản.
- **Ví Tiết Kiệm (`SAVINGS`)**: Ví gom góp tiết kiệm theo mục tiêu tài chính.
- **Ví Ngân Hàng (`BANK`)**: Ví liên kết với tài khoản ngân hàng mô phỏng.
- **Ví Heo Đất IoT (`SMART_PIGGY`)**: Ví liên kết với phần cứng heo đất thông minh.

### 🛡️ Nguyên Tắc An Ninh & Bọc Thép:
- **Chống IDOR**: Mọi thao tác truy vấn, cập nhật, xóa ví đều kiểm tra chặt chẽ `WHERE user_id = :current_user_id AND is_deleted = 0`.
- **Phân quyền Guard**: Yêu cầu quyền `WALLET_LIST`, `WALLET_CREATE`, `WALLET_DETAIL`, `WALLET_UPDATE`, `WALLET_DELETE`.

---

## 🚀 2. Chi Tiết Các Endpoint

### 2.1 Lấy Danh Sách Ví Của User
- **Endpoint**: `GET /api/v1/wallets`
- **Mục đích**: Lấy danh sách tất cả các ví đang hoạt động của người dùng hiện tại.
- **Header**: `Authorization: Bearer <TOKEN>`
- **Response Mẫu (200 OK)**:
```json
{
  "success": true,
  "error_code": "WALLET_FETCH_SUCCESS",
  "message": "Lấy thông tin chi tiết ví thành công.",
  "data": [
    {
      "id": "7e503f4a-4f12-4a86-af0d-ab069152dfdf",
      "user_id": "bfe20456-36ac-4239-b8cc-7da285e86b70",
      "name": "Ví Tiền Mặt Chính",
      "wallet_code": "CASH_BFE20456",
      "wallet_type": "CASH",
      "wallet_account": "5781479760",
      "balance": "1500000.0000",
      "currency": "VND",
      "color": "#388E3C",
      "icon": "cash",
      "status": "ACTIVE"
    }
  ]
}
```

---

### 2.2 Xem Chi Tiết 1 Ví
- **Endpoint**: `GET /api/v1/wallets/{wallet_id}`
- **Mục đích**: Xem chi tiết số dư và thông số cấu hình của 1 ví cụ thể.

---

### 2.3 Khởi Tạo Ví Mới
- **Endpoint**: `POST /api/v1/wallets`
- **Mục đích**: Người dùng mở thêm ví tiết kiệm hoặc ví chi tiêu riêng biệt.
- **Payload Request**:
```json
{
  "wallet_code": "SAVINGS_HOUSE_2026",
  "name": "Ví Tiết Kiệm Mua Nhà",
  "wallet_type": "SAVINGS",
  "currency": "VND",
  "description": "Quỹ tích lũy mua chung cư",
  "color": "#1976D2",
  "icon": "home"
}
```
- **Response Mẫu (201 Created)**:
```json
{
  "success": true,
  "error_code": "WALLET_CREATE_SUCCESS",
  "message": "Khởi tạo ví tài khoản mới thành công.",
  "data": {
    "status": "SUCCESS",
    "wallet_code": "SAVINGS_HOUSE_2026"
  }
}
```

---

### 2.4 Cập Nhật Thông Tin Ví
- **Endpoint**: `PUT /api/v1/wallets/{wallet_id}`
- **Payload Request**:
```json
{
  "name": "Ví Tiết Kiệm Mua Xe",
  "color": "#E91E63",
  "icon": "car",
  "description": "Chuyển mục tiêu sang mua xe"
}
```

---

### 2.5 Xóa Mềm Ví Tài Khoản
- **Endpoint**: `DELETE /api/v1/wallets/{wallet_id}`
- **Mục đích**: Xóa mềm ví (`is_deleted = 1`) khi người dùng không còn nhu cầu sử dụng.

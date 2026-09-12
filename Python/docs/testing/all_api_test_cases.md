# 🧪 BỘ TESTCASE TOÀN DIỆN TẤT CẢ API (KÈM LỆNH CURL SẴN SÀNG COPY-PASTE)

## 📌 Hướng Dẫn Sử Dụng
Tất cả các lệnh cURL dưới đây đã được định dạng chuẩn, có thể copy và dán trực tiếp vào Terminal (Command Prompt / PowerShell / Linux Bash) hoặc Import vào Postman.

---

## 1. PHÂN HỆ XÁC THỰC (AUTH)

### 1.1 Đăng Ký Tài Khoản Mới (Happy Path)
```bash
curl -X POST "http://127.0.0.1/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -H "Accept-Language: vi" \
  -d '{
    "username": "tester_2026",
    "email": "tester_2026@gmail.com",
    "phone_number": "0912345678",
    "password": "SecurePassword123!",
    "confirm_password": "SecurePassword123!",
    "full_name": "Nguyễn Văn Test",
    "date_of_birth": "1999-01-01",
    "gender": "MALE"
  }'
```

### 1.2 Đăng Nhập Tài Khoản
```bash
curl -X POST "http://127.0.0.1/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -H "Accept-Language: vi" \
  -d '{
    "email": "admin@fintech.local",
    "password": "Admin@123456"
  }'
```

---

## 2. PHÂN HỆ HỒ SƠ NGƯỜI DÙNG (USERS)

### 2.1 Xem Thông Tin Cá Nhân (`GET /users/me`)
```bash
curl -X GET "http://127.0.0.1/api/v1/users/me" \
  -H "Authorization: Bearer <TOKEN_CỦA_BẠN>" \
  -H "Accept-Language: vi"
```

### 2.2 Cập Nhật Hồ Sơ (`PUT /users/me`)
```bash
curl -X PUT "http://127.0.0.1/api/v1/users/me" \
  -H "Authorization: Bearer <TOKEN_CỦA_BẠN>" \
  -H "Content-Type: application/json" \
  -d '{
    "full_name": "Nguyễn Văn Đã Cập Nhật",
    "gender": "MALE"
  }'
```

### 2.3 Đổi Mật Khẩu (`POST /users/change-password`)
```bash
curl -X POST "http://127.0.0.1/api/v1/users/change-password" \
  -H "Authorization: Bearer <TOKEN_CỦA_BẠN>" \
  -H "Content-Type: application/json" \
  -d '{
    "old_password": "SecurePassword123!",
    "new_password": "NewSecurePass456!",
    "confirm_password": "NewSecurePass456!"
  }'
```

---

## 3. PHÂN HỆ DANH MỤC THU CHI (CATEGORIES)

### 3.1 Lấy Danh Sách Danh Mục
```bash
curl -X GET "http://127.0.0.1/api/v1/categories" \
  -H "Authorization: Bearer <TOKEN_CỦA_BẠN>"
```

### 3.2 Tạo Danh Mục Cá Nhân Mới
```bash
curl -X POST "http://127.0.0.1/api/v1/categories" \
  -H "Authorization: Bearer <TOKEN_CỦA_BẠN>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Nuôi Thú Cưng",
    "type": "EXPENSE",
    "icon": "paw",
    "color": "#9C27B0"
  }'
```

---

## 4. PHÂN HỆ VÍ TÀI CHÍNH (WALLETS)

### 4.1 Lấy Danh Sách Ví
```bash
curl -X GET "http://127.0.0.1/api/v1/wallets" \
  -H "Authorization: Bearer <TOKEN_CỦA_BẠN>"
```

### 4.2 Tạo Ví Mới
```bash
curl -X POST "http://127.0.0.1/api/v1/wallets" \
  -H "Authorization: Bearer <TOKEN_CỦA_BẠN>" \
  -H "Content-Type: application/json" \
  -d '{
    "wallet_code": "SAVINGS_01",
    "name": "Ví Tiết Kiệm Mua Nhà",
    "wallet_type": "SAVINGS",
    "currency": "VND"
  }'
```

---

## 5. PHÂN HỆ GIAO DỊCH THU CHI (TRANSACTIONS)

### 5.1 Tạo Giao Dịch Thu/Chi
```bash
curl -X POST "http://127.0.0.1/api/v1/transactions" \
  -H "Authorization: Bearer <TOKEN_CỦA_BẠN>" \
  -H "Content-Type: application/json" \
  -d '{
    "wallet_id": "<WALLET_ID>",
    "category_id": "<CATEGORY_ID>",
    "amount": 50000.0,
    "transaction_type": "EXPENSE",
    "description": "Ăn sáng bánh mì"
  }'
```

### 5.2 Lấy Danh Sách Giao Dịch
```bash
curl -X GET "http://127.0.0.1/api/v1/transactions?limit=20&offset=0" \
  -H "Authorization: Bearer <TOKEN_CỦA_BẠN>"
```

---

## 6. PHÂN HỆ CHUYỂN TIỀN NỘI BỘ (TRANSFERS)

### 6.1 Chuyển Tiền Giữa 2 Ví
```bash
curl -X POST "http://127.0.0.1/api/v1/transfers" \
  -H "Authorization: Bearer <TOKEN_CỦA_BẠN>" \
  -H "Content-Type: application/json" \
  -d '{
    "source_wallet_id": "<SOURCE_WALLET_ID>",
    "destination_wallet_id": "<DEST_WALLET_ID>",
    "amount": 200000.0,
    "description": "Chuyển tiền vào quỹ tiết kiệm"
  }'
```

---

## 7. PHÂN HỆ QUẢN LÝ NGÂN SÁCH (BUDGETS)

### 7.1 Lấy Danh Sách Ngân Sách
```bash
curl -X GET "http://127.0.0.1/api/v1/budgets" \
  -H "Authorization: Bearer <TOKEN_CỦA_BẠN>"
```

### 7.2 Tạo Ngân Sách Chi Tiêu
```bash
curl -X POST "http://127.0.0.1/api/v1/budgets" \
  -H "Authorization: Bearer <TOKEN_CỦA_BẠN>" \
  -H "Content-Type: application/json" \
  -d '{
    "category_id": "<CATEGORY_ID>",
    "amount_limit": 2500000.0
  }'
```

---

## 8. PHÂN HỆ HEO ĐẤT THÔNG MINH IOT (SMART PIGGY IOT)

### 8.1 Ghép Nối Heo Đất Mới (Pairing)
```bash
curl -X POST "http://127.0.0.1:8000/api/v1/smart_piggy/pair" \
  -H "Authorization: Bearer <TOKEN_CỦA_BẠN>" \
  -H "Content-Type: application/json" \
  -d '{
    "mac_address": "24:6F:28:9A:BC:DE",
    "device_name": "Heo Đất Thông Minh Phòng Khách",
    "pairing_code": "123456"
  }'
```

### 8.2 Luồng Nạp Tiền Từ Cảm Biến Phần Cứng (ESP32 Ingestion)
```bash
curl -X POST "http://127.0.0.1:8000/api/v1/smart_piggy/drop-money" \
  -H "Content-Type: application/json" \
  -d '{
    "mac_address": "24:6F:28:9A:BC:DE",
    "coin_value": 50000.0,
    "weight_delta_grams": 1.2,
    "sensor_confidence": 0.98,
    "nonce": "test-nonce-12345",
    "timestamp": 1771934400
  }'
```

### 8.3 Đồng Bộ Tiền Đút Ngoại Tuyến (Offline Batch Sync)
```bash
curl -X POST "http://127.0.0.1:8000/api/v1/smart_piggy/sync-offline-batch" \
  -H "Content-Type: application/json" \
  -d '{
    "mac_address": "24:6F:28:9A:BC:DE",
    "batch_items": [
      {"offline_tx_id": "off-01", "coin_value": 20000.0},
      {"offline_tx_id": "off-02", "coin_value": 50000.0}
    ]
  }'
```

### 8.4 Lấy Danh Sách & Lịch Sử Đút Tiền Heo Đất
```bash
curl -X GET "http://127.0.0.1:8000/api/v1/smart_piggy/devices" \
  -H "Authorization: Bearer <TOKEN_CỦA_BẠN>"

curl -X GET "http://127.0.0.1:8000/api/v1/smart_piggy/devices/<DEVICE_ID>/history?limit=50" \
  -H "Authorization: Bearer <TOKEN_CỦA_BẠN>"
```

### 8.5 Cảnh Báo Rung Lắc / Chống Trộm / Đập Heo
```bash
curl -X POST "http://127.0.0.1:8000/api/v1/smart_piggy/devices/<DEVICE_ID>/tamper-alert" \
  -H "Authorization: Bearer <TOKEN_CỦA_BẠN>" \
  -H "Content-Type: application/json" \
  -d '{
    "sensor_type": "MPU6050_TILT",
    "intensity_level": "CRITICAL",
    "details": "Phát hiện heo đất bị dốc ngược và rung lắc dữ dội."
  }'
```

### 8.6 Điều Khiển Đèn LED RGB Trên Lưng Heo Từ Mobile App
```bash
curl -X POST "http://127.0.0.1:8000/api/v1/smart_piggy/devices/<DEVICE_ID>/led-control" \
  -H "Authorization: Bearer <TOKEN_CỦA_BẠN>" \
  -H "Content-Type: application/json" \
  -d '{
    "color_hex": "#00FF00",
    "effect_mode": "RAINBOW",
    "duration_seconds": 10
  }'
```

---

## 9. PHÂN HỆ TRÍ TUỆ NHÂN TẠO HEO ĐẤT & OCR (AI & OCR SUITE)

### 9.1 AI Dự Báo Ngày Đầy Heo / Hoàn Thành Mục Tiêu
```bash
curl -X GET "http://127.0.0.1:8000/api/v1/smart_piggy/ai/deposit-forecast" \
  -H "Authorization: Bearer <TOKEN_CỦA_BẠN>"
```

### 9.2 AI Phân Tích Thói Quen Tiết Kiệm & Tính Kiên Trì
```bash
curl -X GET "http://127.0.0.1:8000/api/v1/smart_piggy/ai/behavior-analysis" \
  -H "Authorization: Bearer <TOKEN_CỦA_BẠN>"
```

### 9.3 Cấp Độ Gamification & Huy Hiệu Heo Đất
```bash
curl -X GET "http://127.0.0.1:8000/api/v1/smart_piggy/gamification/status" \
  -H "Authorization: Bearer <TOKEN_CỦA_BẠN>"
```

### 9.4 Quét Hóa Đơn OCR Thông Minh
```bash
curl -X POST "http://127.0.0.1:8000/api/v1/ocr/scan" \
  -H "Authorization: Bearer <TOKEN_CỦA_BẠN>" \
  -H "Content-Type: application/json" \
  -d '{
    "image_url": "https://sample-invoices.local/bill_01.png"
  }'
```

### 9.5 AI Chấm Điểm Sức Khỏe Tài Chính Tổng Quan
```bash
curl -X GET "http://127.0.0.1:8000/api/v1/ai/spending-score" \
  -H "Authorization: Bearer <TOKEN_CỦA_BẠN>"

curl -X GET "http://127.0.0.1:8000/api/v1/ai/recommendations" \
  -H "Authorization: Bearer <TOKEN_CỦA_BẠN>"
```

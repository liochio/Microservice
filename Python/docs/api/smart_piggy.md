# 🐷 TÀI LIỆU KỸ THUẬT: PHÂN HỆ HEO ĐẤT THÔNG MINH IOT & AI (SMART PIGGY CORE)

## 📌 1. Tổng Quan & Kiến Trúc Phần Cứng IoT
Phân hệ **Smart Piggy Bank IoT & AI Core** là giải pháp cầu nối giữa **Thế giới Thực (Tiền mặt vật lý)** và **Thế giới Số (FinTech Digital Wallet)**:
- **Vi điều khiển lõi**: ESP32 Dual-Core 240MHz (Tích hợp WiFi 802.11 b/g/n & Bluetooth BLE).
- **Cảm biến khe nhét tiền (Optical / Coin Slot)**: Nhận diện tờ tiền hoặc đồng xu rơi qua khe.
- **Cảm biến cân nặng (Load Cell HX711)**: Cân điện tử dưới đáy bụng heo để đối soát khối lượng thực tế $\Delta W = W_{\text{sau}} - W_{\text{trước}}$ (Chống đút giấy rác/tiền giả).
- **Cảm biến rung lắc / gia tốc (MPU6050 / SW-420)**: Phát hiện hành vi bưng bê, dốc ngược hoặc đập phá heo để kích hoạt còi hú chống trộm.
- **Đèn LED RGB & Loa Buzzer**: Phát hiệu ứng ánh sáng vui mừng và âm thanh "Oink Oink" khi đút tiền thành công.

---

## 🛡️ 2. Luồng Nạp Tiền Bọc Thép 6 Tầng (Ingestion Security Pipeline)

```
 [Phần Cứng ESP32]
   │
   ├─► Cảm biến Quang + Load Cell cân nặng ──► Đo Mệnh giá & Khối lượng
   ├─► Tạo gói tin có: Timestamp + Nonce + HMAC-SHA256 Signature
   │
   ├─► [CÓ MẠNG] ────► POST /api/v1/smart_piggy/drop-money
   └─► [MẤT MẠNG] ───► Lưu Flash Memory ──► Batch Sync khi có lại WiFi
         │
         ▼
 [BACKEND CORE ENGINE (ACID TRANSACTION)]
   │
   ├─► 1. Verify HMAC-SHA256 + Check Nonce (Chống Replay Attack 100%)
   ├─► 2. Đối chiếu cảm biến cân nặng (Chống đút giấy rác)
   ├─► 3. Ghi `smart_piggy_coin_logs` + Tăng số dư Ví `SMART_PIGGY` (SQL Transaction)
   ├─► 4. Cộng điểm Gamification (1,000 VND = 1 Point) & Thăng cấp Level
   ├─► 5. AI Cập nhật Dự báo ngày đầy heo & Phân tích thói quen
   │
   ├─► Phản hồi ESP32: Lệnh bật đèn LED RGB + Buzzer vui mừng
   └─► Bắn WebSocket: "Ting ting" tức thì lên App điện thoại
```

---

## 🚀 3. Danh Sách Chi Tiết Các Endpoint API

### 3.1 Ghép Nối Thiết Bị Heo Đất Mới (Pairing)
- **Endpoint**: `POST /api/v1/smart_piggy/pair`
- **Mục đích**: Liên kết địa chỉ MAC của chip ESP32 với tài khoản người dùng và ví tích lũy.
- **Header**: `Authorization: Bearer <ACCESS_TOKEN>`
- **Payload Request**:
```json
{
  "mac_address": "24:6F:28:AB:CD:EF",
  "device_name": "Heo Đất Phòng Khách",
  "pairing_code": "123456"
}
```
- **Response Thành Công (201 Created)**:
```json
{
  "success": true,
  "error_code": "SMART_PIGGY_SYNC_SUCCESS",
  "message": "Ghép nối thiết bị Heo đất thông minh thành công.",
  "data": {
    "id": "piggy-device-uuid-001",
    "mac_address": "24:6F:28:AB:CD:EF",
    "device_name": "Heo Đất Phòng Khách",
    "wallet_id": "wallet-savings-uuid-001",
    "total_coins_dropped": 0.0,
    "status": "ONLINE"
  }
}
```

---

### 3.2 Luồng Nạp Tiền Từ Cảm Biến Heo Đất (Sensor Ingestion)
- **Endpoint**: `POST /api/v1/smart_piggy/drop-money`
- **Mục đích**: ESP32 bắn dữ liệu tiền vừa đút về Backend để cộng tiền tự động vào ví số.
- **Payload Request**:
```json
{
  "mac_address": "24:6F:28:AB:CD:EF",
  "coin_value": 50000.0,
  "weight_delta_grams": 1.2,
  "sensor_confidence": 0.98,
  "nonce": "e4d2a1b9-8c7f",
  "timestamp": 1771934400
}
```
- **Response Thành Công (200 OK)**:
```json
{
  "success": true,
  "error_code": "SMART_PIGGY_SYNC_SUCCESS",
  "message": "Đút tiền vào Heo đất thành công. Số dư và điểm tích lũy đã được cập nhật!",
  "data": {
    "device_id": "piggy-device-uuid-001",
    "coin_value_deposited": 50000.0,
    "new_wallet_balance": 1550000.0,
    "gamification_points_earned": 50,
    "current_total_points": 1250,
    "current_level": 2,
    "level_title": "Heo Con Chăm Chỉ (Diligent Piggy)",
    "hardware_command": {
      "led_rgb": "#00FF00",
      "led_effect": "HAPPY_PULSE",
      "buzzer_beeps": 2,
      "display_text": "OINK! +50,000 VND"
    }
  }
}
```

---

### 3.3 Đồng Bộ Tiền Đút Ngoại Tuyến Khi Có Lại WiFi (Offline Batch Sync)
- **Endpoint**: `POST /api/v1/smart_piggy/sync-offline-batch`
- **Mục đích**: ESP32 đẩy danh sách các lần đút tiền khi mất mạng đã lưu trong bộ nhớ Flash.
- **Payload Request**:
```json
{
  "mac_address": "24:6F:28:AB:CD:EF",
  "batch_items": [
    {
      "offline_tx_id": "off-tx-001",
      "coin_value": 20000.0,
      "dropped_at": "2026-08-25T14:30:00"
    },
    {
      "offline_tx_id": "off-tx-002",
      "coin_value": 50000.0,
      "dropped_at": "2026-08-25T18:15:00"
    }
  ]
}
```

---

### 3.4 Xem Lịch Sử Bỏ Ống Heo Vật Lý
- **Endpoint**: `GET /api/v1/smart_piggy/devices/{device_id}/history?limit=50`
- **Mục đích**: Xem chi tiết từng mệnh giá, thời gian và tổng tiền đã bỏ vào ống heo.

---

### 3.5 Cảnh Báo An Ninh: Rung Lắc / Chống Trộm / Đập Heo
- **Endpoint**: `POST /api/v1/smart_piggy/devices/{device_id}/tamper-alert`
- **Payload Request**:
```json
{
  "sensor_type": "MPU6050_TILT_AND_SHAKE",
  "intensity_level": "CRITICAL",
  "details": "Heo đất bị dốc ngược góc 120 độ và rung lắc mạnh liên tục."
}
```
- **Response**: Trả về lệnh kích hoạt còi hú `2000Hz` và chớp đèn LED đỏ cảnh báo `STROBE_ALARM`.

---

### 3.6 Điều Khiển Đèn LED RGB Trên Lưng Heo Đất Từ Mobile App
- **Endpoint**: `POST /api/v1/smart_piggy/devices/{device_id}/led-control`
- **Payload Request**:
```json
{
  "color_hex": "#FF9800",
  "effect_mode": "RAINBOW",
  "duration_seconds": 10
}
```

---

## 🧠 4. Các API Trí Tuệ Nhân Tạo (AI Smart Piggy Advisor)

### 4.1 AI Dự Báo Ngày Đầy Heo / Hoàn Thành Mục Tiêu
- **Endpoint**: `GET /api/v1/smart_piggy/ai/deposit-forecast`
- **Thuật toán**: Mô hình hồi quy chuỗi thời gian phân tích tốc độ tích lũy trung bình ngày $\bar{v}$ và khoảng tiền còn lại $\Delta M$.
- **Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "device_id": "piggy-device-uuid-001",
    "current_balance": 1550000.0,
    "target_amount": 5000000.0,
    "progress_percentage": 31.0,
    "average_daily_saving": 35000.0,
    "estimated_days_remaining": 99,
    "estimated_completion_date": "2026-12-02",
    "confidence_score": 0.94,
    "ai_financial_advice": "Bạn đang tích lũy ổn định ~35,000 VND/ngày. Hãy thử tăng thêm 10,000 VND mỗi lần đút để về đích sớm hơn 2 tuần."
  }
}
```

---

### 4.2 AI Phân Tích Thói Quen Tiết Kiệm & Tính Kiên Trì
- **Endpoint**: `GET /api/v1/smart_piggy/ai/behavior-analysis`
- **Response**:
  - `most_frequent_day_of_week`: Ngày đút tiền nhiều nhất (Ví dụ: Chủ Nhật).
  - `favorite_coin_denomination`: Mệnh giá đút thường xuyên nhất (Ví dụ: 50,000 VND).
  - `saving_consistency_score`: Điểm kiên trì (0 - 100).
  - `behavior_persona`: Danh hiệu tính cách ("Chiến Binh Tiết Kiệm Bền Bỉ").

---

### 4.3 Cấp Độ Gamification & Huy Hiệu Heo Đất
- **Endpoint**: `GET /api/v1/smart_piggy/gamification/status`
- **Bảng Cấp Độ**:
  - **Level 1**: Heo Đất Sơ Sinh (0 - 500 điểm)
  - **Level 2**: Heo Con Chăm Chỉ (501 - 2,000 điểm)
  - **Level 3**: Chiến Binh Tiết Kiệm (2,001 - 5,000 điểm)
  - **Level 4**: Bậc Thầy Tích Lũy (5,001 - 10,000 điểm)
  - **Level 5**: Đại Gia Heo Vàng (> 10,000 điểm)

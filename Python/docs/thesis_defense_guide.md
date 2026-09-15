# 🎓 CẨM NANG BẢO VỆ ĐỒ ÁN TỐT NGHIỆP: HỆ THỐNG BACKEND FINTECH & HEO ĐẤT THÔNG MINH IOT - AI

## 📌 1. Thông Tin Đề Tài & Tóm Tắt Ý Nghĩa Thực Tiễn
- **Tên đề tài**: Nghiên cứu, thiết kế và phát triển Hệ thống Quản trị Tài chính Cá nhân Thông minh kết hợp Phần cứng Heo Đất IoT (ESP32) và Trí tuệ Nhân tạo (AI Financial & Predictive Analytics).
- **Công nghệ cốt lõi**:
  - **Backend**: Python FastAPI (Asynchronous High-Throughput), SQLAlchemy ORM, Pydantic V2.
  - **Cơ sở dữ liệu**: MySQL Enterprise 8.0 (ACID Transactions, Multi-indexing, UTF8MB4).
  - **Bảo mật**: HMAC-SHA256 Signatures, Nonce Replay-Proof, JWT Dual-Token, 33 RBAC Granular Permissions.
  - **Phần cứng IoT**: ESP32 Dual-Core 240MHz, Optical Sensor, HX711 Load Cell, MPU6050 Accelerometer, RGB LED, Buzzer.
  - **Thời gian thực (Real-time)**: WebSocket Live Connection Pooling.
  - **Trí tuệ nhân tạo (AI/ML)**: Time-Series Regression Forecasting, Financial Behavioral Pattern Recognition, Smart OCR Invoice Extraction.
  - **Cổng thanh toán**: Chuẩn mở VietQR NAPAS 247 & Webhook Engine.

---

## 🏛️ 2. Sơ Đồ Kiến Trúc Hệ Thống (Architecture Blueprint)

'''
                            [NGƯỜI DÙNG / APP DI ĐỘNG]
                                     │    ▲
                  HTTP REST (69 APIs)│    │ WebSocket Live Stream (Ting-ting)
                                     ▼    │
      ┌────────────────────────────────────────────────────────────────────────┐
      │                  FASTAPI BACKEND CORE ENGINE                           │
      │                                                                        │
      │  ┌──────────────────────┬──────────────────────┬────────────────────┐  │
      │  │  Authentication     │  Financial Services  │  Smart Piggy Core  │  │
      │  │  - JWT & RBAC        │  - Wallets & Topup   │  - ESP32 Ingestion │  │
      │  │  - OTP Verification  │  - ACID Transfers    │  - HMAC & Nonce    │  │
      │  │  - User Profile      │  - Budgets & Goals   │  - Offline Sync    │  │
      │  └──────────────────────┴──────────────────────┴────────────────────┘  │
      │  ┌──────────────────────┬──────────────────────┬────────────────────┐  │
      │  │  AI & Analytics      │  Payment & Webhook   │  Real-time Engine  │  │
      │  │  - Time-Series Pred  │  - VietQR NAPAS 247  │  - WebSocket Pool  │  │
      │  │  - Behavior Scoring  │  - Auto Balance Credit│ - Notifications   │  │
      │  │  - OCR Receipt Scan  │  - Statement Export  │  - Parent Bonus    │  │
      │  └──────────────────────┴──────────────────────┴────────────────────┘  │
      └───────────────────────────────────┬────────────────────────────────────┘
                                          │
                                          ▼
                               [MYSQL DATABASE 8.0]
'''

---

## 🛡️ 3. Các Điểm Sáng Kỹ Thuật Khi Trình Bày Trước Hội Đồng

### 3.1 Luồng Nạp Tiền Bọc Thép 6 Tầng (ACID Ingestion Pipeline)
1. **Chống giả mạo thiết bị (Anti-Spoofing)**: Xác thực địa chỉ MAC cứng của chip ESP32 được ghép nối với User.
2. **Chống tấn công phát lại (Anti-Replay Attack)**: Gói tin chứa 'Timestamp' và 'Nonce' ngẫu nhiên được ký điện tử bằng 'HMAC-SHA256'.
3. **Đối soát cảm biến kép (Dual-Sensor Cross-Validation)**: Kết hợp mắt đọc quang học (mệnh giá) và cân điện tử Load Cell HX711 đo khối lượng $\Delta W$ để chống đút tiền giả hoặc giấy rác.
4. **Giao dịch ACID hai đầu**: Tăng số dư Ví 'SMART_PIGGY' và tạo bản ghi 'Transaction (INCOME)' trong cùng 1 Database Transaction duy nhất.
5. **Cha Mẹ Nhân Đôi Tiền (Parent Matching Bonus)**: Tự động trích tiền từ Ví Cha Mẹ thưởng thêm $50\% - 100\%$ vào Ví Heo của con.
6. **Phản hồi phần cứng & Bắn WebSocket**: Trả về mã lệnh bật đèn LED RGB vui mừng + còi Buzzer cho ESP32, đồng thời bắn WebSocket làm rung chuông "Ting ting" trên điện thoại cha mẹ và con.

---

## 🎤 4. Bộ Câu Hỏi Phản Biện Thường Gặp & Cách Trả Lời (FAQ Jury Defense)

### ❓ Câu 1: Nếu Heo đất bị mất mạng WiFi khi đang đút tiền thì dữ liệu có bị mất không?
> **Trả lời**: Hệ thống có cơ chế **Offline Flash Memory Buffering**. Khi không có kết nối WiFi, vi điều khiển ESP32 sẽ ghi nhận lần đút tiền vào bộ nhớ Flash nội bộ. Khi có lại mạng WiFi, ESP32 tự động gọi API 'POST /api/v1/smart_piggy/sync-offline-batch' để đồng bộ toàn bộ danh sách tiền ngoại tuyến về máy chủ theo chuẩn ACID.

### ❓ Câu 2: Làm sao hệ thống đảm bảo một request chuyển tiền hoặc nạp tiền không bị thực thi 2 lần nếu mạng chập chờn?
> **Trả lời**: Hệ thống triển khai **Idempotency Engine** ở tầng Middleware. Mỗi request tài chính mang một 'Idempotency-Key' (hoặc 'Nonce'). Nếu client vô tình gửi lặp, Backend sẽ trả ngay kết quả đã lưu trong Cache O(1) mà không thực thi trừ tiền lần thứ hai.

### ❓ Câu 3: Mô hình AI dự báo ngày đầy heo hoạt động như thế nào?
> **Trả lời**: Thuật toán sử dụng mô hình phân tích hồi quy chuỗi thời gian dựa trên tốc độ bỏ ống heo thực tế trung bình ngày $ar{v} = rac{\sum \Delta M}{\Delta t}$ và mức độ biến thiên độ lệch chuẩn. Từ đó tính toán số ngày còn lại $D = rac{M_{	ext{mục tiêu}} - M_{	ext{hiện tại}}}{ar{v}}$ kèm hệ số tin cậy (Confidence Score).

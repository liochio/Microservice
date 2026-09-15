# 🐍 LIOCHIO FINTECH SATELLITE & IOT BACKEND (FASTAPI :8000)

## 1. Giới Thiệu
Phân hệ ứng dụng tốc độ cao phục vụ người dùng cuối (Mobile/Web Client), Heo Đất Thông Minh IoT (Smart Piggy Bank ESP32) và Trí tuệ nhân tạo (AI Vision, Biometrics, OCR).

## 2. Ranh Giới Kiến Trúc Chuẩn 10/10
- **Database Riêng**: Kết nối độc lập vào 'liochio_app_db'. Không thực hiện bất kỳ câu lệnh SQL xuyên database nào sang Core Backend.
- **Ủy Quyền Xác Thực (Zero Shadow Auth)**: 100% yêu cầu xác thực ('/api/v1/auth/**') được chuyển tiếp trong suốt sang Core IAM Gateway (':8080').
- **Đồng Bộ Sổ Cái (M2M Sync)**: Khi có biến động số dư ví (Topup, Coin-Drop, Transfer), 'CoreApiClient' tự động ký số HMAC-SHA256 và gọi M2M API sang 'ledger-service (:8085)'.

## 3. Khởi Chạy
'''bash
# Cài đặt thư viện
pip install -r requirements.txt

# Khởi động dịch vụ
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
'''

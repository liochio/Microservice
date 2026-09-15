# 🐷 Phân Hệ 08: Heo Đất Thông Minh IoT (Smart Piggy Bank IoT)

## 1. Tổng Quan & Mục Tiêu
Phân hệ **Smart Piggy Bank IoT** kết nối phần cứng Heo đất vật lý (vi điều khiển ESP32, cảm biến nhận diện mệnh giá xu/tiền giấy, màn hình LCD, vòng LED RGB) với hệ thống Backend qua giao thức **MQTT** và **WebSocket**.

### Trạng thái triển khai:
- ⏳ **Đang hoàn thiện**: Mô hình bảng 'iot_devices', 'iot_sensor_data', 'iot_transactions', 'iot_device_logs'.
- ⏳ **Kế hoạch tương lai (Roadmap)**: MQTT Broker Consumer (EMQX/Mosquitto), Cập nhật Firmware OTA cho ESP32, Đèn LED đổi màu theo tiến độ tiết kiệm, Âm thanh chúc mừng khi bỏ tiền vào heo.

---

## 2. Luồng Hoạt Động IoT (IoT Event Pipeline)
1. **Người dùng bỏ tiền vào Heo**:
   - Cảm biến trên Heo nhận diện mệnh giá xu (VD: 5.000 VNĐ).
   - ESP32 đóng gói payload JSON: '{"device_id": "PIGGY_001", "amount": 5000, "timestamp": 1724217000}'.
   - ESP32 ký gói tin với Secret Key và gửi qua MQTT Topic 'devices/piggy/events'.
2. **Backend Xử Lý**:
   - MQTT Consumer giải mã gói tin, xác thực danh tính thiết bị.
   - Tự động cộng số dư vào Ví Heo Đất ('wallet_type = SMART_PIGGY') của tài khoản đã liên kết.
   - Ghi nhận 'iot_transactions' và cập nhật mục tiêu tài chính.
   - Phát sự kiện WebSocket tới ứng dụng Mobile của người dùng để hiển thị thông báo tức thì.

---

## 3. Cấu Trúc Bảng Dữ Liệu
- 'iot_devices': Quản lý danh sách thiết bị Heo đất ('device_code', 'mac_address', 'firmware_version', 'status', 'user_id').
- 'iot_sensor_data': Nhật ký dữ liệu cảm biến (mức pin, nhiệt độ, trạng thái nắp).
- 'iot_transactions': Lịch sử các lần bỏ tiền vào Heo đất.
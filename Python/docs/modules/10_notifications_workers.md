# 📬 Phân Hệ 10: Thông Báo Đa Kênh & Tiến Trình Nền (Notifications & Background Workers)

## 1. Tổng Quan & Mục Tiêu
Phân hệ **Notifications & Workers** chịu trách nhiệm điều phối toàn bộ việc gửi thông báo (Email SMTP, Firebase Push Notification, SMS OTP) và vận hành các tác vụ xử lý bất đồng bộ (Background Workers, Cronjobs).

### Trạng thái triển khai:
- ✅ **Đã hoàn thiện**: Worker quét email thông báo kích hoạt tài khoản (`app/jobs/notification_worker.py`), Đa ngôn ngữ tiêu đề email qua i18n, Template HTML động trong CSDL, Chế độ Simulator in Console khi chạy môi trường Dev.
- ⏳ **Kế hoạch tương lai (Roadmap)**: Đẩy thông báo Push Firebase Cloud Messaging (FCM), Hàng đợi tin cậy qua RabbitMQ / Redis Celery, Cronjob tính lãi suất Heo đất hàng đêm.

---

## 2. Kiến Trúc Tiến Trình Nền (Worker Pipeline)
1. **Ghi nhận sự kiện (Producer)**:
   - Khi có sự kiện (Đăng ký, Giao dịch, Cảnh báo ngân sách), hệ thống chèn bản ghi vào bảng `notifications` với trạng thái `PENDING`.
2. **Tiêu thụ & Xử lý (Consumer Worker)**:
   - `NotificationWorker` chạy nền theo chu kỳ `interval_seconds = 5s`.
   - Quét các thông báo `PENDING`, nạp mẫu email `notification_templates`, ánh xạ các biến `{full_activation_url}`, `{expire_time_str}`.
   - Kết nối cổng SMTP gửi mail thực tế và cập nhật trạng thái `COMPLETED` / `FAILED`.

---

## 3. Cấu Trúc Bảng Dữ Liệu
- `notifications`: Bảng hàng đợi thông báo trung tâm (`id`, `user_id`, `notification_type`, `title`, `content`, `status`).
- `notification_logs`: Lưu vết phản hồi cổng Gateway/SMTP, token kích hoạt, thời gian phản hồi.
- `notification_templates`: Mẫu nội dung email / push notification theo ngôn ngữ.
- `push_devices`: Quản lý FCM Device Token của ứng dụng di động.
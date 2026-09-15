# Worker Service - Background Job Poller & Distributed Tasks

## 1. Giới thiệu tổng quan
'worker-service' là Background Worker chuyên trách chạy ngầm, không mở cổng HTTP public, thực thi các tác vụ định kỳ và xử lý sự kiện bất đồng bộ:
- **Transactional Outbox Poller**: Quét định kỳ bảng 'outbox_events' và phát tán event qua RabbitMQ/Kafka/Internal Bus.
- **ShedLock Distributed Lock**: Đảm bảo trong môi trường multi-instance clustering chỉ có duy nhất 1 node worker được thực thi job tại một thời điểm.
- **Dọn dẹp Token & Log cũ**: Tự động xóa các access token, user session và audit log hết hạn.

## 2. Kiến trúc & Công nghệ
- **ShedLock**: '@EnableSchedulerLock' với 'LockProvider' từ JDBC DataSource.
- **Spring '@Scheduled'**: Cronjob định kỳ linh hoạt.
- **Database**: Kết nối trực tiếp vào 'liochio_core_db' để xử lý sự kiện cốt lõi.

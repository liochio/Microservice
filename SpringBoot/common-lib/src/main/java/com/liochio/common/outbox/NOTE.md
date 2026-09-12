# Package: com.liochio.common.outbox

## 1. Vai trò & Chức năng
- Cung cấp mô hình Transactional Outbox Pattern đảm bảo tính toàn vẹn dữ liệu và chống mất mát sự kiện phân tán khi Database commit.

## 2. Các thành phần chính
- `OutboxEvent.java`: Entity mapping với bảng cơ sở dữ liệu `outbox_events`.
- `OutboxRepository.java`: Truy vấn các sự kiện đang chờ gửi (`PENDING`).
- `OutboxPublisher.java`: Dịch vụ phát tán sự kiện ghi vào bảng Outbox cùng Transaction.

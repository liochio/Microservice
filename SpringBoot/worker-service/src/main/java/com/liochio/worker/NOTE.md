# Package com.liochio.worker

## Mục đích
Chứa toàn bộ logic xử lý Background Task và Cron Scheduler:
- `OutboxPollerService`: Quét sự kiện chưa gửi trong `outbox_events` (status `PENDING`), đánh dấu `PROCESSED` hoặc `FAILED` với exponential backoff.
- `CleanupScheduledTask`: Lên lịch dọn dẹp các tệp tạm, token blacklist và session quá hạn.

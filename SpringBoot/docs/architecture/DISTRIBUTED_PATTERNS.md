# MẪU THIẾT KẾ PHÂN TÁN & HIỆU NĂNG CAO (DISTRIBUTED PATTERNS & RESILIENCE)

> **Mục tiêu:** Đảm bảo hệ thống đạt độ khả dụng cao (High Availability), nhất quán dữ liệu cuối cùng (Eventual Consistency) và chịu tải lớn.  

---

## 1. TRANSACTIONAL OUTBOX PATTERN (`outbox_events`)

Khi một nghiệp vụ quan trọng diễn ra (ví dụ tạo đơn đặt chỗ hoặc tạo người dùng mới), việc gửi trực tiếp thông điệp qua Message Broker trong cùng Transaction của Database tiềm ẩn rủi ro mất mát dữ liệu nếu Broker gặp sự cố hoặc Transaction bị Rollback.

```
[ HTTP Request: Tạo Booking ]
             │
             ▼
   [ DB Transaction Bắt Đầu ]
             ├── 1. Lưu bản ghi vào bảng `bookings`
             └── 2. Lưu sự kiện vào bảng `outbox_events` (status: PENDING)
             │
             ▼
   [ DB Transaction Commit Thành Công ]
             │
             ▼
   [ Outbox Scheduler / CDC Poller ]
             ├── Đọc các bản ghi PENDING từ `outbox_events`
             ├── Đẩy thông điệp tới RabbitMQ / Kafka / Notification Hub
             └── Cập nhật status = 'PROCESSED'
```

---

## 2. PHÂN TẦNG BỘ NHỚ ĐỆM (2-TIER CACHING: L1 + L2)

- **L1 Cache (Caffeine In-Memory)**: Lưu các dữ liệu siêu tĩnh, truy cập tần suất hàng triệu request/giây (Input Sanitization Rules, System Error Codes, Tenant Base Configs). Thời gian sống TTL: 5 - 15 phút.
- **L2 Cache (Redis Distributed)**: Lưu phiên người dùng, Token Blacklist, Rate Limiting Counters, Dynamic Entity cache và Idempotency Locks.

---

## 3. KHÓA PHÂN TÁN & TÁC VỤ ĐỊNH KỲ (SHEDLOCK - BẢNG 38)

Khi hệ thống mở rộng đa thực thể (Scale horizontally nhiều instance Microservice), các tác vụ `@Scheduled` (dọn dẹp session hết hạn, quét outbox event, backup dữ liệu) sẽ bị chạy trùng lặp giữa các node nếu không có khóa phân tán:

- Tích hợp **ShedLock** sử dụng bảng `shedlock` trên MySQL làm lock provider.
- Node nào giành được lock sẽ thực thi tác vụ, các node còn lại tự động bỏ qua chu kỳ chạy đó.

---

## 4. CHỐNG TRÙNG LẶP REQUEST (IDEMPOTENCY - BẢNG 37)

- Khách hàng bấm thanh toán nhiều lần hoặc mạng bị rớt khi đang xử lý -> Client gửi kèm Header `Idempotency-Key`.
- **`IdempotentAspect`** kiểm tra trên Redis Lock `SETNX`:
  - Nếu key đang xử lý: trả lỗi `409 Conflict`.
  - Nếu key đã hoàn tất trong bảng `idempotency_keys`: trả ngay kết quả cũ mà không thực thi lại giao dịch trừ tiền.

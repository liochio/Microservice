# Package: com.liochio.notification.adapter

## 1. Vai trò & Chức năng
- Cung cấp các triển khai cụ thể của `NotificationStrategy`.

## 2. Các thành phần chính
- `EmailNotificationAdapter.java`: Gửi email SMTP / Mailpit.
- `TelegramNotificationAdapter.java`: Gửi tin nhắn qua Telegram Bot Webhook.
- `WebSocketNotificationAdapter.java`: Đẩy tin nhắn realtime STOMP WebSocket.

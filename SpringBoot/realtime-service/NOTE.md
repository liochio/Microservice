# Realtime Service - Cổng Giao Tiếp Thời Gian Thực (WebSocket & STOMP Broker)

## 1. Giới thiệu tổng quan
`realtime-service` phụ trách toàn bộ kết nối hai chiều thời gian thực (Full-duplex Realtime WebSockets) giữa Server và Client theo giao thức STOMP qua Redis Pub/Sub Hub.

- **Cổng chạy mặc định**: `8087` (`http://localhost:8087`)
- **WebSocket Handshake URL**: `ws://localhost:8080/ws`
- **Hạ tầng Caching & Broker**: Redis 7.x In-Memory

## 2. Các kênh STOMP chính
- `/topic/notifications/{tenantId}/{userId}`: Nhận thông báo tức thì (đơn hàng, thông báo hệ thống, tin nhắn mới).
- `/topic/chat/{tenantId}/{sessionId}`: Chat trực tiếp nhiều người dùng hoặc chat với AI Assistant.
- `/app/chat.send`: Client gửi tin nhắn vào phòng chat.

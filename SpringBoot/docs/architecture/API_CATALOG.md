# DANH MỤC API TOÀN HỆ THỐNG (ENTERPRISE API SPECIFICATION CATALOG)

> **Điểm đón tiếp chung (API Gateway):** `http://localhost:8080`  
> **Tài liệu Swagger OpenAPI 3.0:** `http://localhost:8080/swagger-ui.html`  
> **Headers bắt buộc khi gọi Protected API:**
> - `Authorization: Bearer <accessToken>`
> - `X-Tenant-ID: <tenantId>` (hoặc tự động bóc tách từ Subdomain)
> - `Accept-Language: vi | en | zh`
> - `Idempotency-Key: <unique-uuid>` (tùy chọn với các API POST giao dịch quan trọng)

---

## 1. DỊCH VỤ XÁC THỰC & BẢO MẬT (`auth-service` : 8081)

| Phương Thức | Tuyến Đường (Endpoint) | Quyền Hạn | Mô Tả Chức Năng |
| :--- | :--- | :---: | :--- |
| `POST` | `/api/v1/auth/register` | Public | Đăng ký tài khoản người dùng mới |
| `POST` | `/api/v1/auth/login` | Public | Đăng nhập hệ thống, cấp Access & Refresh Token |
| `POST` | `/api/v1/auth/refresh-token` | Public | Làm mới Access Token (Token Rotation) |
| `POST` | `/api/v1/auth/logout` | Authenticated | Đăng xuất và đưa Refresh Token vào danh sách thu hồi |
| `GET` | `/api/v1/auth/me` | Authenticated | Lấy thông tin cá nhân và quyền hạn người dùng hiện tại |
| `GET` | `/api/v1/auth/security/devices` | Authenticated | Lấy danh sách thiết bị đã đăng nhập của người dùng |
| `GET` | `/api/v1/auth/security/sessions` | Authenticated | Lấy danh sách phiên làm việc đang hoạt động |
| `POST` | `/api/v1/auth/security/sessions/{id}/revoke` | Authenticated | Thu hồi từ xa một phiên làm việc |
| `GET` | `/api/v1/tenants` | `system:admin` | Danh sách tất cả các Tenant (Super Admin) |
| `POST` | `/api/v1/tenants` | `system:admin` | Khởi tạo Tenant mới |
| `GET` | `/api/v1/tenants/features/catalog` | Public | Lấy danh mục tính năng hệ thống (Feature Catalog) |
| `GET` | `/api/v1/tenants/{tenantId}/features` | Authenticated | Lấy danh sách tính năng được cấp phát của Tenant |

---

## 2. DỊCH VỤ THANH TOÁN & ĐẶT CHỖ (`payment-service` : 8085)

| Phương Thức | Tuyến Đường (Endpoint) | Quyền Hạn | Mô Tả Chức Năng |
| :--- | :--- | :---: | :--- |
| `POST` | `/api/v1/payments/bookings` | Authenticated | Tạo đơn đặt chỗ / đơn hàng mới (hỗ trợ Idempotency) |
| `GET` | `/api/v1/payments/bookings` | Authenticated | Lấy danh sách đơn đặt chỗ của người dùng hiện tại |
| `POST` | `/api/v1/payments/create-url` | Authenticated | Tạo link thanh toán VNPay / MoMo / Stripe |
| `POST` | `/api/v1/payments/ipn/{gateway}` | Public | Webhook tiếp nhận kết quả thanh toán từ cổng trung gian |

---

## 3. DỊCH VỤ MEDIA & TẢI TỆP TIN (`media-service` : 8083)

| Phương Thức | Tuyến Đường (Endpoint) | Quyền Hạn | Mô Tả Chức Năng |
| :--- | :--- | :---: | :--- |
| `POST` | `/api/v1/media/upload` | `media:upload` | Tải lên file ảnh / video / tài liệu đơn lẻ |
| `POST` | `/api/v1/media/chunk-upload` | `media:upload` | Tải lên file dung lượng lớn theo từng chunk phân đoạn |
| `GET` | `/api/v1/media` | Authenticated | Lấy danh sách tài sản media của Tenant |

---

## 4. DỊCH VỤ THÔNG BÁO ĐA KÊNH (`notification-service` : 8084)

| Phương Thức | Tuyến Đường (Endpoint) | Quyền Hạn | Mô Tả Chức Năng |
| :--- | :--- | :---: | :--- |
| `POST` | `/api/v1/notifications/send` | `system:admin` | Phát tán thông báo qua Email, Telegram, SMS |
| `GET` | `/api/v1/notifications/history` | Authenticated | Lấy lịch sử thông báo đã gửi của Tenant |
| `WS` | `/ws-notification` | Public | Kết nối STOMP WebSocket nhận thông báo thời gian thực |

---

## 5. ĐỘNG CƠ EAV & THỰC THỂ ĐỘNG (`entity-service` : 8082)

| Phương Thức | Tuyến Đường (Endpoint) | Quyền Hạn | Mô Tả Chức Năng |
| :--- | :--- | :---: | :--- |
| `GET` | `/api/v1/entities/dynamic/{type}` | Public | Lấy danh sách thực thể động theo loại (phân trang) |
| `POST` | `/api/v1/entities/dynamic/{type}` | `entity:write` | Tạo mới một thực thể động (Dynamic Entity) |
| `GET` | `/api/v1/entities/dynamic/{type}/{slug}` | Public | Lấy chi tiết thực thể động theo slug |
| `PUT` | `/api/v1/entities/dynamic/{type}/{id}` | `entity:write` | Cập nhật thực thể động |
| `DELETE`| `/api/v1/entities/dynamic/{type}/{id}` | `entity:delete`| Xóa mềm thực thể động |
| `GET` | `/api/v1/entities/templates` | Public | Lấy danh sách mẫu giao diện Website (Web Templates) |
| `GET` | `/api/v1/entities/templates/types` | Public | Lấy danh mục các loại thực thể động (Entity Types) |
| `GET` | `/api/v1/entities/reviews/{entityId}` | Public | Lấy danh sách đánh giá của thực thể |
| `POST` | `/api/v1/entities/reviews` | Authenticated | Gửi đánh giá / bình luận cho thực thể |
| `POST` | `/api/v1/ai/chat/sessions` | Authenticated | Khởi tạo phiên trò chuyện mới với AI Chatbot |
| `POST` | `/api/v1/ai/chat/sessions/{id}/messages` | Authenticated | Gửi tin nhắn và nhận phản hồi từ AI RAG Assistant |

---

## 6. DỊCH VỤ DU LỊCH & KHÁM PHÁ (`tour-service` : 8091 / `entity-service`)

| Phương Thức | Tuyến Đường (Endpoint) | Quyền Hạn | Mô Tả Chức Năng |
| :--- | :--- | :---: | :--- |
| `GET` | `/api/v1/tours` | Public | Lấy danh sách tour du lịch đang mở bán (phân trang) |
| `GET` | `/api/v1/tours/{slug}` | Public | Lấy thông tin chi tiết tour du lịch theo slug |
| `POST` | `/api/v1/tours` | `entity:write` | Tạo mới một tour du lịch |

---

## 7. DỊCH VỤ ÂM NHẠC & AUDIO (`music-service` : 8092 / `entity-service`)

| Phương Thức | Tuyến Đường (Endpoint) | Quyền Hạn | Mô Tả Chức Năng |
| :--- | :--- | :---: | :--- |
| `GET` | `/api/v1/music/songs` | Public | Lấy danh sách bài hát (phân trang) |
| `GET` | `/api/v1/music/songs/{slug}` | Public | Lấy chi tiết bài hát theo slug kèm audio streaming URL |
| `POST` | `/api/v1/music/songs` | `entity:write` | Thêm bài hát mới vào thư viện |

---

## 8. DỊCH VỤ ĐIỆN ẢNH & VIDEO (`film-service` : 8093 / `entity-service`)

| Phương Thức | Tuyến Đường (Endpoint) | Quyền Hạn | Mô Tả Chức Năng |
| :--- | :--- | :---: | :--- |
| `GET` | `/api/v1/films` | Public | Lấy danh sách các bộ phim (phân trang) |
| `GET` | `/api/v1/films/{slug}` | Public | Lấy thông tin chi tiết phim theo slug |
| `POST` | `/api/v1/films` | `entity:write` | Thêm bộ phim mới vào kho dữ liệu |

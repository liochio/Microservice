# Package: com.liochio.common.dto

## 1. Vai trò & Chức năng
- Định nghĩa các đối tượng truyền tải dữ liệu chuẩn hóa (Data Transfer Objects) giữa Backend và Frontend/Client.

## 2. Các thành phần chính
- `ApiResponse.java`: Cấu trúc phản hồi đồng nhất 100% API {status, message, data, timestamp (Instant UTC ISO-8601)}.
- `PageResponse.java`: Đóng gói phân trang tinh gọn từ Spring Data Page<T>.
- `ClientContextRequest.java`: DTO thu thập IP, User-Agent, Tenant ID, RequestId của Client.

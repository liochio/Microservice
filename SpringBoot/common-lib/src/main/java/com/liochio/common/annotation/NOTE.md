# Package: com.liochio.common.annotation

## 1. Vai trò & Chức năng
- Định nghĩa các Annotation tùy biến phục vụ lọc dữ liệu XSS, phân quyền RBAC và chống trùng lặp request.

## 2. Các thành phần chính
- '@DynamicSanitize': Đặt trên trường String của DTO để tự động làm sạch mã độc XSS/HTML.
- '@RequirePermission': Đặt trên method Controller để kiểm tra quyền hạn 'resource:action'.
- '@Idempotent': Đặt trên endpoint thanh toán/giao dịch để chống click đúp bằng Redis Distributed Lock.

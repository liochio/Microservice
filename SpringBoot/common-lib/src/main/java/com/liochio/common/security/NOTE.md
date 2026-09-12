# Package: com.liochio.common.security

## 1. Vai trò & Chức năng
- Cung cấp hạ tầng bảo mật JWT, xử lý phản hồi 401 Unauthorized / 403 Forbidden và bộ lọc xác thực.

## 2. Các thành phần chính
- `JwtUtils.java`: Tạo và giải mã Token JWT (JJWT 0.12.x) chứa User ID, Tenant ID, Roles, Permissions.
- `JwtAuthenticationFilter.java`: Chặn request, kiểm tra chữ ký Token và nạp quyền vào Spring Security Context.
- `CustomAuthenticationEntryPoint.java`: Trả về JSON 401 chuẩn hóa khi chưa đăng nhập.
- `CustomAccessDeniedHandler.java`: Trả về JSON 403 chuẩn hóa khi thiếu quyền hạn.

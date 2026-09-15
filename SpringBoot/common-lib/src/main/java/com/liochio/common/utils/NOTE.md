# Package: com.liochio.common.utils

## 1. Vai trò & Chức năng
- Cung cấp các hàm tiện ích dùng chung (Utility methods) không trạng thái (stateless).

## 2. Các thành phần chính
- 'SanitizerUtils.java': Xử lý Regex làm sạch HTML, chống XSS, lọc ký tự đặc biệt.
- 'JsonUtils.java': Chuyển đổi an toàn giữa Java Object và JSON String với cấu hình chuẩn UTC.
- 'SecurityUtils.java': Băm mật khẩu BCrypt và trích xuất Bearer Token từ request.

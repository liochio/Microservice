# Package: com.liochio.auth.controller

## 1. Vai trò & Chức năng
- Tiếp nhận các yêu cầu HTTP liên quan đến xác thực và phân quyền (Port 8081).

## 2. Các thành phần chính
- 'AuthController.java': Đăng nhập, đăng ký, refresh token (Token Rotation), logout, lấy thông tin cá nhân.
- 'UserController.java': Quản trị danh sách người dùng và xóa mềm tài khoản.
- 'RoleController.java': Quản lý danh sách vai trò và phân quyền động RBAC.

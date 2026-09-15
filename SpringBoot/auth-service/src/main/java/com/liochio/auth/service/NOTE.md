# Package: com.liochio.auth.service

## 1. Vai trò & Chức năng
- Xử lý toàn bộ logic nghiệp vụ xác thực, cấp phát token JWT và phân quyền.

## 2. Các thành phần chính
- 'AuthService.java': Xác thực mật khẩu BCrypt, cấp Access Token/Refresh Token, cơ chế Refresh Token Rotation, bắn Outbox Event.
- 'UserService.java': Quản trị User, tìm kiếm và xóa mềm.
- 'RoleService.java': Gán quyền động cho các vai trò trong Database.

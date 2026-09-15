# Package: com.liochio.auth.repository

## 1. Vai trò & Chức năng
- Giao tiếp với cơ sở dữ liệu MySQL qua Spring Data JPA.

## 2. Các thành phần chính
- 'UserRepository.java': Sử dụng '@EntityGraph' truy vấn kèm Roles & Permissions chống N+1.
- 'RoleRepository.java': Quản lý vai trò.
- 'PermissionRepository.java': Quản lý quyền hạn.
- 'UserTokenRepository.java': Quản lý phiên và Refresh Token.

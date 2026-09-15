# Package: com.liochio.common.context

## 1. Vai trò & Chức năng
- Quản lý các đối tượng ngữ cảnh yêu cầu theo luồng thực thi (ThreadLocal) xuyên suốt các tầng.

## 2. Các thành phần chính
- 'TenantContext.java': Lưu trữ và truy xuất 'tenantId' cho multi-tenancy.
- 'UserContext.java': Lưu trữ thông tin tài khoản người dùng đã xác thực (User ID, Roles, Permissions).

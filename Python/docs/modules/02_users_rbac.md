# 👥 Phân Hệ 02: Quản Lý Người Dùng & Phân Quyền Động (Users & RBAC)

## 1. Tổng Quan & Mục Tiêu
Phân hệ **Users & RBAC** (Role-Based Access Control) cung cấp cơ chế phân quyền đa tầng linh hoạt theo cây Phân hệ (Modules), Quyền hạn (Permissions) và Vai trò (Roles).

### Trạng thái triển khai:
- ✅ **Đã hoàn thiện**: Mô hình phân quyền 4 tầng ('users' -> 'user_roles' -> 'roles' -> 'role_permissions' -> 'permissions' -> 'modules'), Guard kiểm tra quyền hạn trên RAM ('RoleBasedGuard', 'PermissionGuard').
- ⏳ **Kế hoạch tương lai (Roadmap)**: Phân quyền theo nhóm phòng ban (Department/Tenant RBAC), Quản lý hồ sơ KYC điện tử (eKYC / Căn cước công dân), Tích hợp Avatar Cloud Storage (S3/MinIO).

---

## 2. Ma Trận Phân Quyền (RBAC Matrix)

### 2.1. Cấu trúc 4 tầng
'''
[User] ---> [UserRole] ---> [Role] ---> [RolePermission] ---> [Permission] ---> [Module]
'''
- **Module (Phân hệ)**: Cụm chức năng cấp cao (VD: 'WALLET_MGMT', 'TRANSACTION_MGMT', 'LEDGER_MGMT').
- **Permission (Quyền hành động)**: Quyền cụ thể gắn với endpoint API (VD: 'WALLET_CREATE', 'WALLET_TOPUP', 'USER_BLOCK').
- **Role (Vai trò)**: Tập hợp các Permission (VD: 'SUPER_ADMIN', 'ADMIN', 'USER').

### 2.2. Danh mục Vai trò mặc định
1. **'SUPER_ADMIN'**: Quản trị viên tối cao, sở hữu 100% quyền hạn trong toàn bộ hệ thống.
2. **'ADMIN'**: Quản trị viên vận hành, quản lý người dùng, ví và xem báo cáo kiểm toán.
3. **'USER'**: Người dùng thành viên tiêu chuẩn (quản lý ví cá nhân, nạp tiền, chuyển tiền, tạo ngân sách).

---

## 3. Cơ Chế Guard Kiểm Soát Truy Cập
- **'get_current_user'**: Dependency trích xuất Bearer Token, kiểm tra hết hạn, nạp 'user_id', 'permissions', 'modules' vào 'request.state'.
- **'RoleBasedGuard(required_module)'**: Chặn truy cập nếu người dùng không thuộc Module chỉ định.
- **'PermissionGuard(required_permission)'**: Chặn truy cập nếu người dùng không sở hữu mã Permission cụ thể.

---

## 4. Cấu Trúc Bảng Dữ Liệu
- 'users': Bảng người dùng cốt lõi.
- 'user_profiles': Thông tin hồ sơ mở rộng (địa chỉ, nghề nghiệp, avatar).
- 'roles': Bảng danh mục vai trò.
- 'user_roles': Bảng liên kết người dùng - vai trò (N - N).
- 'modules': Bảng phân hệ menu động phân cấp cha - con.
- 'permissions': Bảng quyền chi tiết.
- 'role_permissions': Bảng liên kết vai trò - quyền (N - N).
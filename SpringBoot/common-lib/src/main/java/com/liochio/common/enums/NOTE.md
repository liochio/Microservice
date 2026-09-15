# Package: com.liochio.common.enums

## 1. Vai trò & Chức năng
- Định nghĩa các kiểu liệt kê chuẩn hóa (Type-safe Enumerations) cho toàn bộ hệ thống.

## 2. Các thành phần chính
- 'RoleEnum.java': Quản lý vai trò người dùng đa người thuê ('SUPER_ADMIN', 'TENANT_ADMIN', 'EDITOR', 'VIEWER').
- 'LanguageEnum.java': Quản lý danh sách ngôn ngữ hỗ trợ ('VI', 'EN', 'ZH').
- 'DynamicFieldType.java': Kiểu dữ liệu trường động cho Hybrid EAV + JSONB ('TEXT', 'NUMBER', 'BOOLEAN', 'JSON_OBJECT'...).
- 'OutboxStatus.java': Trạng thái sự kiện trong Transactional Outbox Pattern ('PENDING', 'PROCESSING', 'PUBLISHED', 'FAILED').
- 'PaymentStatus.java': Trạng thái giao dịch cổng thanh toán ('PENDING', 'SUCCESS', 'FAILED'...).
- 'NotificationChannel.java': Kênh thông báo ('EMAIL', 'SMS', 'TELEGRAM', 'WEBSOCKET', 'IN_APP').
- 'StorageProvider.java': Nhà cung cấp lưu trữ tệp tin ('LOCAL', 'CLOUDINARY', 'S3_COMPATIBLE').

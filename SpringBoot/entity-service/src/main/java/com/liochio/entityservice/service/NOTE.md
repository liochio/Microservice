# Package: com.liochio.entityservice.service

## 1. Vai trò & Chức năng
- Xử lý logic nghiệp vụ cho Portfolio, Dynamic Engine và Server-Driven UI layout.

## 2. Các thành phần chính
- 'PortfolioService.java': Tìm kiếm, thêm/sửa/xóa mềm portfolio, xóa/nạp cache và bắn Outbox event.
- 'DynamicEntityService.java': Quản lý thực thể động, tự động tăng lượt xem.
- 'UiConfigService.java': Quản lý và cache layout schema cho các trang.
- 'FormDefinitionService.java': Quản lý cấu trúc form động.
- 'NavigationMenuService.java': Quản lý menu navigation động.

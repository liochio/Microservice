# Package: com.liochio.entityservice.controller

## 1. Vai trò & Chức năng
- Tiếp nhận các yêu cầu HTTP liên quan đến Dynamic Engine, Portfolio và Server-Driven UI (Port 8082).

## 2. Các thành phần chính
- `PortfolioController.java`: Quản trị dự án Portfolio (tìm kiếm, tạo, sửa, xóa mềm).
- `DynamicEntityController.java`: Quản trị các thực thể động Hybrid EAV + JSONB (`/api/entities/**`).
- `UiConfigController.java`: Cung cấp cấu trúc layout JSON cho Server-Driven UI (`/api/ui-configs/**`).
- `FormDefinitionController.java`: Cung cấp định nghĩa form nhập liệu và validate động (`/api/forms/**`).
- `NavigationMenuController.java`: Cung cấp cấu trúc menu điều hướng động (`/api/menus/**`).

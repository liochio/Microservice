# Package: com.liochio.entityservice.entity

## 1. Vai trò & Chức năng
- Định nghĩa các thực thể kế thừa `BaseEntity` kết hợp cột JSON chuẩn MySQL 8 (`@JdbcTypeCode(SqlTypes.JSON)`).

## 2. Các thành phần chính
- `PortfolioEntity.java`: Bảng `portfolio_items`.
- `DynamicEntity.java`: Bảng `dynamic_entities`.
- `UiConfigurationEntity.java`: Bảng `ui_configurations`.
- `FormDefinitionEntity.java`: Bảng `form_definitions`.
- `NavigationMenuEntity.java`: Bảng `navigation_menus`.

# Package: com.liochio.entityservice.specification

## 1. Vai trò & Chức năng
- Định nghĩa các tiêu chí truy vấn động (JPA Specification) phục vụ tìm kiếm đa điều kiện.

## 2. Các thành phần chính
- `PortfolioSpecification.java`: Lọc portfolio theo category, keyword (title, description), tenantId.
- `DynamicEntitySpecification.java`: Lọc dynamic entity theo entityType, status, keyword, tenantId.

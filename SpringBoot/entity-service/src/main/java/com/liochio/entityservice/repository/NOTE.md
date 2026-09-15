# Package: com.liochio.entityservice.repository

## 1. Vai trò & Chức năng
- Giao tiếp với MySQL thông qua Spring Data JPA & JpaSpecificationExecutor.

## 2. Các thành phần chính
- 'PortfolioRepository.java': Truy vấn dự án Portfolio kèm Specification lọc.
- 'DynamicEntityRepository.java': Truy vấn thực thể động theo slug và tenant.
- 'UiConfigurationRepository.java': Truy vấn cấu hình layout UI.
- 'FormDefinitionRepository.java': Truy vấn định nghĩa form.
- 'NavigationMenuRepository.java': Truy vấn menu navigation.

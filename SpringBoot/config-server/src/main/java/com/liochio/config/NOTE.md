# Package: com.liochio.config

## 1. Vai trò & Chức năng
- Cung cấp Spring Cloud Config Server quản lý toàn bộ cấu hình hệ thống (Port 8888).

## 2. Các thành phần chính
- `ConfigServerApplication.java`: Điểm khởi chạy ứng dụng với annotation `@EnableConfigServer`.
- `shared-configs/application.yml`: Nạp tập trung cấu hình DataSource MySQL, Flyway, Hibernate UTC, Redis, Eureka cho toàn bộ các services con.

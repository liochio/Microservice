# Package: com.liochio.registry

## 1. Vai trò & Chức năng
- Cung cấp dịch vụ Eureka Discovery Server quản lý toàn bộ các Microservices con (Port 8761).

## 2. Các thành phần chính
- `ServiceRegistryApplication.java`: Điểm khởi chạy ứng dụng với annotation `@EnableEurekaServer`.
- `application.yml`: Cấu hình tắt self-registration và bật eviction timer.

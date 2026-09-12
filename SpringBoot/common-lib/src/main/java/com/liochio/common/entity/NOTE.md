# Package: com.liochio.common.entity

## 1. Vai trò & Chức năng
- Cung cấp lớp thực thể cơ sở `BaseEntity` dùng chung cho 100% các Entity trong hệ thống Microservices.

## 2. Các thành phần chính
- `BaseEntity.java`: Tích hợp JPA Auditing (`createdAt`, `updatedAt`, `createdBy`, `lastModifiedBy` chuẩn Instant UTC), Soft Delete (`is_deleted`), và Hibernate Filter (`tenantFilter`) cho Multi-Tenancy.

# Package: com.liochio.common.config

## 1. Vai trò & Chức năng
- Định nghĩa toàn bộ các cấu hình Spring Beans dùng chung cho các Microservices.

## 2. Các thành phần chính
- `JacksonConfig.java`: Cấu hình ObjectMapper chuẩn hóa Instant UTC ISO-8601.
- `JpaAuditingConfig.java`: Kích hoạt JPA Auditing tự động lưu createdBy/lastModifiedBy.
- `RedisConfig.java`: Cấu hình `RedisTemplate<String, Object>` cho L2 Cache.
- `CaffeineCacheConfig.java`: Cấu hình Caffeine L1 In-memory Cache.
- `OpenApiConfig.java`: Cấu hình SpringDoc Swagger OpenAPI 3 với JWT Security Scheme.
- `ShedLockConfig.java`: Cấu hình ShedLock đồng bộ Scheduler ngầm qua Redis Lock Provider.

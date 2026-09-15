# Package: com.liochio.gateway.filter

## 1. Vai trò & Chức năng
- Chặn các yêu cầu HTTP tại cửa ngõ API Gateway trước khi định tuyến tới các Microservices bên trong.

## 2. Các thành phần chính
- 'JwtAuthenticationGatewayFilter.java': Giải mã Token JWT, chặn sớm các yêu cầu trái phép (401), chuyển tiếp User Context qua downstream headers.
- 'TenantExtractionGatewayFilter.java': Trích xuất mã khách thuê ('X-Tenant-ID') từ header hoặc subdomain.

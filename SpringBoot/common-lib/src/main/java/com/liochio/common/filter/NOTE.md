# Package: com.liochio.common.filter

## 1. Vai trò & Chức năng
- Cung cấp các bộ lọc Servlet Filter kiểm soát luồng request đầu vào.

## 2. Các thành phần chính
- `TenantFilter.java`: Bộ lọc ưu tiên cao nhất, trích xuất mã khách thuê từ header `X-Tenant-ID` hoặc subdomain để nạp vào `TenantContext` và kích hoạt Hibernate Multi-Tenancy Filter.

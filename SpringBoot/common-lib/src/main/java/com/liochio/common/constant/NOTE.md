# Package: com.liochio.common.constant

## 1. Vai trò & Chức năng
- Quản lý tập trung 100% các hằng số của toàn bộ hệ thống Microservices, loại bỏ hoàn toàn các chuỗi hardcoded string trong source code.

## 2. Các thành phần chính
- 'AppConstants.java': Hằng số múi giờ UTC, phân trang mặc định, tên microservices.
- 'HeaderConstants.java': Hằng số HTTP headers ('X-Tenant-ID', 'Authorization', 'Accept-Language', 'Idempotency-Key'...).
- 'SecurityConstants.java': Hằng số JWT claims, thời gian sống của token, whitelist URLs.
- 'MessageConstants.java': Hằng số khóa thông báo đa ngôn ngữ i18n ('api.response.*', 'api.error.*').
- 'CacheConstants.java': Tên vùng Cache L1/L2 và TTL cấu hình.
- 'ErrorCodeConstants.java': Bảng mã lỗi số nguyên chuẩn hóa (1000 - 5999).
- 'RegexConstants.java': Mẫu biểu thức chính quy kiểm tra định dạng và làm sạch XSS/HTML.

## 3. Khi nào sử dụng
- Được import vào bất kỳ tầng nào khi cần sử dụng tên header, khóa cache, định dạng ngày tháng hoặc mã lỗi.

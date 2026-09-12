# Package: com.liochio.payment.config

## 1. Vai trò & Chức năng
- Cấu hình Spring Security cho Payment Service.

## 2. Các thành phần chính
- `SecurityConfig.java`: Cho phép IPN Webhook callbacks (`/api/payments/ipn/**`) công khai và yêu cầu xác thực JWT cho API tạo URL thanh toán.

# Package: com.liochio.payment.service

## 1. Vai trò & Chức năng
- Xử lý nghiệp vụ thanh toán, điều phối Strategy qua PaymentFactory và phát sự kiện 'PAYMENT_SUCCESS' vào Outbox Table.

## 2. Các thành phần chính
- 'PaymentService.java': Tạo liên kết thanh toán, xác thực IPN Signature, cập nhật trạng thái đơn hàng và bắn sự kiện Outbox.

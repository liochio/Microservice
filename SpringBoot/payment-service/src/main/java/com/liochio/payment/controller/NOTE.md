# Package: com.liochio.payment.controller

## 1. Vai trò & Chức năng
- Tiếp nhận các yêu cầu thanh toán và callback IPN Webhook từ cổng thanh toán (Port 8085).

## 2. Các thành phần chính
- 'PaymentController.java': API '/api/payments/create-url', '/api/payments/orders/{orderId}' có gắn '@Idempotent' chống click đúp.
- 'IpnWebhookController.java': API '/api/payments/ipn/vnpay', '/api/payments/ipn/stripe', '/api/payments/ipn/momo'.

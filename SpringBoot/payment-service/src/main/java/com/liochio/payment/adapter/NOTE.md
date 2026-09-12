# Package: com.liochio.payment.adapter

## 1. Vai trò & Chức năng
- Cung cấp các triển khai cụ thể của `PaymentStrategy` cho từng cổng thanh toán.

## 2. Các thành phần chính
- `VNPayPaymentAdapter.java`: Tích hợp VNPay với thuật toán ký mã hóa HMAC SHA512 và xác thực IPN callback.
- `StripePaymentAdapter.java`: Tích hợp Stripe Checkout và Webhook.
- `MomoPaymentAdapter.java`: Tích hợp ví điện tử MoMo QR Code.

# Package: com.liochio.common.pattern

## 1. Vai trò & Chức năng
- Cung cấp các mẫu thiết kế hướng đối tượng chuẩn GoF (Gang of Four) cho toàn bộ hệ thống Microservices:
  - **Strategy Pattern (`strategy/`)**: Định nghĩa các giao diện chiến lược lưu trữ (`StorageStrategy`), thông báo (`NotificationStrategy`), thanh toán (`PaymentStrategy`).
  - **Factory Pattern (`factory/`)**: Các nhà máy tra cứu và khởi tạo Strategy tương ứng (`StorageFactory`, `NotificationFactory`, `PaymentFactory`).
  - **Template Method Pattern (`template/`)**: Các khung xử lý giao dịch chuẩn (`AbstractPaymentProcessor`, `AbstractNotificationSender`).
  - **Builder Pattern (`builder/`)**: Xây dựng đối tượng phức tạp.

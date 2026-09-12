# Kafka Event Streaming Package

## Chức năng
Gói pp/kafka phụ trách tích hợp hạ tầng hàng đợi sự kiện phân tán Apache Kafka:
1. **Producer**: Phát hành các sự kiện tài chính quan trọng (TRANSACTION_COMPLETED, TRANSFER_SUCCESS, SMART_PIGGY_UNLOCKED, BUDGET_EXCEEDED).
2. **Consumer**: Lắng nghe các sự kiện IAM từ Spring Boot Core (USER_REGISTERED, USER_DEACTIVATED, ROLE_CHANGED) để đồng bộ dữ liệu người dùng cục bộ.
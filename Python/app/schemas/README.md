# Schemas Package (Data Transfer Objects & Pydantic Validation)

## Chức năng
Gói pp/schemas chứa toàn bộ Pydantic models dùng để:
1. Validate dữ liệu đầu vào từ client HTTP requests.
2. Serialization và định dạng dữ liệu đầu ra cho HTTP responses tuân thủ chuẩn ApiResponse[T].
3. Khai báo kiểu dữ liệu an toàn cho Logical Foreign Key user_id: str (định dạng usr_xxxxxxxx).

## Danh mục Schemas
- user.py: DTO thông tin người dùng, profile, avatar.
- wallet.py: DTO nạp tiền, rút tiền, kiểm tra số dư ví số.
- 	ransaction.py: DTO lịch sử biến động số dư, giao dịch.
- 	ransfer.py: DTO chuyển tiền P2P nội bộ giữa các tài khoản.
- udget.py: DTO lập ngân sách chi tiêu, cảnh báo hạn mức.
- goal.py: DTO mục tiêu tiết kiệm, tiến độ tích lũy.
- piggy.py: DTO heo đất thông minh Smart Piggy, mở khóa với X-Action-Token.
- i.py: DTO chatbot tư vấn tài chính, phân tích thói quen chi tiêu.
- ocr.py: DTO bóc tách hóa đơn hóa đơn bán lẻ qua Vision AI.
- 
otification.py: DTO cấu hình thông báo và lịch sử thông báo push.
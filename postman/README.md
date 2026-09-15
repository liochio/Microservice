# 🚀 Hướng Dẫn Sử Dụng Postman Collection Hệ Sinh Thái Liochio Microservices (Sẵn Sàng 1-Click Run)

Toàn bộ hệ sinh thái **Liochio Microservices & FinTech** đã được nạp sẵn dữ liệu mẫu (Seed Data), chuẩn hóa tài khoản, mật khẩu và gán nhãn thiết bị tin cậy (**Trusted Device**) để bạn có thể **Import vào Postman / Thunder Client / Bruno là bấm chạy ngay 100% không cần cấu hình thêm**.

---

## 📂 Danh Sách File Postman Đã Cập Nhật

1. **Bộ Collection (16 Modules, 70+ APIs):**
   ['postman/Liochio_Microservices_API.postman_collection.json'](file:///D:/Github/Back-end/Microservice/postman/Liochio_Microservices_API.postman_collection.json)
2. **Bộ Biến Môi Trường (Environment):**
   ['postman/Liochio_Local_Environment.postman_environment.json'](file:///D:/Github/Back-end/Microservice/postman/Liochio_Local_Environment.postman_environment.json)

---

## 🔑 Thông Tin Tài Khoản Mặc Định Đã Đồng Bộ

| Username | Password | Vai trò (Role) | Trạng thái |
| :--- | :--- | :--- | :--- |
| **'admin'** | **'Password123@'** *(hoặc 'password123')* | 'ROLE_SUPER_ADMIN' | 'ACTIVE' |
| **'fintech_user01'** | **'Password123@'** | 'ROLE_CUSTOMER', 'ROLE_VIEWER' | 'ACTIVE' |
| **'fintech_user02'** | **'Password123@'** | 'ROLE_CUSTOMER' | 'ACTIVE' |

> [!NOTE]
> Thiết bị 'fp_postman_client_01' đã được cấu hình sẵn là **Trusted Device** trong cơ sở dữ liệu 'liochio_auth_db', nên khi bạn bấm Login từ Postman, hệ thống sẽ **cấp Token ngay lập tức** mà không cần qua bước OTP 2FA.

---

## 🛠️ Hướng Dẫn 2 Bước Chạy Ngay (1-Click Run)

### Bước 1: Import 2 File vào Postman
1. Mở ứng dụng **Postman**.
2. Bấm nút **Import** (góc trên bên trái).
3. Kéo thả đồng thời 2 file:
   - 'Liochio_Microservices_API.postman_collection.json'
   - 'Liochio_Local_Environment.postman_environment.json'
4. Ở góc trên cùng bên phải, chọn Environment: **'Liochio Microservices Local (Nginx Port 80)'**.

### Bước 2: Bấm Login để Tự Động Kích Hoạt Toàn Bộ Collection
1. Mở thư mục **'01. IAM Core - Authentication (Java)'**.
2. Chọn **'1. Đăng nhập FinTech User (Login fintech_user01)'** hoặc **'2. Đăng nhập Quản trị viên (Login Admin)'**.
3. Bấm **'Send'**.
4. **Xong!** Đoạn script tích hợp sẵn sẽ **tự động bắt Token RS256 và lưu vào biến '{{token}}'**.
👉 Giờ đây bạn có thể mở bất kỳ thư mục nào (Ví điện tử, Chuyển tiền P2P, Heo đất thông minh, Báo cáo tài chính, Dynamic Entity, Tour, Music, Film, Payments...) và bấm **'Send'** để chạy ngay lập tức!

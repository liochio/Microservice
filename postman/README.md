# 🚀 Hướng Dẫn Sử Dụng Postman Collection Hệ Sinh Thái Liochio Microservices (Sẵn Sàng 1-Click Run 100%)

Toàn bộ hệ sinh thái **Liochio Microservices & FinTech** đã được nạp sẵn toàn bộ dữ liệu mẫu (Pre-set Full Seed Data), chuẩn hóa tài khoản, mật khẩu, cơ chế bắt Token RS256 hai chiều (Spring Boot Core IAM & Python FastAPI Resource), sinh chữ ký số bảo mật **M2M HMAC-SHA256** theo thời gian thực và gán nhãn thiết bị tin cậy (**Trusted Device**) `fp_postman_client_01`.

Bạn chỉ cần **Import vào Postman / Bruno / Thunder Client là bấm chạy ngay 100% không cần sửa bất kỳ trường dữ liệu nào**.

---

## 📂 Danh Sách File Postman Đã Chuẩn Hóa & Nâng Cấp

| File | Mô tả chi tiết | Mục đích sử dụng |
| :--- | :--- | :--- |
| [`Liochio_Microservices_API.postman_collection.json`](file:///d:/Github/Back-end/Microservice/postman/Liochio_Microservices_API.postman_collection.json) | **Bộ Collection Hợp Nhất Toàn Diện** (11 Thư mục, 65+ APIs) bao gồm cả Java Spring Boot IAM, Ledger Sổ cái kép, Payment, Worker và Python FinTech IoT Smart Piggy. | Chạy toàn diện toàn bộ nền tảng chỉ trong 1 bộ Collection duy nhất. |
| [`01_Liochio_Core_Platform_API.postman_collection.json`](file:///d:/Github/Back-end/Microservice/postman/01_Liochio_Core_Platform_API.postman_collection.json) | **Phân hệ Core Banking & IAM** (Spring Boot Port 8081): Xác thực RS256/JWKS, Đăng ký, QR Login, Smart OTP RFC 6238, eKYC CCCD, Sổ cái kép Double-Entry Ledger, Cổng thanh toán & Worker EOD. | Dành cho kiểm thử chuyên sâu tầng Core IAM & Sổ cái ngân hàng. |
| [`02_Liochio_Python_Resource_API.postman_collection.json`](file:///d:/Github/Back-end/Microservice/postman/02_Liochio_Python_Resource_API.postman_collection.json) | **Phân hệ FinTech & Smart Piggy IoT** (Python FastAPI Port 8000): Ghép đôi Heo đất ESP32, Thả xu cảm biến TCRT5000, Đồng bộ ngoại tuyến LittleFS, Rút tiền 2 pha Mutex, AI Dự báo chi tiêu & Ngân sách. | Dành cho kiểm thử chuyên sâu phân hệ IoT phần cứng và AI Cố vấn. |
| [`Liochio_Local_Environment.postman_environment.json`](file:///d:/Github/Back-end/Microservice/postman/Liochio_Local_Environment.postman_environment.json) | **Bộ Biến Môi Trường Hoàn Chỉnh** chứa đầy đủ 34+ biến hệ thống, URL, Port, Secret Key, Device ID, User ID và Token fallback. | Dùng chung cho tất cả các Collection. |

---

## 🔑 Thông Tin Tài Khoản Mặc Định Đã Đồng Bộ Sẵn

| Username | Password | Vai trò (Role) | Trạng thái | Quyền hạn |
| :--- | :--- | :--- | :--- | :--- |
| **`fintech_user01`** | **`Password123@`** | `ROLE_CUSTOMER`, `ROLE_VIEWER` | `ACTIVE` | Giao dịch ví, Heo đất IoT, Smart OTP, eKYC, Nạp rút tiền |
| **`admin`** | **`Password123@`** | `ROLE_SUPER_ADMIN` | `ACTIVE` | Toàn quyền kiểm soát Sổ cái kép, Phê duyệt eKYC, Worker, Payment |

> [!NOTE]
> Mã thiết bị `fp_postman_client_01` đã được nạp sẵn vào danh sách **Trusted Device**, do đó khi gửi lệnh Login, hệ thống sẽ **cấp Token ngay lập tức** mà không bị chặn bởi bước xác thực thiết bị mới.

---

## ⚡ Các Cải Tiến Đặc Biệt Giúp Chạy 1-Click "Zero-Error"

1. **Tự động bắt và gán Token 2 chiều:**
   - Khi chạy API Login (dù là `1.1a Login Retail User`, `1.1b Login Admin` hay `Python Login`), Test Script tự động trích xuất `accessToken`, `refreshToken`, `userId` và gán đồng thời vào cả **Environment Variables** lẫn **Collection Variables** (`coreToken`, `pythonToken`, `token`, `coreUserId`, `pythonUserId`, `userId`).
2. **Sinh Chữ Ký Số M2M HMAC-SHA256 Thời Gian Thực:**
   - Các API hạch toán M2M Sổ cái kép (`5.4`, `5.5`, `5.6`, `5.7`) được tích hợp sẵn Pre-request Script sử dụng thư viện `CryptoJS.HmacSHA256` để tự động băm chữ ký số theo payload chuẩn hóa (`userId|type|amount|idempotencyKey|timestamp`) và secret key `liochio-fintech-m2m-secret-key-2026` với timestamp mới nhất, bảo đảm không bao giờ bị lỗi lệch quá 5 phút.
3. **Tách Biệt Nhóm Hủy Phiên (Teardown & Cleanup):**
   - Các API có tính chất hủy phiên hoặc xóa dữ liệu (`Revoke Device`, `Revoke Session`, `Logout`, `Logout All`, `Delete Account`) đã được di chuyển xuống thư mục cuối cùng `08. Đăng Xuất & Thu Hồi Phiên (Teardown & Cleanup - Chạy Cuối Cùng)` và gán ID an toàn (`999999`), giúp công cụ **Postman Collection Runner** khi chạy hàng loạt từ trên xuống dưới không bị gián đoạn hay làm mất quyền đăng nhập của các API phía sau.
4. **Tự Động Bắt và Đồng Bộ Device ID Heo Đất IoT:**
   - API `2.1 Lấy danh sách Heo Đất` và `2.3 Ghép đôi Heo Đất` tự động bắt ID thực tế trong database và gán vào biến `{{piggyDeviceId}}`, giúp các API thả tiền, rút tiền và dự báo AI phía sau hoạt động chuẩn xác 100%.

---

## 🛠️ Hướng Dẫn Chạy (Chỉ 2 Bước)

### Bước 1: Import vào Postman
1. Mở ứng dụng **Postman**.
2. Nhấn nút **Import** ở góc trên bên trái.
3. Kéo thả file Collection bạn muốn dùng (`Liochio_Microservices_API.postman_collection.json` hoặc 2 bộ lẻ) và file `Liochio_Local_Environment.postman_environment.json`.
4. Ở góc trên cùng bên phải màn hình Postman, tại ô chọn môi trường (Environment Dropdown), chọn: **`Liochio Local Environment (Retail FinTech & IoT)`**.

### Bước 2: Bấm Chạy Ngay
- **Cách 1 (Chạy từng Request):** Mở thư mục `01. Xác Thực & Vòng Đời Tài Khoản`, chọn `1.1a Đăng nhập FinTech User (Login Retail User)` và bấm **Send**. Sau khi đăng nhập thành công, bạn có thể bấm **Send** bất kỳ API nào trong toàn bộ hệ thống!
- **Cách 2 (Chạy tự động toàn bộ Collection Runner):** Chuột phải vào tên Collection -> chọn **Run collection** -> bấm **Run Liochio...**. Toàn bộ các API sẽ chạy tuần tự từ đầu đến cuối 100% xanh mướt (Pass)!

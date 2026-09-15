# 🧪 Hướng Dẫn Toàn Diện Kiểm Thử Hệ Thống (FinTech Testing Guide)

## 1. Mục Đích & Phạm Vi
Tài liệu này hướng dẫn các nhà phát triển và đội ngũ QA phương pháp kiểm thử toàn diện hệ thống FinTech Backend: từ công cụ kiểm thử (Swagger UI, cURL, Postman), quy trình lấy Token xác thực, cách theo dõi Trace-ID, cho đến việc thực thi các kịch bản kiểm thử bảo mật và chịu tải.

---

## 2. Chuẩn Bị Môi Trường Kiểm Thử

### 2.1. Khởi động hệ thống
1. **Khởi tạo cơ sở dữ liệu & nạp dữ liệu mẫu**:
   '''bash
   python setup_database.py
   '''
2. **Khởi động server Backend**:
   '''bash
   uvicorn app.main:app --host 127.0.0.1 --port 8000 --reload
   '''
3. **Khởi động Worker gửi thông báo (Terminal riêng)**:
   '''bash
   python run_worker.py
   '''

### 2.2. Tài khoản mẫu có sẵn trong hệ thống:
| Tài khoản | Email | Mật khẩu | Vai trò | Mục đích test |
| :--- | :--- | :--- | :--- | :--- |
| 'superadmin' | 'admin@fintech.local' | 'Admin@123456' | 'SUPER_ADMIN' | Kiểm thử toàn bộ API và phân hệ quản trị |
| 'testuser' | 'user@fintech.local' | 'User@123456' | 'USER' | Kiểm thử nghiệp vụ ví, nạp tiền, phân quyền |

---

## 3. Các Công Cụ Kiểm Thử & Cách Sử Dụng

### 3.1. Kiểm thử trực quan qua Swagger UI ('/docs')
1. Mở trình duyệt truy cập: [http://localhost:8000/docs](http://localhost:8000/docs).
2. Thực hiện gọi API 'POST /api/v1/auth/login' với tài khoản 'testuser' để lấy 'access_token'.
3. Bấm vào nút **Authorize 🔒** (góc trên bên phải), dán token vào ô 'Value' (không cần gõ thêm chữ Bearer), bấm **Authorize**.
4. Toàn bộ các API yêu cầu xác thực ('/wallets', '/wallet_topup', '/logout') sẽ tự động được đính kèm token trong Header.

### 3.2. Kiểm thử qua cURL
Ví dụ lấy danh sách ví:
'''bash
curl -X GET "http://localhost:8000/api/v1/wallets"   -H "Authorization: Bearer <YOUR_ACCESS_TOKEN>"   -H "Accept-Language: vi"
'''

---

## 4. Các Quy Chuẩn Kiểm Thử Bắt Buộc

1. **Kiểm tra Header Trace-ID**:
   - Mọi response trả về từ Server đều phải có Header 'X-Trace-Id'.
   - Nếu client truyền 'X-Correlation-ID: custom-id', response phải phản chiếu đúng ID này.
2. **Kiểm tra Đa ngôn ngữ (i18n)**:
   - Truyền Header 'Accept-Language: vi' -> Thông báo trả về Tiếng Việt.
   - Truyền Header 'Accept-Language: en' -> Thông báo trả về Tiếng Anh.
   - Truyền Header 'Accept-Language: zh' -> Thông báo trả về Tiếng Trung.
3. **Kiểm tra Phân quyền HTTP Status Code**:
   - **401 Unauthorized**: Khi không truyền Token, Token hết hạn, hoặc Token sai chữ ký.
   - **403 Forbidden**: Khi Token hợp lệ nhưng tài khoản không có quyền ('PermissionGuard' / 'RoleBasedGuard').
   - **422 / 400 Bad Request**: Khi dữ liệu đầu vào vi phạm định dạng validation.
4. **Kiểm tra Tính Bất Biến Idempotency**:
   - Gọi API nạp tiền '/wallet_topup' nhiều lần với cùng một 'Idempotency-Key' -> Số dư chỉ được cộng đúng 1 lần.
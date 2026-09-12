# 📋 Toàn Bộ Danh Sách Testcase Chi Tiết Cho Từng API (API Test Cases Matrix)

## 📌 Bảng Ký Hiệu & Phân Loại Testcase
- **HP (Happy Path)**: Luồng dữ liệu chuẩn, kỳ vọng thành công 200/201.
- **NE (Negative / Error Path)**: Luồng dữ liệu sai lệch, kỳ vọng trả về 400/401/403/404/422.
- **SEC (Security & Guard)**: Kiểm thử tấn công XSS, SQLi, IDOR, brute-force, leo thang đặc quyền.
- **IDEM (Idempotency)**: Kiểm thử phòng chống lặp giao dịch tài chính.

---

## 1. PHÂN HỆ XÁC THỰC (AUTHENTICATION API)

### 1.1. `POST /api/v1/auth/register` (Đăng ký tài khoản)
| Mã TC | Phân loại | Tên Testcase & Dữ liệu đầu vào | Kết quả kỳ vọng | Mã lỗi |
| :--- | :--- | :--- | :--- | :--- |
| `AUTH-REG-01` | **HP** | Đăng ký tài khoản với đầy đủ thông tin hợp lệ | Status `201 Created`, user_id mới, trạng thái `PENDING` | `USER_REGISTER_SUCCESS` |
| `AUTH-REG-02` | **NE** | Thiếu trường `username` / `email` / `password` | Status `400 Bad Request`, báo lỗi thiếu trường | `MISSING_USERNAME` |
| `AUTH-REG-03` | **NE** | Email sai định dạng (ví dụ: `invalid-email`) | Status `400 Bad Request` | `INVALID_EMAIL_FORMAT` |
| `AUTH-REG-04` | **NE** | Số điện thoại < 10 số hoặc chứa chữ cái | Status `400 Bad Request` | `INVALID_PHONE_FORMAT` |
| `AUTH-REG-05` | **NE** | Mật khẩu yếu (thiếu chữ hoa, số hoặc ký tự đặc biệt) | Status `400 Bad Request` | `WEAK_PASSWORD` |
| `AUTH-REG-06` | **NE** | Mật khẩu xác nhận không khớp `password != confirm_password` | Status `400 Bad Request` | `PASSWORD_MISMATCH` |
| `AUTH-REG-07` | **NE** | Tuổi người dùng < 18 tuổi dựa trên `date_of_birth` | Status `400 Bad Request` | `USER_UNDER_AGE` |
| `AUTH-REG-08` | **NE** | Đăng ký với Email đã tồn tại trong CSDL | Status `400 Bad Request` | `EMAIL_ALREADY_EXISTS` |
| `AUTH-REG-09` | **NE** | Đăng ký với Username đã tồn tại trong CSDL | Status `400 Bad Request` | `USERNAME_ALREADY_EXISTS` |
| `AUTH-REG-10` | **SEC** | Username chứa từ khóa bị cấm: `admin_fintech` | Status `400 Bad Request` | `FORBIDDEN_USERNAME` |
| `AUTH-REG-11` | **SEC** | `full_name` chứa mã XSS: `<script>alert(1)</script>` | Status `400 Bad Request` | `XSS_DETECTED_IN_NAME` |

---

### 1.2. `POST /api/v1/auth/login` (Đăng nhập)
| Mã TC | Phân loại | Tên Testcase & Dữ liệu đầu vào | Kết quả kỳ vọng | Mã lỗi |
| :--- | :--- | :--- | :--- | :--- |
| `AUTH-LOG-01` | **HP** | Đăng nhập với email và password chính xác của tài khoản `ACTIVE` | Status `200 OK`, trả về `access_token`, `refresh_token`, `expires_in` | `USER_LOGIN_SUCCESS` |
| `AUTH-LOG-02` | **NE** | Đăng nhập với mật khẩu không đúng | Status `401 Unauthorized` | `INVALID_CREDENTIALS` |
| `AUTH-LOG-03` | **NE** | Đăng nhập với Email chưa từng đăng ký | Status `401 Unauthorized` | `USER_NOT_FOUND` |
| `AUTH-LOG-04` | **NE** | Đăng nhập tài khoản đang ở trạng thái `PENDING` (chưa kích hoạt) | Status `403 Forbidden` | `USER_NOT_ACTIVE` |
| `AUTH-LOG-05` | **NE** | Đăng nhập tài khoản bị khóa `BLOCKED` | Status `403 Forbidden` | `USER_ACCOUNT_BLOCKED` |

---

### 1.3. `GET /api/v1/auth/activate` (Kích hoạt Link Email)
| Mã TC | Phân loại | Tên Testcase & Dữ liệu đầu vào | Kết quả kỳ vọng | Mã lỗi |
| :--- | :--- | :--- | :--- | :--- |
| `AUTH-ACT-01` | **HP** | Truy cập link kích hoạt với `token` hợp lệ còn hạn | Status `200 OK`, User chuyển `OTP_PENDING`, **KHÔNG LỘ OTP** | `AUTH_LINK_VERIFIED_SUCCESS` |
| `AUTH-ACT-02` | **NE** | Truy cập với token không tồn tại trong hệ thống | Status `400 Bad Request` | `INVALID_OR_USED_LINK_TOKEN` |
| `AUTH-ACT-03` | **NE** | Truy cập với token đã quá 15 phút (hết hạn) | Status `400 Bad Request` | `LINK_TOKEN_EXPIRED` |
| `AUTH-ACT-04` | **NE** | Truy cập với token đã được kích hoạt trước đó (trạng thái `USED`) | Status `400 Bad Request` | `INVALID_OR_USED_LINK_TOKEN` |

---

### 1.4. `POST /api/v1/auth/verify-otp` (Xác thực OTP)
| Mã TC | Phân loại | Tên Testcase & Dữ liệu đầu vào | Kết quả kỳ vọng | Mã lỗi |
| :--- | :--- | :--- | :--- | :--- |
| `AUTH-OTP-01` | **HP** | Nhập đúng mã OTP 6 số còn hiệu lực | Status `200 OK`, User chuyển sang trạng thái `ACTIVE` | `AUTH_OTP_VERIFIED_SUCCESS` |
| `AUTH-OTP-02` | **NE** | Nhập sai mã OTP | Status `400 Bad Request` | `INVALID_OTP_CODE` |
| `AUTH-OTP-03` | **SEC** | Nhập sai OTP liên tiếp 3 lần | Status `403 Forbidden`, OTP bị vô hiệu hóa `LOCKED` | `OTP_BRUTE_FORCE_LOCKED` |
| `AUTH-OTP-04` | **NE** | Nhập OTP sau khi đã quá 5 phút | Status `400 Bad Request` | `OTP_TIMEOUT_EXPIRED` |

---

### 1.5. `POST /api/v1/auth/refresh-token` (Xoay vòng Refresh Token)
| Mã TC | Phân loại | Tên Testcase & Dữ liệu đầu vào | Kết quả kỳ vọng | Mã lỗi |
| :--- | :--- | :--- | :--- | :--- |
| `AUTH-REF-01` | **HP** | Gửi `refresh_token` hợp lệ chưa bị thu hồi | Status `200 OK`, cấp cặp token mới **giữ nguyên đầy đủ quyền hạn** | `TOKEN_REFRESH_SUCCESS` |
| `AUTH-REF-02` | **NE** | Gửi refresh token đã hết hạn | Status `401 Unauthorized` | `AUTH_TOKEN_EXPIRED` |
| `AUTH-REF-03` | **SEC** | **Replay Attack**: Gửi lại refresh token cũ đã từng được rotate | Status `401 Unauthorized`, toàn bộ phiên của User bị khóa | `AUTH_SESSION_REVOKED` |

---

### 1.6. `POST /api/v1/auth/logout` (Đăng xuất)
| Mã TC | Phân loại | Tên Testcase & Dữ liệu đầu vào | Kết quả kỳ vọng | Mã lỗi |
| :--- | :--- | :--- | :--- | :--- |
| `AUTH-OUT-01` | **HP** | Gửi Header `Authorization: Bearer <valid_token>` | Status `200 OK`, phiên bị thu hồi trong CSDL (`is_revoked = 1`) | `AUTH_LOGOUT_SUCCESS` |
| `AUTH-OUT-02` | **NE** | Không gửi Authorization Header | Status `401 Unauthorized` | `AUTH_UNAUTHORIZED` |

---

## 2. PHÂN HỆ QUẢN LÝ VÍ (WALLET MANAGEMENT API)

### 2.1. `POST /api/v1/wallets` (Tạo ví mới)
| Mã TC | Phân loại | Tên Testcase & Dữ liệu đầu vào | Kết quả kỳ vọng | Mã lỗi |
| :--- | :--- | :--- | :--- | :--- |
| `WAL-CRE-01` | **HP** | Tạo ví với `wallet_code`, `name`, `currency: VND`, `wallet_type: CASH` | Status `201 Created`, số dư khởi tạo `0.0`, trạng thái `ACTIVE` | `WALLET_CREATE_SUCCESS` |
| `WAL-CRE-02` | **NE** | Không gửi Bearer Token | Status `401 Unauthorized` | `AUTH_UNAUTHORIZED` |
| `WAL-CRE-03` | **NE** | Tạo ví với `wallet_code` đã tồn tại của người dùng | Status `400 Bad Request` | `WALLET_ALREADY_EXISTS` |
| `WAL-CRE-04` | **NE** | Loại tiền tệ không hợp lệ (`currency: XYZ`) | Status `400 Bad Request` | `INVALID_CURRENCY_ENUM` |

---

### 2.2. `GET /api/v1/wallets` & `GET /api/v1/wallets/{wallet_id}` (Truy vấn ví)
| Mã TC | Phân loại | Tên Testcase & Dữ liệu đầu vào | Kết quả kỳ vọng | Mã lỗi |
| :--- | :--- | :--- | :--- | :--- |
| `WAL-LIS-01` | **HP** | Lấy danh sách ví của tài khoản đang đăng nhập | Status `200 OK`, danh sách các ví `is_deleted = 0` | `WALLET_FETCH_SUCCESS` |
| `WAL-DET-01` | **HP** | Xem chi tiết ví thuộc quyền sở hữu của mình | Status `200 OK`, thông tin chi tiết ví và số dư | `WALLET_FETCH_SUCCESS` |
| `WAL-DET-02` | **SEC** | **IDOR Attack**: User A cố tình truyền `wallet_id` của User B | Status `403 Forbidden` hoặc `404 Not Found` | `WALLET_FORBIDDEN_ACCESS` |
| `WAL-DET-03` | **NE** | Truy vấn `wallet_id` không tồn tại trong CSDL | Status `404 Not Found` | `WALLET_NOT_FOUND` |

---

### 2.3. `PUT /api/v1/wallets/{wallet_id}` (Cập nhật ví)
| Mã TC | Phân loại | Tên Testcase & Dữ liệu đầu vào | Kết quả kỳ vọng | Mã lỗi |
| :--- | :--- | :--- | :--- | :--- |
| `WAL-UPD-01` | **HP** | Cập nhật tên, màu sắc và icon của ví | Status `200 OK`, thông tin ví được cập nhật | `WALLET_UPDATE_SUCCESS` |
| `WAL-UPD-02` | **SEC** | Cố tình cập nhật ví của người khác | Status `403 Forbidden` | `WALLET_FORBIDDEN_ACCESS` |

---

### 2.4. `DELETE /api/v1/wallets/{wallet_id}` (Xóa mềm ví)
| Mã TC | Phân loại | Tên Testcase & Dữ liệu đầu vào | Kết quả kỳ vọng | Mã lỗi |
| :--- | :--- | :--- | :--- | :--- |
| `WAL-DEL-01` | **HP** | Xóa ví hợp lệ của tài khoản | Status `200 OK`, ví chuyển sang `is_deleted = 1` | `WALLET_DELETE_SUCCESS` |
| `WAL-DEL-02` | **NE** | Xóa ví đã bị xóa trước đó | Status `404 Not Found` | `WALLET_NOT_FOUND` |

---

## 3. PHÂN HỆ NẠP TIỀN & GIAO DỊCH (TRANSACTIONS & TOP-UP)

### 3.1. `POST /api/v1/wallet_topup` (Nạp tiền vào ví)
| Mã TC | Phân loại | Tên Testcase & Dữ liệu đầu vào | Kết quả kỳ vọng | Mã lỗi |
| :--- | :--- | :--- | :--- | :--- |
| `TOP-EXE-01` | **HP** | Nạp 500,000 VND vào ví `CASH_VND_001` kèm Header `Idempotency-Key` | Status `200 OK`, số dư tăng đúng 500,000, trạng thái `SUCCESS` | `WALLET_TOPUP_SUCCESS` |
| `TOP-EXE-02` | **IDEM** | **Double Spending**: Gửi lại request nạp tiền cùng `Idempotency-Key` | Status `200 OK`, trả về kết quả cũ, **số dư không bị cộng dồn lần 2** | `WALLET_TOPUP_SUCCESS` |
| `TOP-EXE-03` | **NE** | Nạp số tiền <= 0 (ví dụ: `amount: -100000`) | Status `400 Bad Request` | `INVALID_AMOUNT_POSITIVE` |
| `TOP-EXE-04` | **NE** | Nạp tiền vào ví đã bị xóa hoặc bị khóa (`is_deleted = 1`) | Status `400 Bad Request` | `WALLET_DELETED` |
| `TOP-EXE-05` | **SEC** | User không có quyền `WALLET_TOPUP` | Status `403 Forbidden` | `AUTH_PERMISSION_DENIED` |
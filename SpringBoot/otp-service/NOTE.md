# DEDICATED OTP & SMARTOTP MICROSERVICE (`otp-service`)

---

## 1. TỔNG QUAN & KIẾN TRÚC

Phân hệ **OTP & SmartOTP** được tách biệt hoàn toàn thành một **Microservice độc lập** (`otp-service`) với cơ sở dữ liệu riêng biệt `portfolio_otp`, chạy trên cổng mặc định **`8094`**.

### 1.1. Các Trách Nhiệm Chính
1. **Quản lý Sinh & Xác Thực Mã OTP 6 Số**:
   - Thuật toán sinh mã ngẫu nhiên bảo mật cao (`SecureRandom`).
   - Băm BCrypt 12 rounds lưu trong database `portfolio_otp`.
   - Giới hạn thời gian sống (TTL 300 giây = 5 phút) và giới hạn thử sai (tối đa 3 lần).
2. **Quản lý SmartOTP TOTP Chuẩn RFC 6238**:
   - Sinh Base32 Secret 160-bit và đường dẫn quét mã QR Barcode `otpauth://totp/...`.
   - Xác thực mã TOTP 6 số luân phiên theo thời gian 30s và bảo vệ bằng mã PIN bảo mật.
3. **Cơ Chế Bật/Tắt Service Bỏ Qua Xác Thực Cho Môi Trường Dev (`is_enabled` / `bypass_in_dev`)**:
   - Quản lý qua bảng `otp_service_configs` trong DB `portfolio_otp`.
   - Khi `is_enabled = FALSE` (hoặc `bypass_in_dev = TRUE` trong môi trường DEV), hệ thống sẽ **tự động chấp thuận (Bypass verify) 100%** hoặc sử dụng mã bypass mặc định `123456`.

---

## 2. BẢNG CƠ SỞ DỮ LIỆU RIÊNG (`portfolio_otp`)

```sql
-- 1. Bảng cấu hình bật tắt service
CREATE TABLE `otp_service_configs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    `is_enabled` BOOLEAN NOT NULL DEFAULT TRUE,
    `bypass_in_dev` BOOLEAN NOT NULL DEFAULT TRUE,
    `dev_bypass_code` VARCHAR(20) NOT NULL DEFAULT '123456',
    `environment` VARCHAR(20) NOT NULL DEFAULT 'DEV',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_otp_config_tenant` (`tenant_id`)
);

-- 2. Bảng lưu trữ OTP & SmartOTP
CREATE TABLE `user_otp_verifications` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    `user_id` BIGINT NOT NULL,
    `otp_type` VARCHAR(30) NOT NULL DEFAULT 'EMAIL',
    `otp_purpose` VARCHAR(50) NOT NULL,
    `otp_code_hash` VARCHAR(255) NULL,
    `smart_otp_secret` VARCHAR(255) NULL,
    `smart_otp_pin_hash` VARCHAR(255) NULL,
    `target_destination` VARCHAR(150) NULL,
    `attempt_count` INT NOT NULL DEFAULT 0,
    `max_attempts` INT NOT NULL DEFAULT 3,
    `status` VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    `reference_id` VARCHAR(64) NULL,
    `expires_at` TIMESTAMP NOT NULL,
    `verified_at` TIMESTAMP NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_otp_lookup` (`tenant_id`, `user_id`, `otp_purpose`, `status`),
    INDEX `idx_otp_expiry` (`expires_at`)
);
```

---

## 3. DANH SÁCH ENDPOINTS & API ĐỊNH TUYẾN

| HTTP Method | Endpoint | Mô Tả Nghiệp Vụ | Cờ Bypass Dev |
| :--- | :--- | :--- | :---: |
| `GET` | `/api/v1/otp/config` | Xem cấu hình và trạng thái Bật/Tắt Service | ✅ |
| `PUT` | `/api/v1/otp/config` | Bật/Tắt OTP Service hoặc chuyển đổi Dev Bypass Code | ✅ |
| `POST` | `/api/v1/otp/generate` | Sinh mã OTP 6 số (gửi SMS / Email) | ✅ |
| `POST` | `/api/v1/otp/verify` | Xác thực mã OTP 6 số | ✅ |
| `POST` | `/api/v1/otp/smart-otp/setup` | Khởi tạo TOTP Secret & QR Barcode URI | ✅ |
| `POST` | `/api/v1/otp/smart-otp/verify`| Xác thực & Kích hoạt SmartOTP TOTP | ✅ |

---

## 4. HƯỚNG DẪN BẬT / TẮT SERVICE TRONG DATABASE CHO DEV

### Cách 1: Cập nhật trực tiếp bằng câu lệnh SQL trong DB `portfolio_otp`
```sql
-- Tắt kiểm tra OTP toàn bộ cho môi trường DEV (Bỏ qua xác thực OTP):
UPDATE portfolio_otp.otp_service_configs 
SET is_enabled = FALSE 
WHERE tenant_id = 'SYSTEM' OR tenant_id = 'default';

-- Bật lại kiểm tra OTP nghiêm ngặt:
UPDATE portfolio_otp.otp_service_configs 
SET is_enabled = TRUE, bypass_in_dev = FALSE, environment = 'PROD' 
WHERE tenant_id = 'SYSTEM' OR tenant_id = 'default';
```

### Cách 2: Gọi API Quản Trị Cấu Hình
```http
PUT http://localhost:8094/api/v1/otp/config?tenantId=default
Content-Type: application/json

{
  "isEnabled": false,
  "bypassInDev": true,
  "devBypassCode": "123456"
}
```

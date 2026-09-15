# 🏦 CORE BANKING LEDGER SERVICE (:8085)

## 1. Mục Đích & Sứ Mệnh
Dịch vụ Sổ cái kế toán kép bất biến (Double-Entry General Ledger Engine) của toàn bộ hệ sinh thái Liochio FinTech.

## 2. Nguyên Tắc Bất Biến
1. **Tổng DEBIT = Tổng CREDIT**: Mọi giao dịch chuyển tiền đều có ít nhất 2 dòng chi tiết đối ứng, không có tiền tự sinh ra hoặc mất đi.
2. **Khóa Hàng Bi Quan (Pessimistic Lock)**: Truy vấn 'SELECT ... FOR UPDATE' bảo đảm tính tuần tự và loại bỏ Race Condition khi cập nhật số dư.
3. **Mã Băm Chuỗi Bất Biến (SHA-256 Hash Chaining)**: Mỗi bút toán mới tính mã hash dựa trên 'prev_hash' của bút toán liền trước.
4. **Idempotency**: Ngăn chặn trùng lặp giao dịch tài chính thông qua khóa 'idempotency_key' bắt buộc.
5. **Database-Per-Service**: Sở hữu cơ sở dữ liệu độc lập 'liochio_ledger_db'.

## 3. Cổng Giao Tiếp M2M
- Endpoint: 'POST /api/v1/ledger/m2m/transaction'
- Xác thực: Header 'X-M2M-Signature' (HMAC-SHA256) và 'X-M2M-Timestamp'.

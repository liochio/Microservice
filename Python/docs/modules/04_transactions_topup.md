# 💳 Phân Hệ 04: Quản Lý Giao Dịch & Nạp Tiền (Transactions & Top-Up)

## 1. Tổng Quan & Mục Tiêu
Phân hệ **Transactions & Top-Up** xử lý các luồng tiền nạp vào ví (Deposit/Top-up), cập nhật số dư nguyên tử (Atomic Update) và bảo vệ giao dịch khỏi trùng lặp (Double Spending).

### Trạng thái triển khai:
- ✅ **Đã hoàn thiện**: Nạp tiền vào ví, Khóa lạc quan / Biến động số dư nguyên tử, Header `Idempotency-Key` chống nạp tiền 2 lần khi mạng chập chờn, Ghi nhận Transaction Log chi tiết.
- ⏳ **Kế hoạch tương lai (Roadmap)**: Cổng thanh toán thực tế VNPay / MoMo Callback Webhook, Rút tiền về ngân hàng (Withdrawal), Quét mã QR thanh toán tĩnh/động.

---

## 2. Cơ Chế Chống Trùng Lặp Giao Dịch (Idempotency Key)
- Client truyền Header `Idempotency-Key: <UUID>` trong request nạp tiền.
- Hệ thống kiểm tra khóa này trong CSDL / Redis Cache.
- Nếu request bị retry nhiều lần do timeout mạng: Trả về kết quả của giao dịch ban đầu, **tuyệt đối không cộng dồn số dư lần thứ hai**.

---

## 3. Danh Sách API Endpoints

| Method | Endpoint | Quyền hạn yêu cầu | Mô tả |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/wallet_topup` | `WALLET_TOPUP` | Nạp tiền vào ví tài khoản |
| `GET` | `/api/v1/transactions` | `TRANSACTION_LIST` | Xem danh sách lịch sử giao dịch *(Roadmap)* |
| `GET` | `/api/v1/transactions/{id}` | `TRANSACTION_VIEW` | Xem chi tiết biên lai giao dịch *(Roadmap)* |

---

## 4. Cấu Trúc Bảng Dữ Liệu
- `transactions`: Lưu vết toàn bộ giao dịch tài chính (`transaction_code`, `wallet_id`, `amount`, `balance_before`, `balance_after`, `status`, `idempotency_key`).
- `wallet_history`: Bản ghi biến động số dư tương ứng.
# 🔄 Phân Hệ 05: Chuyển Tiền Nội Bộ & P2P (Transfers & P2P)

## 1. Tổng Quan & Mục Tiêu
Phân hệ **Transfers & P2P** đảm bảo luồng chuyển dịch tài chính an toàn giữa các ví của cùng một người dùng (Internal Transfer) hoặc giữa 2 người dùng khác nhau (P2P Transfer).

### Trạng thái triển khai:
- ⏳ **Đang hoàn thiện**: Mô hình dữ liệu bảng 'transfers', 'wallet_transfers', Dịch vụ chuyển tiền nội bộ 2 pha (Two-phase Commit).
- ⏳ **Kế hoạch tương lai (Roadmap)**: Chuyển tiền qua mã QR P2P, Chuyển tiền nhóm / Chia hóa đơn (Bill Splitting), Tích hợp thông báo real-time qua WebSocket khi nhận tiền.

---

## 2. Quy Trình Chuyển Tiền 2 Pha An Toàn (Two-Phase Lock)
1. **Kiểm tra điều kiện**:
   - Ví nguồn 'source_wallet' và Ví đích 'dest_wallet' phải ở trạng thái 'ACTIVE'.
   - Số dư khả dụng của ví nguồn >= số tiền chuyển + phí giao dịch.
2. **Khóa bản ghi (Row-Level Locking)**:
   - Dùng 'SELECT ... FOR UPDATE' theo thứ tự 'wallet_id' tăng dần để tránh Deadlock.
3. **Thực thi nguyên tử trong 1 Transaction**:
   - Trừ tiền ví nguồn: 'balance = balance - amount'.
   - Cộng tiền ví đích: 'balance = balance + amount'.
   - Ghi 2 bản ghi 'wallet_history' (1 Debit, 1 Credit).
   - Ghi 1 bản ghi 'transfers'.
   - Đẩy sự kiện ghi sổ cái kế toán (Ledger Event).
4. **Rollback tự động**: Nếu xảy ra bất kỳ ngoại lệ nào, toàn bộ giao dịch được hoàn tác 100%.

---

## 3. Cấu Trúc Bảng Dữ Liệu
- 'transfers': Quản lý lệnh chuyển tiền ('transfer_code', 'source_wallet_id', 'dest_wallet_id', 'amount', 'fee', 'status').
- 'wallet_transfers': Bảng liên kết chi tiết chuyển tiền ví.
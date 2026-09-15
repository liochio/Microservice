# 📖 Phân Hệ 07: Sổ Cái Kế Toán Kép & Đối Soát (General Ledger)

## 1. Tổng Quan & Mục Tiêu
Phân hệ **General Ledger** đóng vai trò là "Trái tim kiểm toán tài chính" của hệ thống FinTech. Ứng dụng nguyên lý **Kế toán kép (Double-Entry Bookkeeping)** để đảm bảo tính toàn vẹn và bất biến của toàn bộ dòng tiền.

### Trạng thái triển khai:
- ⏳ **Đang hoàn thiện**: Mô hình bảng 'ledger_accounts', 'journal_entries', 'journal_entry_details', 'accounting_periods', 'reconciliation_logs'.
- ⏳ **Kế hoạch tương lai (Roadmap)**: Báo cáo cân đối phát sinh tự động (Trial Balance), Tiến trình đối soát số dư ban đêm (Nightly Reconciliation Cronjob).

---

## 2. Nguyên Tắc Bất Biến Của Sổ Cái Kế Toán Kép
1. **Cân bằng Nợ - Có**:
   $$\sum \text{Debit (Nợ)} = \sum \text{Credit (Có)}$$
   Mỗi giao dịch tài chính phát sinh đều phải tạo ra ít nhất 2 dòng bút toán ('journal_entry_details'): Một dòng bên Nợ và một dòng bên Có với tổng số tiền bằng nhau tuyệt đối.
2. **Tính Bất Biến (Immutability)**:
   - Các bút toán đã ghi sổ ('journal_entries') **không bao giờ được phép sửa ('UPDATE') hoặc xóa ('DELETE')**.
   - Nếu có sai sót: Phải tạo **Bút toán điều chỉnh (Reversing Entry)** để triệt tiêu sai lệch.

---

## 3. Danh Mục Tài Khoản Kế Toán Chuẩn
- '1000 - Tiền mặt & Số dư ví người dùng' (Tài sản - Asset)
- '2000 - Tiền gửi ký quỹ tại Ngân hàng liên kết' (Tài sản - Asset)
- '3000 - Phải trả người dùng / Nợ tiền gửi' (Nợ phải trả - Liability)
- '4000 - Vốn chủ sở hữu & Quỹ dự phòng' (Vốn - Equity)
- '5000 - Doanh thu phí giao dịch & Dịch vụ' (Doanh thu - Revenue)

---

## 4. Cấu Trúc Bảng Dữ Liệu
- 'ledger_accounts': Hệ thống tài khoản kế toán.
- 'journal_entries': Bút toán kế toán tổng hợp.
- 'journal_entry_details': Chi tiết ghi Nợ/Có từng tài khoản.
- 'accounting_periods': Quản lý kỳ kế toán (mở/đóng kỳ).
- 'reconciliation_logs': Nhật ký đối soát số dư thực tế với sổ cái.
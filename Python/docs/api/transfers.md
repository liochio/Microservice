# 🔄 API HƯỚNG DẪN: PHÂN HỆ CHUYỂN TIỀN NỘI BỘ (TRANSFERS)

## 📌 Mục đích & Nghiệp vụ
Thực hiện điều chuyển tiền giữa 2 ví thuộc sở hữu của người dùng (ví dụ: chuyển từ Ví Tiền Mặt sang Ví Tiết Kiệm hoặc sang Ví MoMo).

### ⚡ Đảm bảo ACID Transaction 100%:
1. Khóa và kiểm tra số dư ví nguồn (`balance >= amount`).
2. Trừ tiền ví nguồn.
3. Cộng tiền ví đích.
4. Ghi nhận bản ghi `transfers`.
5. Tất cả thực hiện trong **1 Transaction SQL duy nhất**.

---

## 1. Thực Hiện Chuyển Tiền
- **Endpoint**: `POST /api/v1/transfers`
- **Payload**:
```json
{
  "source_wallet_id": "wallet-source-uuid",
  "destination_wallet_id": "wallet-dest-uuid",
  "amount": 500000.0,
  "description": "Trích tiền tiết kiệm đầu tháng"
}
```

---

## 2. Xem Lịch Sử Chuyển Tiền
- **Endpoint**: `GET /api/v1/transfers`

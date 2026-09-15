# 🧾 API HƯỚNG DẪN: PHÂN HỆ QUÉT HÓA ĐƠN TỰ ĐỘNG OCR (OCR SCANNER)

## 📌 Mục đích & Nghiệp vụ
Nhận diện văn bản trên ảnh hóa đơn/bill bán hàng siêu thị, nhà hàng. Tự động bóc tách:
- Tên đơn vị bán lẻ (Merchant Name)
- Ngày giờ hóa đơn (Invoice Date)
- Tổng tiền thanh toán (Total Amount)
- Gợi ý danh mục chi tiêu phù hợp
- Chi tiết từng mặt hàng và đơn giá

---

## 1. Quét Ảnh Hóa Đơn
- **Endpoint**: 'POST /api/v1/ocr/scan'
- **Payload**:
'''json
{
  "image_base64": "data:image/jpeg;base64,...",
  "image_url": "https://storage.fintech.local/invoices/inv_01.jpg"
}
'''

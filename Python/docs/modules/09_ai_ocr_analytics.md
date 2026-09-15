# 🤖 Phân Hệ 09: Trí Tuệ Nhân Tạo & OCR Hóa Đơn (AI & OCR Analytics)

## 1. Tổng Quan & Mục Tiêu
Phân hệ **AI & OCR Analytics** cung cấp các tính năng thông minh giúp người dùng tự động hóa quản lý tài chính: Quét hóa đơn qua ảnh chụp (OCR), Chấm điểm sức khỏe tài chính (Financial Health Score), Dự báo chi tiêu tháng tới và Gợi ý tiết kiệm thông minh.

### Trạng thái triển khai:
- ⏳ **Đang hoàn thiện**: Mô hình bảng 'ocr_results', 'ocr_extracted_items', 'ai_financial_scores', 'ai_predictions', 'ai_recommendations', 'ai_model_logs'.
- ⏳ **Kế hoạch tương lai (Roadmap)**: Tích hợp mô hình OCR (Tesseract / EasyOCR / Google Vision), Mô hình Machine Learning dự báo chuỗi thời gian (ARIMA / LSTM) phân tích thói quen tiêu dùng.

---

## 2. Các Tính Năng AI/ML Dự Kiến
1. **Quét Hóa Đơn Tự Động (Receipt OCR)**:
   - Người dùng chụp ảnh hóa đơn mua sắm siêu thị/nhà hàng.
   - OCR tự động trích xuất: Tên cửa hàng, Ngày giờ, Danh sách mặt hàng, Tổng tiền thanh toán.
   - Tự động phân loại danh mục (Ăn uống, Gia dụng,...) và tạo giao dịch chi tiêu tương ứng.
2. **Chấm Điểm Sức Khỏe Tài Chính (Financial Score)**:
   - Đánh giá trên thang điểm 0 - 100 dựa trên: Tỷ lệ tiết kiệm, Mức độ tuân thủ ngân sách, Tần suất chi tiêu bất thường.
3. **Dự Báo & Cảnh Báo Thông Minh (Smart Recommendations)**:
   - Dự đoán số tiền sẽ chi tiêu trong 30 ngày tới.
   - Gợi ý cắt giảm các khoản chi không thiết yếu (Subscription không dùng, ăn ngoài quá nhiều).

---

## 3. Cấu Trúc Bảng Dữ Liệu
- 'ocr_results': Kết quả trích xuất hóa đơn tổng thể.
- 'ocr_extracted_items': Từng dòng chi tiết món hàng trên hóa đơn.
- 'ai_financial_scores': Điểm số tài chính định kỳ của người dùng.
- 'ai_predictions': Kết quả dự báo chi tiêu tương lai.
- 'ai_recommendations': Các lời khuyên tài chính cá nhân hóa.
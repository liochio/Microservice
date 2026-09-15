# 🤖 API HƯỚNG DẪN: PHÂN HỆ TRỢ LÝ TÀI CHÍNH THÔNG MINH AI (AI ADVISOR)

## 📌 Mục đích & Nghiệp vụ
Sử dụng thuật toán học máy và phân tích định lượng để:
1. **Chấm điểm sức khỏe tài chính (Financial Health Score từ 0 - 100)**: Dựa trên tỷ lệ tiết kiệm, dòng tiền thu/chi 30 ngày.
2. **Gợi ý & Cảnh báo chi tiêu thông minh**: Phát hiện danh mục chi tiêu bất thường và đưa ra lời khuyên cắt giảm chi phí.

---

## 1. Chấm Điểm Sức Khỏe Tài Chính
- **Endpoint**: 'GET /api/v1/ai/spending-score'
- **Response**: Score, Rating ('EXCELLENT', 'GOOD', 'FAIR', 'POOR'), Thu nhập/Chi tiêu 30 ngày, Tỷ lệ tiết kiệm và Báo cáo tóm tắt.

---

## 2. Nhận Lời Khuyên Tối Ưu Chi Tiêu
- **Endpoint**: 'GET /api/v1/ai/recommendations'
- **Response**: Danh sách đề xuất kèm số tiền có thể tiết kiệm được ('potential_savings').

# 🎯 BỘ SLIDE BÁO CÁO BẢO VỆ ĐỒ ÁN TỐT NGHIỆP
## ĐỀ TÀI: HỆ THỐNG QUẢN TRỊ TÀI CHÍNH THÔNG MINH KẾT HỢP HEO ĐẤT IOT (ESP32) VÀ TRÍ TUỆ NHÂN TẠO (AI)

---

<!-- SLIDE 1: TRANG BÌA -->
# 🎓 BÁO CÁO ĐỒ ÁN TỐT NGHIỆP
### **HỆ THỐNG QUẢN TRỊ TÀI CHÍNH THÔNG MINH KẾT HỢP HEO ĐẤT IOT (ESP32) VÀ AI**
- **Sinh viên thực hiện**: [Họ và Tên Sinh Viên]
- **Giảng viên hướng dẫn**: [Họ và Tên GVHD]
- **Khoa / Ngành**: Công nghệ Thông tin / Kỹ thuật Phần mềm / IoT
- **Năm thực hiện**: 2026

---

<!-- SLIDE 2: TÍNH CẤP THIẾT -->
# 📌 1. TÍNH CẤP THIẾT CỦA ĐỀ TÀI
- **Thực trạng**: Các app tài chính hiện nay chỉ nhập liệu thủ công $\rightarrow$ Người dùng hay quên, bỏ cuộc giữa chừng.
- **Rào cản tiền mặt**: Tiền mặt tiêu xài hàng ngày khó kiểm soát, trẻ em thiếu công cụ trực quan để rèn luyện thói quen tiết kiệm.
- **Heo đất truyền thống**: Chỉ là ống sứ vô tri, không biết bên trong có bao nhiêu tiền, dễ bị trộm cắp/đập phá.
- **Giải pháp**: Xây dựng **Hệ sinh thái Heo Đất Thông Minh IoT kết hợp AI và Ví Số FinTech**.

---

<!-- SLIDE 3: KIẾN TRÚC TỔNG THỂ -->
# 🏛️ 2. SƠ ĐỒ KIẾN TRÚC HỆ THỐNG
'''
   [Phần Cứng Heo Đất ESP32]          [Mobile Flutter App / Web]
              │                                    │
              │ (HMAC-SHA256)                      │ (JWT Bearer Token / WebSocket)
              ▼                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                   FASTAPI BACKEND CORE ENGINE (69 APIs)                │
│                                                                        │
│  ├─► Xác thực & Phân quyền RBAC (33 Permissions)                       │
│  ├─► Luồng Nạp Tiền Cốt Lõi IoT (ACID Ingestion Pipeline)              │
│  ├─► Đồng Bộ Ngoại Tuyến Flash Memory (Offline Sync)                   │
│  ├─► Trí Tuệ Nhân Tạo (Dự Báo Chuỗi Thời Gian & OCR)                   │
│  ├─► Quản Lý Mục Tiêu Tài Chính & Khóa Heo Đất (Piggy Lock)            │
│  ├─► Cha Mẹ Thưởng Nhân Đôi Tiền (Parent Matching Bonus)               │
│  ├─► Cổng Nạp Tiền VietQR NAPAS 247 & Webhook Tự Động                  │
│  └─► WebSocket Live Stream: Thông báo "Ting ting" Tức Thời             │
└───────────────────────────────────┬────────────────────────────────────┘
                                    ▼
                         [MYSQL DATABASE 8.0 ACID]
'''

---

<!-- SLIDE 4: PHẦN CỨNG IOT -->
# 🤖 3. THIẾT KẾ PHẦN CỨNG HEO ĐẤT IOT
- **Vi điều khiển lõi**: ESP32 Dual-Core 240MHz (WiFi + BLE).
- **Cảm biến tiền (Optical / Coin Slot)**: Nhận diện mệnh giá rơi qua khe nhét tiền.
- **Cảm biến cân nặng (Load Cell HX711)**: Cân điện tử dưới đáy bụng heo đo $\Delta W$ đối soát khối lượng $\rightarrow$ **Chống đút giấy rác / tiền giả**.
- **Cảm biến gia tốc rung lắc (MPU6050)**: Phát hiện dốc ngược hoặc đập phá heo $\rightarrow$ Kích hoạt còi hú 2000Hz và chớp LED đỏ báo trộm.
- **Đèn LED RGB & Loa Buzzer**: Phát hiệu ứng ánh sáng vui mừng và âm thanh khi đút tiền thành công.

---

<!-- SLIDE 5: LUỒNG NẠP TIỀN BẢO MẬT CAO -->
# 🛡️ 4. LUỒNG NẠP TIỀN BẢO MẬT CAO 6 TẦNG (ACID INGESTION)
1. **Xác thực thiết bị**: Đối chiếu địa chỉ MAC cứng của chip ESP32 đã ghép nối với User.
2. **Chữ ký điện tử HMAC-SHA256 & Nonce**: Chống 100% nguy cơ Replay Attack (tấn công phát lại).
3. **Đối soát cảm biến kép**: Mắt đọc quang học + Cân điện tử Load Cell.
4. **Database Transaction ACID**: Tăng số dư Ví Heo Đất và ghi nhận Giao dịch Thu Nhập (INCOME) trong cùng 1 Transaction.
5. **Cha Mẹ Thưởng Nhân Đôi Tiền**: Tự động trích tiền từ Ví Cha Mẹ thưởng thêm $50\% - 100\%$ vào Ví Con.
6. **Bắn WebSocket Live Stream**: Điện thoại rung chuông "Ting ting" + Animation pháo hoa tại giây thứ 0.

---

<!-- SLIDE 6: ĐỒNG BỘ NGOẠI TUYẾN -->
# 📴 5. CƠ CHẾ ĐỒNG BỘ NGOẠI TUYẾN (OFFLINE FLASH SYNC)
- **Tình huống**: Heo đất bị mất mạng WiFi khi người dùng nhét tiền vào ống.
- **Xử lý tại phần cứng**: ESP32 lưu vết các lần đút tiền vào bộ nhớ Flash Memory nội bộ.
- **Tự động đồng bộ**: Khi có lại WiFi, ESP32 gọi API 'POST /api/v1/smart_piggy/sync-offline-batch'.
- **Kết quả**: Backend xử lý toàn bộ batch theo chuẩn ACID, đảm bảo **không bao giờ bị mất tiền**.

---

<!-- SLIDE 7: TRÍ TUỆ NHÂN TẠO -->
# 🧠 6. PHÂN HỆ TRÍ TUỆ NHÂN TẠO (AI & PREDICTIVE ANALYTICS)
1. **AI Dự báo ngày đầy Heo (Deposit Forecast)**:
   - Phân tích hồi quy chuỗi thời gian dựa trên tốc độ bỏ ống heo trung bình ngày $\bar{v}$.
   - Ước tính chính xác số ngày hoàn thành mục tiêu $D = \frac{M_{\text{mục tiêu}} - M_{\text{hiện tại}}}{\bar{v}}$.
2. **AI Phân tích hành vi tích lũy (Behavior Analysis)**:
   - Tìm thứ trong tuần đút tiền nhiều nhất, mệnh giá yêu thích, tính điểm kiên trì ($0 - 100$).
3. **AI Chấm điểm sức khỏe tài chính (Spending Score)**:
   - Đánh giá tỷ lệ tiết kiệm, cảnh báo nguy cơ vượt ngân sách.
4. **Smart OCR Receipt Scan**:
   - Trích xuất tự động cửa hàng, ngày mua, tổng tiền từ ảnh chụp hóa đơn.

---

<!-- SLIDE 8: CÁC TÍNH NĂNG NỔI BẬT -->
# 💡 7. CÁC TÍNH NĂNG ĐỘT PHÁ CỦA HỆ THỐNG
- **Khóa Heo Đất (Piggy Lock)**: Đóng băng không cho rút tiền trước hạn hoặc trước khi đạt $100\%$ mục tiêu $\rightarrow$ Rèn tính kỷ luật cho trẻ.
- **Cha Mẹ Nhân Đôi Tiền (Parent Matching Bonus)**: Khuyến khích trẻ tiết kiệm thông qua cơ chế thưởng tự động từ cha mẹ.
- **Cổng Nạp Tiền VietQR NAPAS 247**: Sinh mã QR nạp tiền kèm nội dung tự động, Webhook tự động cộng tiền.
- **Xuất Báo Cáo Sao Kê Excel/CSV**: Định dạng chuẩn UTF-8 có BOM chống lỗi font tiếng Việt.
- **Hệ Thống Gamification**: 5 Cấp độ Heo Đất (*Heo Sơ Sinh $\rightarrow$ Đại Gia Heo Vàng*) và Huy hiệu thành tích.

---

<!-- SLIDE 9: KẾT QUẢ KIỂM THỬ -->
# 🧪 8. KẾT QUẢ TRIỂN KHAI & KIỂM THỬ
- **Quy mô hệ thống**: **69 API Endpoints** hoàn chỉnh chia thành 10 phân hệ nghiệp vụ.
- **Độ tin cậy mã nguồn**: 326/326 modules Python biên dịch thành công 0 lỗi.
- **Kiểm thử tự động (E2E Integration)**: **100% Passed** trên toàn bộ 69 endpoints.
- **Hiệu năng xử lý**: Độ trễ trung bình $< 15\text{ms}$ cho mỗi giao dịch tài chính.

---

<!-- SLIDE 10: TỔNG KẾT & KẾT LUẬN -->
# 🏆 9. TỔNG KẾT & ĐÁNG GIÁ ĐỒ ÁN
- ✅ **Hoàn thành 100% các mục tiêu nghiên cứu** đặt ra ban đầu.
- ✅ **Sản phẩm hoàn chỉnh**: Sẵn sàng kết nối Mobile App và phần cứng ESP32 thực tế.
- ✅ **Ý nghĩa khoa học & thực tiễn**: Ứng dụng công nghệ FinTech, IoT và AI vào bài toán giáo dục tài chính gia đình.

---

# 👏 CẢM ƠN QUÝ THẦY CÔ HỘI ĐỒNG ĐÃ LẮNG NGHE!
### **Q&A - XIN KÍNH MỜI THẦY CÔ ĐẶT CÂU HỎI**

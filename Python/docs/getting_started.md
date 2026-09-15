# 🚀 HƯỚNG DẪN CÀI ĐẶT & CHẠY HỆ THỐNG (GETTING STARTED GUIDE)

## 📌 1. Nguyên Nhân Lỗi ''uvicorn' is not recognized'
Lỗi xảy ra do lệnh 'uvicorn' được cài đặt bên trong môi trường ảo Python ('venv'), nhưng cửa sổ dòng lệnh (Terminal/CMD/PowerShell) của bạn chưa được kích hoạt môi trường 'venv'.

---

## ⚡ 2. Ba (03) Cách Khởi Chạy Server Nhanh Nhất

### 🔹 Cách 1: Chạy bằng 1 cú Click chuột (Khuyên dùng trên Windows)
- Bấm đúp chuột (Double click) vào file **'run_server.bat'** tại thư mục gốc của dự án.
- Máy chủ sẽ tự động khởi động trên 'http://127.0.0.1:8000'.

---

### 🔹 Cách 2: Chạy trực tiếp qua Python của 'venv' (Không cần kích hoạt môi trường)
Mở Terminal tại thư mục 'd:\Github\finance-graduation-project' và gõ:

'''cmd
.\venv\Scripts\python.exe -m uvicorn app.main:app --reload --host 127.0.0.1 --port 8000
'''

---

### 🔹 Cách 3: Kích hoạt 'venv' trước khi gõ 'uvicorn'
- **Nếu dùng Command Prompt (CMD)**:
  '''cmd
  venv\Scripts\activate
  uvicorn app.main:app --reload --port 8000
  '''
- **Nếu dùng PowerShell**:
  '''powershell
  .\venv\Scripts\Activate.ps1
  uvicorn app.main:app --reload --port 8000
  '''

---

## 🌐 3. Truy Cập Swagger UI & Tài Liệu API
Sau khi server khởi động:
- **Tài liệu Swagger UI tương tác**: [http://127.0.0.1:8000/docs](http://127.0.0.1:8000/docs)
- **Tài liệu ReDoc**: [http://127.0.0.1:8000/redoc](http://127.0.0.1:8000/redoc)

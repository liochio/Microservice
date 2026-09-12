@echo off
chcp 65001 > nul
echo ======================================================================
echo 🚀 KHỞI ĐỘNG MÁY CHỦ FINTECH CORE MONOLITH (FASTAPI 2.0)
echo ======================================================================
echo.
if not exist "venv\Scripts\python.exe" (
    echo ❌ Không tìm thấy môi trường ảo venv! Vui lòng tạo venv trước.
    pause
    exit /b 1
)

echo 🌐 Đang khởi chạy Uvicorn trên http://127.0.0.1:8000 ...
echo 📖 Swagger UI: http://127.0.0.1:8000/docs
echo.
.\venv\Scripts\python.exe -m uvicorn app.main:app --reload --host 127.0.0.1 --port 8000
pause

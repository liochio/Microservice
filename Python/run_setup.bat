@echo off
chcp 65001 > nul
echo ======================================================================
echo ⚙️ KHỞI TẠO CSDL VÀ NẠP DỮ LIỆU MẪU (DATABASE SEEDER)
echo ======================================================================
echo.
set PYTHONUTF8=1
set PYTHONPATH=%~dp0
.\venv\Scripts\python.exe setup_database.py
pause

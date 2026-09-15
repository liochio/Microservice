
import sys
import os

# Tự động cấu hình chuẩn UTF-8 cho Windows Console chống lỗi UnicodeEncodeError
if sys.platform == "win32":
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    if hasattr(sys.stderr, "reconfigure"):
        sys.stderr.reconfigure(encoding="utf-8", errors="replace")

from contextlib import asynccontextmanager
from typing import AsyncGenerator
from pathlib import Path

from fastapi import FastAPI
from fastapi.responses import HTMLResponse
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles

from app.core.config.settings import settings
from app.api.router import api_router
from app.db.session import SessionLocal
from app.core.exceptions.handler import register_exception_handlers
from app.core.translator.translator_engine import i18n_translator
from app.core.translator.i18n_loader import auto_sync_i18n_json_to_db
from app.core.listeners.audit_listener import register_audit_listeners
from app.core.middleware.middleware import (
    RequestContextAndLogMiddleware,
    RedisRateLimitMiddleware
)


@asynccontextmanager
async def lifespan(_app: FastAPI) -> AsyncGenerator[None, None]:
    """
    👑 QUẢN LÝ VÒNG ĐỜI ỨNG DỤNG (APPLICATION LIFESPAN):
    """
    print("🚀 [STARTUP] Khởi động hệ thống FinTech Monolith Core & Web App...")

    # 1. Tải từ điển i18n vào RAM
    i18n_translator.load_translations_to_memory()

    # 2. Đồng bộ i18n xuống DB lúc khởi động
    try:
        auto_sync_i18n_json_to_db()
        print("✅ [I18N] Đồng bộ từ điển đa ngôn ngữ hoàn tất.")
    except Exception as e:
        print(f"⚠️ [I18N_WARNING] Bỏ qua đồng bộ i18n: {str(e)}")

    # 3. Đăng ký Audit Listeners
    try:
        register_audit_listeners(SessionLocal)
    except Exception as e:
        print(f"⚠️ [AUDIT_LISTENER_WARNING] Không thể đăng ký audit listeners: {str(e)}")

    print("🎉 [READY] Hệ thống đã sẵn sàng nhận kết nối mạng!")
    yield
    print("🛑 [SHUTDOWN] Đóng kết nối hệ thống an toàn.")


# Khởi tạo ứng dụng FastAPI chính
app = FastAPI(
    title=settings.PROJECT_NAME,
    version="2.5.0",
    docs_url="/docs",
    redoc_url="/redoc",
    openapi_url="/openapi.json",
    lifespan=lifespan
)

# 👑 HỆ THỐNG 3 MIDDLEWARE TINH GỌN (0 DATABASE OVERHEAD)
app.add_middleware(RequestContextAndLogMiddleware)
app.add_middleware(RedisRateLimitMiddleware)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Đăng ký bộ bẫy lỗi tập trung toàn cục
register_exception_handlers(app)

@app.get("/health")
def health_check():
    return {"status": "healthy", "app_name": settings.PROJECT_NAME, "version": "2.5.0"}

# ==============================================================================
# 👑 FRONTEND WEB APP ROUTES (SERVES COMPLETE SINGLE PAGE APP)
# ==============================================================================
STATIC_DIR = Path(__file__).resolve().parent.parent / "static"

@app.get("/", response_class=HTMLResponse)
@app.get("/app", response_class=HTMLResponse)
def serve_full_frontend_app():
    """Trang chủ ứng dụng Full Web App tích hợp 7 phân hệ nghiệp vụ FinTech & Smart Piggy IoT."""
    index_file = STATIC_DIR / "index.html"
    if index_file.exists():
        return HTMLResponse(content=index_file.read_text(encoding="utf-8"))
    return HTMLResponse(content="<h1>Liochio Full Frontend Web App</h1>", status_code=200)

@app.get("/demo", response_class=HTMLResponse)
def serve_simulator_dashboard():
    """Trang giao diện Web & IoT Piggy Simulator trực quan phục vụ hội đồng chấm đồ án."""
    sim_file = STATIC_DIR / "simulator.html"
    if sim_file.exists():
        return HTMLResponse(content=sim_file.read_text(encoding="utf-8"))
    return HTMLResponse(content="<h1>Simulator HTML not found</h1>", status_code=404)

# Mount thư mục static assets nếu có
if STATIC_DIR.exists():
    app.mount("/static", StaticFiles(directory=str(STATIC_DIR)), name="static")

# Nạp toàn bộ các Route API v1
app.include_router(api_router, prefix="/api/v1")
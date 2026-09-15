# setup_project.py
import os

PROJECT_ROOT = os.path.dirname(os.path.abspath(__file__))

# 1. Danh sách toàn bộ thư mục cần khởi tạo theo sơ đồ kiến trúc hệ thống
STRUCTURE = [
    "app",
    "app/api",
    "app/api/dependencies",
    "app/api/internal",
    "app/api/internal/health",
    "app/api/internal/metrics",
    "app/api/internal/scheduler",
    "app/api/internal/kafka",
    "app/api/websocket",
    "app/api/v1",
    "app/api/v1/auth",
    "app/api/v1/users",
    "app/api/v1/wallets",
    "app/api/v1/transactions",
    "app/api/v1/transfers",
    "app/api/v1/budgets",
    "app/api/v1/ledger",
    "app/api/v1/reports",
    "app/api/v1/analytics",
    "app/api/v1/notifications",
    "app/api/v1/payment",
    "app/api/v1/ai",
    "app/api/v1/ocr",
    "app/api/v1/iot",
    "app/api/v1/smart_piggy",
    "app/core",
    "app/core/config",
    "app/core/middleware",
    "app/core/security",
    "app/core/security/jwt",
    "app/core/security/oauth2",
    "app/core/security/hashing",
    "app/core/security/encryption",
    "app/core/security/permissions",
    "app/core/security/rate_limit",
    "app/core/security/csrf",
    "app/core/security/api_key",
    "app/core/translator",
    "app/core/responses",
    "app/core/error_codes",
    "app/core/logging",
    "app/core/exceptions",
    "app/core/events",
    "app/db",
    "app/db/migrations",
    "app/db/seed",
    "app/db/factories",
    "app/db/fixtures",
    "app/models",
    "app/models/auth",
    "app/models/finance",
    "app/models/ledger",
    "app/models/payment",
    "app/models/ai",
    "app/models/ocr",
    "app/models/iot",
    "app/models/smart_piggy",
    "app/models/notification",
    "app/models/audit",
    "app/models/common",
    "app/repositories",
    "app/repositories/auth",
    "app/repositories/finance",
    "app/repositories/ledger",
    "app/repositories/payment",
    "app/repositories/ai",
    "app/repositories/ocr",
    "app/repositories/iot",
    "app/repositories/smart_piggy",
    "app/repositories/common",
    "app/services",
    "app/services/auth",
    "app/services/finance",
    "app/services/ledger",
    "app/services/payment",
    "app/services/ai",
    "app/services/ocr",
    "app/services/iot",
    "app/services/smart_piggy",
    "app/services/notification",
    "app/services/report",
    "app/schemas",
    "app/schemas/requests",
    "app/schemas/responses",
    "app/schemas/websocket",
    "app/schemas/events",
    "app/kafka",
    "app/kafka/producers",
    "app/kafka/consumers",
    "app/kafka/topics",
    "app/kafka/serializers",
    "app/kafka/handlers",
    "app/rabbitmq",
    "app/rabbitmq/queues",
    "app/rabbitmq/workers",
    "app/rabbitmq/retry",
    "app/rabbitmq/dead_letter",
    "app/mqtt",
    "app/mqtt/consumers",
    "app/mqtt/producers",
    "app/mqtt/topics",
    "app/mqtt/payloads",
    "app/mqtt/smart_piggy",
    "app/websocket",
    "app/websocket/manager",
    "app/websocket/channels",
    "app/websocket/events",
    "app/websocket/authentication",
    "consumers",
    "producers",
    "tasks",
    "tasks/email",
    "tasks/notification",
    "tasks/ai",
    "tasks/payment",
    "tasks/report",
    "schedulers",
    "schedulers/cron_jobs",
    "schedulers/monthly_jobs",
    "schedulers/yearly_jobs",
    "schedulers/recurring_transactions",
    "integrations",
    "integrations/momo",
    "integrations/vnpay",
    "integrations/firebase",
    "integrations/aws",
    "integrations/cloudinary",
    "integrations/elasticsearch",
    "integrations/redis",
    "integrations/openai",
    "integrations/banking",
    "static",
    "static/uploads",
    "static/uploads/file",
    "static/uploads/img",
    "static/uploads/icon",
    "static/uploads/temp",
    "static/uploads/receipt",
    "static/uploads/avatar",
    "static/uploads/ai",
    "static/uploads/export",
    "static/uploads/piggy_snapshots",
    "static/uploads/piggy_firmware",
    "static/templates",
    "static/templates/email",
    "static/templates/reports",
    "static/templates/pdf",
    "static/public",
    "logs",
    "i18n",
    "i18n/vi",
    "i18n/en",
    "i18n/zh",
    "utils",
    "tests",
    "tests/unit",
    "tests/integration",
    "tests/performance",
    "tests/security",
    "tests/e2e",
    "scripts",
    "scripts/deployment",
    "scripts/backup",
    "scripts/migration",
    "scripts/seed",
    "docker",
    "docker/nginx",
    "docker/mysql",
    "docker/redis",
    "docker/kafka",
    "docker/rabbitmq",
    "docker/mqtt",
    "docs",
    "docs/architecture",
    "docs/api",
    "docs/database",
    "docs/deployment",
    "docs/security"
]

# 2. Định nghĩa nội dung bảo mật cao cho các file nòng cốt ở thư mục gốc app/
FILES_WITH_CONTENT = {
    "app/main.py": """from fastapi import FastAPI
from app.lifespan import app_lifespan
from app.core.middleware.logging_middleware import LoggingMiddleware
from app.api.router import api_router

app = FastAPI(
    title="AI Finance Management & Smart Piggy Bank Platform",
    version="2.0.0",
    lifespan=app_lifespan
)

# Kích hoạt chuỗi ma trận Middleware cốt lõi
app.add_middleware(LoggingMiddleware)

# Đóng gói cây định tuyến tổng thể
app.include_router(api_router)
""",

    "app/lifespan.py": """from contextlib import asynccontextmanager
from fastapi import FastAPI

@asynccontextmanager
async def app_lifespan(app: FastAPI):
    # STARTUP: Khởi động toàn bộ kết nối tài nguyên lớn
    # Connect MySQL pool, Redis cluster, Kafka broker, MQTT client, Preload AI model
    yield
    # SHUTDOWN: Thu hồi và ngắt sạch tài nguyên an toàn
""",

    "app/dependency.py": """from sqlalchemy.orm import Session
from fastapi import Depends, Request
from app.db.session import SessionLocal

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
""",

    "app/db/base.py": """from sqlalchemy.orm import DeclarativeBase

class Base(DeclarativeBase):
    pass
""",

    "app/db/session.py": """from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from app.core.config.settings import settings

engine = create_engine(settings.DATABASE_URL, pool_pre_ping=True, pool_size=20, max_overflow=10)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
""",

    "app/core/config/settings.py": """import os
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    DATABASE_URL: str = os.getenv("DATABASE_URL", "mysql+pymysql://root:root@127.0.0.1:3306/personal_finance")
    REDIS_URL: str = os.getenv("REDIS_URL", "redis://127.0.0.1:6329/0")
    SECRET_KEY: str = os.getenv("SECRET_KEY", "SUPER_SECRET_SECURITY_KEY_ENTERPRISE_2026")

settings = Settings()
""",

    "app/core/middleware/logging_middleware import LoggingMiddleware": """# Dự phòng mở rộng middleware context sau""",
    "app/core/responses/response_handler.py": """# Dự phòng mở rộng bộ phản hồi đa ngữ sau""",

    # Khởi tạo ma trận i18n trống để tránh crash hệ thống dịch ngôn ngữ
    "i18n/vi/messages.json": "{}",
    "i18n/vi/errors.json": "{}",
    "i18n/vi/labels.json": "{}",
    "i18n/en/messages.json": "{}",
    "i18n/en/errors.json": "{}",
    "i18n/en/labels.json": "{}",
    "i18n/zh/messages.json": "{}",
    "i18n/zh/errors.json": "{}",
    "i18n/zh/labels.json": "{}",

    # File cấu hình môi trường chuẩn hóa DevOps
    ".env": "DATABASE_URL=mysql+pymysql://root:root@127.0.0.1:3306/personal_finance\nREDIS_URL=redis://127.0.0.1:6379/0",
    "requirements.txt": "fastapi>=0.110.0\nuvicorn>=0.28.0\nsqlalchemy>=2.0.25\npymysql>=1.1.0\npydantic-settings>=2.2.1\npython-jose>=3.3.0\npasslib>=1.7.4",
    "alembic.ini": "# Config alembic",
    "docker-compose.yml": "version: '3.8'\nservices:\n  web:\n    build: .\n    ports:\n      - '8000:8000'",
    "Dockerfile": "FROM python:3.11\nWORKDIR /code\nCOPY ./requirements.txt /code/requirements.txt\nRUN pip install --no-cache-dir --upgrade -r /code/requirements.txt\nCOPY ./app /code/app\nCMD ['uvicorn', 'app.main:app', '--host', '0.0.0.0', '--port', '8000']"
}


def build_architecture():
    # Bước 1: Tạo cây thư mục trống
    for folder in STRUCTURE:
        path = os.path.join(PROJECT_ROOT, folder.replace("/", os.sep))
        os.makedirs(path, exist_ok=True)
        # Tạo thêm file __init__.py cho mọi package con trong app/
        if folder.startswith("app"):
            init_file = os.path.join(path, "__init__.py")
            if not os.path.exists(init_file):
                with open(init_file, "w", encoding="utf-8") as f:
                    pass

    # Bước 2: Bắn code nòng cốt vào các file tương ứng
    for rel_path, content in FILES_WITH_CONTENT.items():
        file_path = os.path.join(PROJECT_ROOT, rel_path.replace("/", os.sep))
        # Đảm bảo thư mục cha chứa file đó có tồn tại
        os.makedirs(os.path.dirname(file_path), exist_ok=True)
        with open(file_path, "w", encoding="utf-8") as f:
            f.write(content.strip())

    print("👑 [SUCCESS] Đã tạo xong toàn bộ ma trận kiến trúc Enterprise bảo mật cao!")


if __name__ == "__main__":
    build_architecture()
import os
from pathlib import Path
from pydantic_settings import BaseSettings, SettingsConfigDict
from pydantic import model_validator

BASE_DIR = Path(__file__).resolve().parent.parent.parent.parent


class Settings(BaseSettings):
    """
    👑 CẤU HÌNH TOÀN CỤC HỆ THỐNG (RESOURCE SERVER SETTINGS)
    Mục đích: Nạp các biến môi trường từ file .env, quản lý cấu hình bảo mật, kết nối DB, Redis, Mail.
    """
    # ==============================================================================
    # 1. THÔNG TIN ỨNG DỤNG
    # ==============================================================================
    APP_ENV: str = "development"
    PROJECT_NAME: str = "Liochio FinTech Resource Server"
    APP_URL: str = "http://localhost:8000"

    # ==============================================================================
    # 2. BẢO MẬT & MẬT MÃ JWT RS256 & JWKS
    # ==============================================================================
    SECRET_KEY: str = "09d25e094faa6ca2556c818166b7a9563b93f7099f6f0f4caa6cf63b88e8d3e7"
    ALGORITHM: str = "RS256"
    AUTH_CORE_JWKS_URL: str = "http://127.0.0.1:8081/.well-known/jwks.json"
    PUBLIC_KEY_PEM: str = ""
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 30
    REFRESH_TOKEN_EXPIRE_DAYS: int = 7
    JWT_REFRESH_SECRET_KEY: str = ""

    # ==============================================================================
    # 3. ĐA NGÔN NGỮ (i18n)
    # ==============================================================================
    DEFAULT_LANGUAGE: str = "vi"
    ALLOWED_LANGUAGES: str = "vi,en,zh"

    # ==============================================================================
    # 4. KẾT NỐI DATABASE & REDIS CACHE
    # ==============================================================================
    DATABASE_URL: str = "mysql+pymysql://root:12345678@localhost:3306/liochio_app_db"
    REDIS_URL: str = "redis://127.0.0.1:6379/0"

    # ==============================================================================
    # 5. LƯU TRỮ TỆP TIN
    # ==============================================================================
    STORAGE_TYPE: str = "local"
    UPLOAD_DIR: str = str(BASE_DIR / "uploads")

    # ==============================================================================
    # 6. GỬI MAIL THÔNG BÁO (SMTP)
    # ==============================================================================
    SMTP_HOST: str = "smtp.gmail.com"
    SMTP_PORT: int = 587
    SMTP_USER: str = ""
    SMTP_PASSWORD: str = ""

    # ==============================================================================
    # 7. THIRD-PARTY PAYMENT GATEWAYS
    # ==============================================================================
    MOMO_PARTNER_CODE: str = "MOMO_TEST_PARTNER"
    MOMO_ACCESS_KEY: str = "MOMO_TEST_ACCESS_KEY"
    MOMO_SECRET_KEY: str = "MOMO_TEST_SECRET_KEY"
    MOMO_API_ENDPOINT: str = "https://test-payment.momo.vn/v2/gateway/api/create"

    VNPAY_TMN_CODE: str = "VNPAY_TEST_CODE"
    VNPAY_HASH_SECRET: str = "VNPAY_TEST_SECRET"
    VNPAY_URL: str = "https://sandbox.vnpay.vn/paymentv2/vpcpay.html"

    # ==============================================================================
    # 8. AI & INTELLIGENT CORE
    # ==============================================================================
    GEMINI_API_KEY: str = ""
    AI_MODEL_NAME: str = "gemini-1.5-flash"

    # ==============================================================================
    # 9. ASYNC BROKERS & IOT
    # ==============================================================================
    KAFKA_BOOTSTRAP_SERVERS: str = "127.0.0.1:9092"
    MQTT_BROKER_HOST: str = "127.0.0.1"
    MQTT_BROKER_PORT: int = 1883
    MQTT_CLIENT_ID: str = "finance_backend_core"
    MQTT_USER: str = ""
    MQTT_PASSWORD: str = ""

    @model_validator(mode="after")
    def validate_and_setup(self):
        if not self.JWT_REFRESH_SECRET_KEY:
            self.JWT_REFRESH_SECRET_KEY = self.SECRET_KEY

        os.makedirs(self.UPLOAD_DIR, exist_ok=True)
        return self

    model_config = SettingsConfigDict(
        env_file=str(BASE_DIR / ".env"),
        env_file_encoding="utf-8",
        extra="ignore"
    )


settings = Settings()
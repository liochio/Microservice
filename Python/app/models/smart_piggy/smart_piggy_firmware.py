from sqlalchemy import Column, String, Text, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class SmartPiggyFirmware(Base):
    __tablename__ = "smart_piggy_firmwares"
    __table_args__ = {"comment": "Quản lý các bản build binary Firmware nâng cấp OTA"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    version = Column(String(50), unique=True, nullable=False, index=True)
    firmware_file_path = Column(String(500), nullable=False)
    changelog = Column(Text, nullable=True)
    status = Column(String(50), server_default=text("'RELEASED'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
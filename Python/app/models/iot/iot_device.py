from sqlalchemy import Column, String, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class IotDevice(Base):
    __tablename__ = "iot_devices"
    __table_args__ = {"comment": "Bảng danh mục thiết bị hạ tầng IoT"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    mac_address = Column(String(100), unique=True, nullable=False, index=True)
    device_name = Column(String(150), nullable=False)
    device_type = Column(String(100), nullable=False)
    firmware_version = Column(String(50), nullable=True)
    status = Column(String(50), server_default=text("'OFFLINE'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
from sqlalchemy import Column, String, ForeignKey, JSON, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class SmartPiggySensor(Base):
    __tablename__ = "smart_piggy_sensors"
    __table_args__ = {"comment": "Dữ liệu cảm biến môi trường trên vỏ heo đất"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    smart_piggy_device_id = Column(String(36), ForeignKey("smart_piggy_devices.id", ondelete="CASCADE"), nullable=False, index=True)
    sensor_type = Column(String(100), nullable=False, index=True)
    reading_payload = Column(JSON, nullable=False)
    status = Column(String(50), server_default=text("'PROCESSED'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
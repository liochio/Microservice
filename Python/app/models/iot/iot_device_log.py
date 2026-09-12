from sqlalchemy import Column, String, ForeignKey, Text, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class IotDeviceLog(Base):
    __tablename__ = "iot_device_logs"
    __table_args__ = {"comment": "Nhật ký vận hành, báo lỗi kết nối phần cứng"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    iot_device_id = Column(String(36), ForeignKey("iot_devices.id", ondelete="CASCADE"), nullable=False, index=True)
    log_level = Column(String(20), nullable=False)
    message = Column(Text, nullable=False)
    status = Column(String(50), server_default=text("'RECORDED'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
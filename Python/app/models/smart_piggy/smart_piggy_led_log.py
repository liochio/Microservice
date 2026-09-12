from sqlalchemy import Column, String, ForeignKey, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class SmartPiggyLedLog(Base):
    __tablename__ = "smart_piggy_led_logs"
    __table_args__ = {"comment": "Nhật ký điều khiển hiệu ứng đèn LED báo trạng thái"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    smart_piggy_device_id = Column(String(36), ForeignKey("smart_piggy_devices.id", ondelete="CASCADE"), nullable=False, index=True)
    led_effect = Column(String(100), nullable=False)
    triggered_by = Column(String(255), nullable=False)
    status = Column(String(50), server_default=text("'EXECUTED'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
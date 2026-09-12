from sqlalchemy import Column, String, ForeignKey, Numeric, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class SmartPiggyCoinLog(Base):
    __tablename__ = "smart_piggy_coin_logs"
    __table_args__ = {"comment": "Nhật ký hành vi bỏ tiền xu vật lý"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    smart_piggy_device_id = Column(String(36), ForeignKey("smart_piggy_devices.id", ondelete="CASCADE"), nullable=False, index=True)
    coin_value = Column(Numeric(18, 4), nullable=False)
    status = Column(String(50), server_default=text("'SUCCESS'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
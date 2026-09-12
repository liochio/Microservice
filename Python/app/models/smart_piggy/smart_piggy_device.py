from sqlalchemy import Column, String, ForeignKey, Numeric, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class SmartPiggyDevice(Base):
    __tablename__ = "smart_piggy_devices"
    __table_args__ = {"comment": "Bảng quản lý cốt lõi phần cứng Heo đất thông minh"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(64), nullable=False, index=True, comment="Logical FK liên kết với liochio-core")
    wallet_id = Column(String(36), ForeignKey("wallets.id", ondelete="RESTRICT"), nullable=False, index=True)
    mac_address = Column(String(100), unique=True, nullable=False, index=True)
    device_name = Column(String(150), nullable=False)
    total_coins_dropped = Column(Numeric(18, 4), server_default=text("0.0000"), nullable=False)
    status = Column(String(50), server_default=text("'ONLINE'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")

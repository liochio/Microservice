from sqlalchemy import Column, String, ForeignKey, Numeric, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class SmartPiggyGoal(Base):
    __tablename__ = "smart_piggy_goals"
    __table_args__ = {"comment": "Mục tiêu nuôi heo đất tích lũy dành cho trẻ em"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    smart_piggy_device_id = Column(String(36), ForeignKey("smart_piggy_devices.id", ondelete="CASCADE"), nullable=False, index=True)
    goal_name = Column(String(255), nullable=False)
    target_amount = Column(Numeric(18, 4), nullable=False)
    current_amount = Column(Numeric(18, 4), server_default=text("0.0000"), nullable=False)
    deadline = Column(DateTime, nullable=True)
    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
from sqlalchemy import Column, String, ForeignKey, Numeric, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class SmartPiggyReward(Base):
    __tablename__ = "smart_piggy_rewards"
    __table_args__ = {"comment": "Quản lý phần thưởng động viên khích lệ"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    smart_piggy_goal_id = Column(String(36), ForeignKey("smart_piggy_goals.id", ondelete="CASCADE"), nullable=False, index=True)
    reward_name = Column(String(255), nullable=False)
    points_cost = Column(Numeric(10, 2), nullable=False)
    status = Column(String(50), server_default=text("'AVAILABLE'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
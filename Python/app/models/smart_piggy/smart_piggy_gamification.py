from sqlalchemy import Column, String, ForeignKey, Integer, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class SmartPiggyGamification(Base):
    __tablename__ = "smart_piggy_gamifications"
    __table_args__ = {"comment": "Hệ thống tính điểm Level, Streak ngày nuôi heo"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(64), nullable=False, index=True, comment="Logical FK liên kết với liochio-core")
    current_points = Column(Integer, server_default=text("0"), nullable=False)
    current_level = Column(Integer, server_default=text("1"), nullable=False)
    streak_days = Column(Integer, server_default=text("0"), nullable=False)
    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")

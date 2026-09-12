from sqlalchemy import Column, String, ForeignKey, Text, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class AiRecommendation(Base):
    __tablename__ = "ai_recommendations"
    __table_args__ = {"comment": "Gợi ý tối ưu hóa tài chính tiết kiệm thông minh"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(64), nullable=False, index=True, comment="Logical FK liên kết với liochio-core")
    title = Column(String(255), nullable=False)
    recommendation_text = Column(Text, nullable=False)
    impact_level = Column(String(50), nullable=False)
    status = Column(String(50), server_default=text("'UNREAD'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")

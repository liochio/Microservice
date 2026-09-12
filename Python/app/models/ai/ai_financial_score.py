from sqlalchemy import Column, String, ForeignKey, Integer, Numeric, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class AiFinancialScore(Base):
    __tablename__ = "ai_financial_scores"
    __table_args__ = {"comment": "Bảng chấm điểm sức khỏe tài chính cá nhân"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(64), nullable=False, index=True, comment="Logical FK liên kết với liochio-core")
    score = Column(Integer, nullable=False)
    rating_tier = Column(String(50), nullable=False)
    debt_to_income_ratio = Column(Numeric(5, 4), nullable=False)
    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")

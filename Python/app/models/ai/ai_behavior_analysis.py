from sqlalchemy import Column, String, ForeignKey, Text, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class AiBehaviorAnalysis(Base):
    __tablename__ = "ai_behavior_analyses"
    __table_args__ = {"comment": "Phân tích hành vi chi tiêu rủi ro bất thường"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(64), nullable=False, index=True, comment="Logical FK liên kết với liochio-core")
    risk_level = Column(String(50), nullable=False)
    anomaly_details = Column(Text, nullable=False)
    recommended_action = Column(String(255), nullable=True)
    model_version = Column(String(50), nullable=False)
    status = Column(String(50), server_default=text("'PROCESSED'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")

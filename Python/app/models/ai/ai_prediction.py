from sqlalchemy import Column, String, ForeignKey, Numeric, DateTime, JSON, text
from sqlalchemy.sql import func
from app.db.base import Base

class AiPrediction(Base):
    __tablename__ = "ai_predictions"
    __table_args__ = {"comment": "Dự đoán xu hướng dòng tiền chi tiêu tương lai"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(64), nullable=False, index=True, comment="Logical FK liên kết với liochio-core")
    target_date = Column(DateTime, nullable=False)
    predicted_amount = Column(Numeric(18, 4), nullable=False)
    confidence_score = Column(Numeric(5, 4), nullable=False)
    features_used = Column(JSON, nullable=True)
    status = Column(String(50), server_default=text("'COMPLETED'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")

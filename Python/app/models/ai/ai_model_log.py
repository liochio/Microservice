from sqlalchemy import Column, String, Numeric, Integer, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class AiModelLog(Base):
    __tablename__ = "ai_model_logs"
    __table_args__ = {"comment": "Giám sát hiệu năng và thời gian huấn luyện mô hình AI"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    model_name = Column(String(100), nullable=False, index=True)
    accuracy = Column(Numeric(5, 4), nullable=True)
    loss = Column(Numeric(7, 4), nullable=True)
    training_duration_sec = Column(Integer, nullable=False)
    status = Column(String(50), server_default=text("'SUCCESS'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
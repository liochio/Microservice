from sqlalchemy import Column, String, JSON, Integer, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class ApiRequestLog(Base):
    __tablename__ = "api_request_logs"
    __table_args__ = {"comment": "Bảng lưu nhật ký thô toàn bộ Request Response HTTP"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(36), nullable=True, index=True)
    endpoint = Column(String(500), nullable=False)
    method = Column(String(10), nullable=False)
    request_payload = Column(JSON, nullable=True)
    response_payload = Column(JSON, nullable=True)
    status_code = Column(Integer, nullable=False)
    latency_ms = Column(Integer, nullable=False)
    status = Column(String(50), server_default=text("'SUCCESS'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
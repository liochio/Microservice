from sqlalchemy import Column, String, JSON, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class PaymentMethod(Base):
    __tablename__ = "payment_methods"
    __table_args__ = {"comment": "Cấu hình tích hợp cổng thanh toán trực tuyến"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    code = Column(String(50), unique=True, nullable=False, index=True)
    name = Column(String(100), nullable=False)
    config_payload = Column(JSON, nullable=True)
    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
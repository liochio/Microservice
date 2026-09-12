from sqlalchemy import Column, String, ForeignKey, JSON, Integer, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class PaymentWebhook(Base):
    __tablename__ = "payment_webhooks"
    __table_args__ = {"comment": "Nhật ký tiếp nhận tín hiệu Webhook từ cổng ngân hàng"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    payment_method_id = Column(String(36), ForeignKey("payment_methods.id", ondelete="RESTRICT"), nullable=False, index=True)
    payload = Column(JSON, nullable=False)
    ip_address = Column(String(100), nullable=True)
    http_status_code = Column(Integer, nullable=True)
    status = Column(String(50), server_default=text("'RECEIVED'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
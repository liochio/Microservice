from sqlalchemy import Column, String, Numeric, ForeignKey, Text, JSON, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class PaymentRefund(Base):
    __tablename__ = "payment_refunds"
    __table_args__ = {"comment": "Quản lý hoàn tiền giao dịch thanh toán trực tuyến"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    payment_transaction_id = Column(String(36), ForeignKey("payment_transactions.id", ondelete="RESTRICT"), nullable=False, index=True)
    refund_gateway_id = Column(String(255), unique=True, nullable=True, index=True)
    amount = Column(Numeric(18, 4), nullable=False)
    reason = Column(Text, nullable=False)
    gateway_response = Column(JSON, nullable=True)
    status = Column(String(50), server_default=text("'PENDING'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
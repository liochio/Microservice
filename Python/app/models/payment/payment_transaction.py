from sqlalchemy import Column, String, Numeric, ForeignKey, JSON, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class PaymentTransaction(Base):
    __tablename__ = "payment_transactions"
    __table_args__ = {"comment": "Giao dịch thanh toán cổng trực tuyến trực tiếp"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(64), nullable=False, index=True, comment="Logical FK liên kết với liochio-core")
    payment_method_id = Column(String(36), ForeignKey("payment_methods.id", ondelete="RESTRICT"), nullable=False, index=True)
    reference_order_id = Column(String(100), unique=True, nullable=False, index=True)
    gateway_transaction_id = Column(String(255), unique=True, nullable=True, index=True)
    amount = Column(Numeric(18, 4), nullable=False)
    currency = Column(String(10), server_default=text("'VND'"), nullable=False)
    gateway_response = Column(JSON, nullable=True)
    status = Column(String(50), server_default=text("'PENDING'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")

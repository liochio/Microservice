from sqlalchemy import Column, String, DateTime, Numeric, text
from sqlalchemy.sql import func
from app.db.base import Base

class PaymentReconciliation(Base):
    __tablename__ = "payment_reconciliations"
    __table_args__ = {"comment": "Bảng khớp lệnh đối soát chi tiết cổng thanh toán"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    reconciliation_date = Column(DateTime, nullable=False, index=True)
    payment_method_code = Column(String(50), nullable=False, index=True)
    total_gateway_transactions = Column(Numeric(18, 4), nullable=False)
    total_system_transactions = Column(Numeric(18, 4), nullable=False)
    status = Column(String(50), server_default=text("'SUCCESS'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
from sqlalchemy import Column, String, DateTime, Numeric, Text, text
from sqlalchemy.sql import func
from app.db.base import Base

class ReconciliationLog(Base):
    __tablename__ = "reconciliation_logs"
    __table_args__ = {"comment": "Nhật ký đối soát định kỳ hệ thống kế toán"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    reconciliation_date = Column(DateTime, nullable=False, index=True)
    source_system = Column(String(100), nullable=False)
    target_system = Column(String(100), nullable=False)
    total_source_amount = Column(Numeric(18, 4), nullable=False)
    total_target_amount = Column(Numeric(18, 4), nullable=False)
    difference_amount = Column(Numeric(18, 4), nullable=False)
    discrepancy_details = Column(Text, nullable=True)
    status = Column(String(50), server_default=text("'MATCHED'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
from sqlalchemy import Column, String, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class AccountingPeriod(Base):
    __tablename__ = "accounting_periods"
    __table_args__ = {"comment": "Kỳ kế toán hệ thống phục vụ chốt sổ đóng băng"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    period_name = Column(String(100), nullable=False)
    start_date = Column(DateTime, nullable=False)
    end_date = Column(DateTime, nullable=False)
    status = Column(String(50), server_default=text("'OPEN'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
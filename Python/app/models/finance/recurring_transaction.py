from sqlalchemy import Column, String, Numeric, DateTime, ForeignKey, Integer, text
from sqlalchemy.sql import func
from app.db.base import Base

class RecurringTransaction(Base):
    __tablename__ = "recurring_transactions"
    __table_args__ = {"comment": "Bảng thiết lập lịch thu chi định kỳ tự động"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(64), nullable=False, index=True, comment="Logical FK liên kết với liochio-core")
    wallet_id = Column(String(36), ForeignKey("wallets.id", ondelete="CASCADE"), nullable=False, index=True)
    category_id = Column(String(36), ForeignKey("categories.id", ondelete="CASCADE"), nullable=False, index=True)
    amount = Column(Numeric(18, 4), nullable=False)
    transaction_type = Column(String(20), nullable=False)
    frequency = Column(String(50), nullable=False)
    interval_days = Column(Integer, server_default=text("1"), nullable=False)
    next_run_date = Column(DateTime, nullable=False)
    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")

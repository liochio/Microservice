from sqlalchemy import Column, String, ForeignKey, Numeric, DateTime, text
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.db.base import Base

class JournalEntryDetail(Base):
    __tablename__ = "journal_entry_details"
    __table_args__ = {"comment": "Chi tiết dòng hạch toán kế toán"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    journal_entry_id = Column(String(36), ForeignKey("journal_entries.id", ondelete="CASCADE"), nullable=False, index=True)
    ledger_account_id = Column(String(36), ForeignKey("ledger_accounts.id", ondelete="RESTRICT"), nullable=False, index=True)
    entry_side = Column(String(10), nullable=False)
    amount = Column(Numeric(18, 4), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")

    journal_entry = relationship("JournalEntry", back_populates="details")
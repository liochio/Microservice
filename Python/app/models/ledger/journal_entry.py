from sqlalchemy import Column, String, DateTime, text
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.db.base import Base

class JournalEntry(Base):
    __tablename__ = "journal_entries"
    __table_args__ = {"comment": "Bảng bút toán tổng hợp đầu não cho toán bộ hệ thống kế toán"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    reference_id = Column(String(36), nullable=False, index=True)
    reference_type = Column(String(100), nullable=False)
    entry_date = Column(DateTime, nullable=False, index=True)
    description = Column(String(500), nullable=True)
    status = Column(String(50), server_default=text("'POSTED'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")

    details = relationship("JournalEntryDetail", back_populates="journal_entry", cascade="all, delete-orphan")
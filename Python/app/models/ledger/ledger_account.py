from sqlalchemy import Column, String, ForeignKey, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class LedgerAccount(Base):
    __tablename__ = "ledger_accounts"
    __table_args__ = {"comment": "Hệ thống tài khoản kế toán xương sống"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    account_code = Column(String(50), unique=True, nullable=False, index=True)
    account_name = Column(String(255), nullable=False)
    account_type = Column(String(50), nullable=False)
    parent_id = Column(String(36), ForeignKey("ledger_accounts.id", ondelete="SET NULL"), nullable=True)
    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
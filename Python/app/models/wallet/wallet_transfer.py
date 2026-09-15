

from sqlalchemy import Column, String, Numeric, ForeignKey, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base


class WalletTransfer(Base):
    __tablename__ = "wallet_transfers"
    __table_args__ = {"comment": "Lưu giao dịch chuyển tiền giữa các ví"}
    id = Column(String(36), primary_key=True, server_default=text("(UUID())"))
    from_wallet_id = Column(String(36), ForeignKey("wallets.id", ondelete="RESTRICT"), nullable=False, index=True)
    to_wallet_id = Column(String(36), ForeignKey("wallets.id", ondelete="RESTRICT"), nullable=False, index=True)
    amount = Column(Numeric(18, 4), nullable=False)
    transfer_type = Column(String(50), nullable=True, comment="INTERNAL, EXTERNAL")
    status = Column(String(50), server_default=text("'SUCCESS'"), nullable=False, comment="PENDING, SUCCESS, FAILED")
    description = Column(String(255), nullable=True)
    created_by = Column(String(36), nullable=True)
    created_at = Column(DateTime, server_default=func.now(), nullable=False)
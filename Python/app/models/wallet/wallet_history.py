# D:\UIT - HK2\FinanceProject\app\models\finance\wallet_history.py

from sqlalchemy import Column, String, Numeric, ForeignKey, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base


class WalletHistory(Base):
    __tablename__ = "wallet_histories"
    __table_args__ = {"comment": "Lưu lịch sử biến động số dư ví"}
    id = Column(String(36), primary_key=True, server_default=text("(UUID())"))
    wallet_id = Column(String(36), ForeignKey("wallets.id", ondelete="RESTRICT"), nullable=False, index=True)
    action_type = Column(String(50), nullable=False, comment="CREATE, UPDATE, TRANSFER, LOCK, DELETE")
    balance_before = Column(Numeric(18, 4), nullable=True)
    balance_after = Column(Numeric(18, 4), nullable=True)
    amount = Column(Numeric(18, 4), nullable=True)
    reference_id = Column(String(36), nullable=True, comment="Transaction/Transfer reference")
    note = Column(String(255), nullable=True)
    created_at = Column(DateTime, server_default=func.now(), nullable=False)
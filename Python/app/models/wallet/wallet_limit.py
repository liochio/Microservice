# D:\UIT - HK2\FinanceProject\app\models\finance\wallet_limit.py

from sqlalchemy import Column, String, Numeric, ForeignKey, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base


class WalletLimit(Base):
    __tablename__ = "wallet_limits"
    __table_args__ = {"comment": "Thiết lập hạn mức ví"}
    id = Column(String(36), primary_key=True, server_default=text("(UUID())"))
    wallet_id = Column(String(36), ForeignKey("wallets.id", ondelete="CASCADE"), nullable=False, unique=True)
    daily_limit = Column(Numeric(18, 4), nullable=True)
    monthly_limit = Column(Numeric(18, 4), nullable=True)
    transfer_limit = Column(Numeric(18, 4), nullable=True)
    created_at = Column(DateTime, server_default=func.now(), nullable=False)
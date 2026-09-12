# D:\UIT - HK2\FinanceProject\app\models\finance\wallet_notification_setting.py

from sqlalchemy import Column, String, Numeric, Boolean, ForeignKey, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base


class WalletNotificationSetting(Base):
    __tablename__ = "wallet_notification_settings"
    __table_args__ = {"comment": "Cấu hình thông báo ví"}
    id = Column(String(36), primary_key=True, server_default=text("(UUID())"))
    wallet_id = Column(String(36), ForeignKey("wallets.id", ondelete="CASCADE"), nullable=False, unique=True)
    low_balance_enabled = Column(Boolean, server_default=text("1"), nullable=False)
    large_transaction_enabled = Column(Boolean, server_default=text("1"), nullable=False)
    low_balance_threshold = Column(Numeric(18, 4), nullable=True)
    created_at = Column(DateTime, server_default=func.now(), nullable=False)
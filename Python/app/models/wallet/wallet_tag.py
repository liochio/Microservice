

from sqlalchemy import Column, String, ForeignKey, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base


class WalletTag(Base):
    __tablename__ = "wallet_tags"
    __table_args__ = {"comment": "Tag phân loại ví"}
    id = Column(String(36), primary_key=True, server_default=text("(UUID())"))
    wallet_id = Column(String(36), ForeignKey("wallets.id", ondelete="CASCADE"), nullable=False, index=True)
    tag_name = Column(String(100), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False)
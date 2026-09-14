from sqlalchemy import Column, String, Text, ForeignKey, DateTime, BigInteger, Integer, text
from sqlalchemy.sql import func
from app.db.base import Base

class Notification(Base):
    __tablename__ = "notifications"
    __table_args__ = {"comment": "Bảng lưu trữ thông báo phía người dùng"}

    id = Column(BigInteger, primary_key=True, autoincrement=True, index=True)
    tenant_id = Column(String(50), server_default=text("'SYSTEM'"), default="SYSTEM")
    recipient = Column(String(255), nullable=False, default="")
    channel = Column(String(50), server_default=text("'IN_APP'"), default="IN_APP")
    subject = Column(String(255), nullable=True)
    user_id = Column(String(64), nullable=True, index=True)
    title = Column(String(255), nullable=True)
    content = Column(Text, nullable=False)
    notification_type = Column(String(50), nullable=True, index=True)
    is_read = Column(Integer, server_default=text("0"), default=0)
    status = Column(String(50), server_default=text("'ACTIVE'"), default="ACTIVE")
    created_at = Column(DateTime, server_default=func.now(), nullable=False)
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False)

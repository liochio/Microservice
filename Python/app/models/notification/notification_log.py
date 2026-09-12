from sqlalchemy import Column, String, ForeignKey, Text, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class NotificationLog(Base):
    __tablename__ = "notification_logs"
    __table_args__ = {"comment": "Lịch sử đẩy tin nhắn qua nhà mạng SMS, Firebase Push"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    notification_id = Column(String(36), ForeignKey("notifications.id", ondelete="CASCADE"), nullable=False, index=True)
    channel = Column(String(50), nullable=False, index=True)
    recipient_target = Column(String(255), nullable=False)
    gateway_response = Column(Text, nullable=True)
    status = Column(String(50), server_default=text("'SENT'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
from sqlalchemy import Column, String, Text, ForeignKey, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class Notification(Base):
    __tablename__ = "notifications"
    __table_args__ = {"comment": "Bảng lưu trữ thông báo phía người dùng"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(64), nullable=False, index=True, comment="Logical FK liên kết với liochio-core")
    title = Column(String(255), nullable=False)
    content = Column(Text, nullable=False)
    notification_type = Column(String(50), nullable=False, index=True)
    is_read = Column(String(10), server_default=text("'UNREAD'"), nullable=False)
    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")

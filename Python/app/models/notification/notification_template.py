from sqlalchemy import Column, String, Text, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class NotificationTemplate(Base):
    __tablename__ = "notification_templates"
    __table_args__ = {"comment": "Bảng lưu trữ mẫu tin nhắn thông báo đa ngôn ngữ"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    template_code = Column(String(100), unique=True, nullable=False, index=True)
    title_template_vi = Column(String(255), nullable=False)
    content_template_vi = Column(Text, nullable=False)
    title_template_en = Column(String(255), nullable=False)
    content_template_en = Column(Text, nullable=False)
    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
from sqlalchemy import Column, String, ForeignKey, Text, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class SecurityLog(Base):
    __tablename__ = "security_logs"
    __table_args__ = {"comment": "Bảng ghi nhận nhật ký cảnh báo an ninh bảo mật và tấn công WAF"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(64), nullable=True, index=True, comment="Logical FK liên kết với liochio-core")
    event_type = Column(String(100), nullable=False, index=True)
    severity = Column(String(20), nullable=False)
    description = Column(Text, nullable=False)
    ip_address = Column(String(100), nullable=False)
    status = Column(String(50), server_default=text("'TRIGGERED'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")

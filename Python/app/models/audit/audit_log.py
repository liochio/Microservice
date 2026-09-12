from sqlalchemy import Column, String, ForeignKey, JSON, Index, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class AuditLog(Base):
    __tablename__ = "audit_logs"
    __table_args__ = (Index("ix_audit_user_created", "user_id", "created_at"), {"comment": "Bảng lịch sử kiểm toán lưu vết chi tiết"})

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(64), nullable=True, index=True, comment="Logical FK liên kết với liochio-core")
    action = Column(String(255), nullable=False)
    table_name = Column(String(255), nullable=False)
    old_data = Column(JSON, nullable=True)
    new_data = Column(JSON, nullable=True)
    ip_address = Column(String(100), nullable=True)
    user_agent = Column(String(500), nullable=True)
    status = Column(String(50), server_default=text("'SUCCESS'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")

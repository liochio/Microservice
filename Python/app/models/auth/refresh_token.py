from sqlalchemy import Column, String, DateTime, ForeignKey, Boolean, text
from sqlalchemy.sql import func
from app.db.base import Base

class RefreshToken(Base):
    __tablename__ = "refresh_tokens"
    __table_args__ = {"comment": "Bảng quản lý Refresh Token"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(64), nullable=False, index=True, comment="Logical FK liên kết với liochio-core")
    token = Column(String(500), unique=True, nullable=False, index=True)
    expires_at = Column(DateTime, nullable=False)
    is_revoked = Column(Boolean, server_default=text("0"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")

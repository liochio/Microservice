from sqlalchemy import Column, String, ForeignKey, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class Category(Base):
    __tablename__ = "categories"
    __table_args__ = {"comment": "Bảng phân loại danh mục thu chi"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(64), nullable=True, index=True, comment="Logical FK liên kết với liochio-core")
    name = Column(String(100), nullable=False)
    type = Column(String(20), nullable=False)
    parent_id = Column(String(36), ForeignKey("categories.id", ondelete="SET NULL"), nullable=True)
    icon = Column(String(100), nullable=True)
    color = Column(String(50), nullable=True)
    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")

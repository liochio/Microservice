from sqlalchemy import Column, String, DateTime, ForeignKey, Index, text
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.db.base import Base

class Permission(Base):
    __tablename__ = "permissions"
    __table_args__ = (Index("ux_permissions_code", "code", unique=True), {"comment": "Bảng danh mục quyền hạn hành động API chi tiết"})

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    module_id = Column(String(36), ForeignKey("modules.id", ondelete="CASCADE"), nullable=False, comment="Liên kết sang phân hệ modules")
    code = Column(String(100), nullable=False, comment="Mã quyền hạn (VD: WALLET_FREEZE)")
    name = Column(String(255), nullable=False, comment="Mô tả chi tiết quyền")
    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")

    module = relationship("Module", back_populates="permissions")
from sqlalchemy import Column, String, ForeignKey, DateTime, Index, text
from sqlalchemy.sql import func
from app.db.base import Base

class UserRole(Base):
    __tablename__ = "user_roles"
    __table_args__ = (Index("ux_user_role", "user_id", "role_id", unique=True), {"comment": "Bảng trung gian liên kết Người dùng và Vai trò"})

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(64), nullable=False, index=True, comment="Logical FK liên kết với liochio-core")
    role_id = Column(String(36), ForeignKey("roles.id", ondelete="CASCADE"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")

class RoleModule(Base):
    __tablename__ = "role_modules"
    __table_args__ = (Index("ux_role_module", "role_id", "module_id", unique=True), {"comment": "Bảng trung gian liên kết Vai trò và Module"})

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    role_id = Column(String(36), ForeignKey("roles.id", ondelete="CASCADE"), nullable=False)
    module_id = Column(String(36), ForeignKey("modules.id", ondelete="CASCADE"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")

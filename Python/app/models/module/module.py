from sqlalchemy import Column, String, DateTime, Index, text, ForeignKey
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.db.base import Base

class Module(Base):
    __tablename__ = "modules"
    __table_args__ = (Index("ux_modules_code", "code", unique=True), {"comment": "Bảng quản lý phân hệ chức năng menu động phân cấp cha-con"})

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    code = Column(String(100), nullable=False, comment="Mã định danh module (VD: FINANCE, WALLET_MGMT)")
    name = Column(String(150), nullable=False, comment="Tên hiển thị menu")
    icon = Column(String(100), nullable=True, comment="Icon hiển thị")
    parent_id = Column(String(36), ForeignKey("modules.id", ondelete="CASCADE"), nullable=True, comment="ID của module cha nếu phân cấp cây")
    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")

    # FIX DỨT ĐIỂM: Thêm single_parent=True để cho phép delete-orphan trên quan hệ cha-con
    sub_modules = relationship(
        "Module",
        back_populates="parent",
        cascade="all, delete-orphan",
        remote_side=[id],
        single_parent=True
    )
    parent = relationship("Module", back_populates="sub_modules", remote_side=[parent_id])
    permissions = relationship("Permission", back_populates="module", cascade="all, delete-orphan")
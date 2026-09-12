# D:\UIT - HK2\FinanceProject\app\models\finance\wallet.py

from sqlalchemy import Column, String, Numeric, ForeignKey, DateTime, Boolean, text
from sqlalchemy.sql import func
from app.db.base import Base


class Wallet(Base):
    __tablename__ = "wallets"
    __table_args__ = {"comment": "Bảng ví tài khoản lưu trữ số dư trực tiếp"}
    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(64), nullable=False, index=True, comment="Logical FK liên kết với liochio-core")
    wallet_code = Column(String(50), unique=True, nullable=False, comment="Mã ví duy nhất (vd: VPBANK_001)")
    name = Column(String(100), nullable=False)
    wallet_type = Column(String(50), nullable=False, index=True, comment="Phân loại: CASH, BANK, EWALLET, SAVINGS, SMART_PIGGY")
    wallet_account = Column(String(50), nullable=False, comment="Số tài khoản ví")
    balance = Column(Numeric(18, 4), server_default=text("0.0000"), nullable=False)
    currency = Column(String(10), server_default=text("'VND'"), nullable=False)
    color = Column(String(20), nullable=True, comment="Mã màu hiển thị UI")
    icon = Column(String(50), nullable=True, comment="Icon hiển thị UI")
    description = Column(String(255), nullable=True, comment="Mô tả mục đích sử dụng ví")
    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False, index=True, comment="ACTIVE, LOCKED, ARCHIVED")
    is_deleted = Column(Boolean, server_default=text("0"), nullable=False, comment="Cờ xóa mềm")
    deleted_at = Column(DateTime, nullable=True, comment="Thời điểm bị xóa mềm")
    is_default = Column(Boolean, server_default=text("0"), nullable=False, comment="Ví mặc định của user")
    locked_at = Column(DateTime, nullable=True, comment="Thời điểm khóa ví")
    locked_reason = Column(String(255), nullable=True, comment="Lý do khóa ví")
    archived_at = Column(DateTime, nullable=True, comment="Thời điểm archive ví")
    created_by = Column(String(36), nullable=True, comment="User tạo ví")
    updated_by = Column(String(36), nullable=True, comment="User cập nhật gần nhất")
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")

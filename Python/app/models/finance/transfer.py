from sqlalchemy import Column, String, Numeric, DateTime, ForeignKey, text
from sqlalchemy.sql import func
from app.db.base import Base

class Transfer(Base):
    __tablename__ = "transfers"
    __table_args__ = {"comment": "Bảng ghi nhận luồng chuyển tiền nội bộ giữa các ví"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(64), nullable=False, index=True, comment="Logical FK liên kết với liochio-core")
    source_wallet_id = Column(String(36), ForeignKey("wallets.id", ondelete="RESTRICT"), nullable=False, index=True)
    destination_wallet_id = Column(String(36), ForeignKey("wallets.id", ondelete="RESTRICT"), nullable=False, index=True)
    amount = Column(Numeric(18, 4), nullable=False)
    transfer_date = Column(DateTime, nullable=False)
    fee = Column(Numeric(18, 4), server_default=text("0.0000"), nullable=False)
    description = Column(String(500), nullable=True)
    status = Column(String(50), server_default=text("'COMPLETED'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")

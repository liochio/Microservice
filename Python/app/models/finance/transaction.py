from sqlalchemy import Column, String, Numeric, DateTime, ForeignKey, Index, text
from sqlalchemy.sql import func
from app.db.base import Base


class Transaction(Base):
    __tablename__ = "transactions"

    __table_args__ = (
        Index("ix_transaction_user_date", "user_id", "transaction_date"),
        {"comment": "Bảng quản lý biến động giao dịch thu chi"}
    )

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")

    user_id = Column(String(64), nullable=False, index=True, comment="Logical FK liên kết với liochio-core")

    wallet_id = Column(String(36), ForeignKey("wallets.id", ondelete="RESTRICT"), nullable=False, index=True)

    category_id = Column(String(36), ForeignKey("categories.id", ondelete="RESTRICT"), nullable=False, index=True)

    proof_file_id = Column(String(36), nullable=True, comment="ID tệp đính kèm hóa đơn chứng từ")

    amount = Column(Numeric(18, 4), nullable=False)

    transaction_type = Column(String(20), nullable=False, comment="TOPUP, INCOME, EXPENSE, TRANSFER")

    transaction_date = Column(DateTime, nullable=False)

    description = Column(String(500), nullable=True)

    status = Column(String(50), server_default=text("'COMPLETED'"), nullable=False)

    # ===== BỔ SUNG =====

    transaction_code = Column(String(50), unique=True, nullable=True, comment="Mã giao dịch duy nhất")

    balance_before = Column(Numeric(18, 4), nullable=True, comment="Số dư trước giao dịch")

    balance_after = Column(Numeric(18, 4), nullable=True, comment="Số dư sau giao dịch")

    source_type = Column(String(50), nullable=True, comment="BANK, EWALLET, CASH")

    source_name = Column(String(100), nullable=True, comment="Tên nguồn tiền")

    destination_wallet_id = Column(String(36), ForeignKey("wallets.id", ondelete="SET NULL"), nullable=True, comment="Ví nhận khi transfer")

    reference_id = Column(String(36), nullable=True, comment="Reference payment/transfer")

    external_transaction_id = Column(String(100), nullable=True, comment="Mã giao dịch từ hệ thống ngoài")

    failure_reason = Column(String(255), nullable=True, comment="Lý do thất bại")

    created_by = Column(String(36), nullable=True, comment="User tạo giao dịch")

    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")

    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")

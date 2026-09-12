# D:\UIT - HK2\FinanceProject\app\models\finance\mock_bank_account.py

from sqlalchemy import Column, String, Numeric, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base


class MockBankAccount(Base):
    __tablename__ = "mock_bank_accounts"
    __table_args__ = {"comment": "Tài khoản ngân hàng giả lập"}

    id = Column(String(36), primary_key=True, server_default=text("(UUID())"))

    bank_name = Column(String(100), nullable=False)

    account_number = Column(String(50), nullable=False, unique=True)

    account_name = Column(String(255), nullable=False)

    balance = Column(Numeric(18, 4), server_default=text("0.0000"), nullable=False)

    currency = Column(String(10), server_default=text("'VND'"), nullable=False)

    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False)

    created_at = Column(DateTime, server_default=func.now(), nullable=False)
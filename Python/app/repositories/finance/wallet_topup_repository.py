# D:\UIT - HK2\FinanceProject\app\repositories\finance\wallet_topup_repository.py

from datetime import datetime, time, timedelta
from decimal import Decimal
from typing import Optional

from sqlalchemy.orm import Session
from sqlalchemy import func

from app.models.wallet.wallet import Wallet
from app.models.finance.mock_bank_account import MockBankAccount
from app.models.finance.transaction import Transaction


class WalletTopupRepository:
    """👑 KHO LƯU TRỮ TRUY VẤN THEO SỐ TÀI KHOẢN CHỐNG RACE CONDITION"""

    @staticmethod
    def get_wallet_by_account_for_update(
        db: Session,
        wallet_account: str
    ) -> Optional[Wallet]:

        return db.query(Wallet).filter(
            Wallet.wallet_account == wallet_account,
            Wallet.is_deleted == False
        ).with_for_update(nowait=False).first()

    @staticmethod
    def get_bank_account_by_number_for_update(
        db: Session,
        bank_name: str,
        account_number: str
    ) -> Optional[MockBankAccount]:

        return db.query(MockBankAccount).filter(
            func.lower(MockBankAccount.bank_name) == func.lower(bank_name),
            MockBankAccount.account_number == account_number
        ).with_for_update(nowait=False).first()

    @staticmethod
    def get_daily_total_topup(
        db: Session,
        user_id: str
    ) -> Decimal:

        today_start = datetime.combine(datetime.now().date(), time.min)
        today_end = datetime.combine(datetime.now().date(), time.max)

        result = db.query(func.sum(Transaction.amount)).filter(
            Transaction.user_id == user_id,
            Transaction.transaction_type == "TOPUP",
            Transaction.status == "COMPLETED",
            Transaction.created_at >= today_start,
            Transaction.created_at <= today_end
        ).scalar()

        return Decimal(str(result)) if result else Decimal("0.0000")

    @staticmethod
    def get_monthly_total_topup(
        db: Session,
        user_id: str
    ) -> Decimal:

        first_day = datetime.now().replace(
            day=1,
            hour=0,
            minute=0,
            second=0,
            microsecond=0
        )

        result = db.query(func.sum(Transaction.amount)).filter(
            Transaction.user_id == user_id,
            Transaction.transaction_type == "TOPUP",
            Transaction.status == "COMPLETED",
            Transaction.created_at >= first_day
        ).scalar()

        return Decimal(str(result)) if result else Decimal("0.0000")

    @staticmethod
    def check_duplicate_topup(
        db: Session,
        user_id: str,
        wallet_account: str,
        amount: Decimal
    ):

        latest_tx = db.query(Transaction).join(
            Wallet,
            Wallet.id == Transaction.wallet_id
        ).filter(
            Transaction.user_id == user_id,
            Transaction.transaction_type == "TOPUP",
            Wallet.wallet_account == wallet_account,
            Transaction.amount == amount
        ).order_by(
            Transaction.created_at.desc()
        ).first()

        if not latest_tx:
            return None

        time_diff = datetime.now() - latest_tx.created_at

        if time_diff <= timedelta(seconds=10):
            return latest_tx

        return None

    @staticmethod
    def save_transaction(
        db: Session,
        tx: Transaction
    ) -> Transaction:

        db.add(tx)
        db.flush()

        return tx
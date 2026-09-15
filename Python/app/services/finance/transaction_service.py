
from datetime import datetime
from sqlalchemy import select, update
from decimal import Decimal
from app.repositories.finance.transaction_repository import TransactionRepository
from app.repositories.wallet.wallet_repository import WalletRepository
from app.repositories.finance.category_repository import CategoryRepository
from app.models.wallet.wallet import Wallet
from app.constants import SystemConstants
from app.core.exceptions.base_exception import FintechBaseException


class TransactionService:
    """
    👑 SERVICE: NGHIỆP VỤ QUẢN LÝ GIAO DỊCH THU / CHI
    🎯 Tuân thủ Quy tắc Bọc Thép:
       - Cập nhật số dư ví và tạo giao dịch trong cùng 1 Transaction SQL (ACID).
       - Kiểm tra nghiêm ngặt số dư ví nếu là Chi tiêu (EXPENSE).
    """

    @staticmethod
    def get_user_transactions(db, user_id: str, wallet_id: str = None, category_id: str = None,
                              tx_type: str = None, start_date: str = None, end_date: str = None,
                              limit: int = 50, offset: int = 0) -> tuple:
        s_date = datetime.fromisoformat(start_date) if start_date else None
        e_date = datetime.fromisoformat(end_date) if end_date else None
        return TransactionRepository.list_transactions(
            db=db, user_id=user_id, wallet_id=wallet_id, category_id=category_id,
            tx_type=tx_type, start_date=s_date, end_date=e_date, limit=limit, offset=offset
        )

    @staticmethod
    def get_transaction_detail(db, user_id: str, transaction_id: str) -> dict:
        tx = TransactionRepository.get_by_id(db, transaction_id, user_id)
        if not tx:
            raise FintechBaseException(error_code="TRANSACTION_NOT_FOUND", status_code=404)
        return tx

    @staticmethod
    def create_transaction(db, user_id: str, payload) -> dict:
        # 1. Kiểm tra ví tài khoản tồn tại và thuộc sở hữu của User
        wallet = db.execute(
            select(Wallet).where(Wallet.id == payload.wallet_id, Wallet.user_id == user_id, Wallet.is_deleted == 0)
        ).scalars().first()
        if not wallet:
            raise FintechBaseException(error_code=SystemConstants.WALLET_NOT_FOUND, status_code=404)

        # 2. Kiểm tra danh mục hợp lệ
        cat = CategoryRepository.get_by_id(db, payload.category_id)
        if not cat:
            raise FintechBaseException(error_code="CATEGORY_NOT_FOUND", status_code=404)

        # 3. Tính toán biến động số dư ví
        current_bal = Decimal(str(wallet.balance))
        tx_amount = Decimal(str(payload.amount))

        if payload.transaction_type == "EXPENSE":
            if current_bal < tx_amount:
                raise FintechBaseException(error_code="INSUFFICIENT_WALLET_BALANCE", status_code=400)
            new_bal = current_bal - tx_amount
        else:  # INCOME
            new_bal = current_bal + tx_amount

        # 4. Cập nhật số dư ví
        wallet.balance = new_bal
        wallet.updated_at = datetime.now()

        # 5. Ghi nhận giao dịch vào bảng transactions
        tx_record = TransactionRepository.insert_transaction(
            db=db,
            user_id=user_id,
            wallet_id=payload.wallet_id,
            category_id=payload.category_id,
            amount=float(tx_amount),
            tx_type=payload.transaction_type,
            tx_date=payload.transaction_date or datetime.now(),
            description=payload.description or "",
            balance_before=float(current_bal),
            balance_after=float(new_bal)
        )

        db.commit()
        return tx_record

    @staticmethod
    def delete_transaction(db, user_id: str, transaction_id: str) -> bool:
        tx = TransactionRepository.get_by_id(db, transaction_id, user_id)
        if not tx:
            raise FintechBaseException(error_code="TRANSACTION_NOT_FOUND", status_code=404)

        wallet = db.execute(
            select(Wallet).where(Wallet.id == tx["wallet_id"], Wallet.user_id == user_id, Wallet.is_deleted == 0)
        ).scalars().first()
        if wallet:
            # Hoàn lại số dư ví
            current_bal = Decimal(str(wallet.balance))
            amt = Decimal(str(tx["amount"]))
            if tx["transaction_type"] == "EXPENSE":
                wallet.balance = current_bal + amt
            elif tx["transaction_type"] == "INCOME":
                wallet.balance = max(Decimal("0"), current_bal - amt)
            wallet.updated_at = datetime.now()

        # Cập nhật trạng thái giao dịch sang CANCELLED
        from app.models.finance.transaction import Transaction
        db.execute(update(Transaction).where(Transaction.id == transaction_id).values(status="CANCELLED"))
        db.commit()
        return True

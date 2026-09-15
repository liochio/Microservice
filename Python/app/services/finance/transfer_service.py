
from datetime import datetime
from decimal import Decimal
from sqlalchemy import select
from app.repositories.finance.transfer_repository import TransferRepository
from app.models.wallet.wallet import Wallet
from app.constants import SystemConstants
from app.core.exceptions.base_exception import FintechBaseException


class TransferService:
    """
    👑 SERVICE: ĐIỀU PHỐI CHUYỂN TIỀN NGUYÊN TỬ (ACID TRANSACTION)
    🎯 Quy tắc Bọc Thép:
       - Trừ tiền ví nguồn và cộng tiền ví đích trong duy nhất 1 Transaction SQL.
       - Chống âm tiền ví nguồn.
    """

    @staticmethod
    def get_user_transfers(db, user_id: str, limit: int = 50) -> list:
        return TransferRepository.list_by_user(db, user_id, limit)

    @staticmethod
    def get_transfer_detail(db, user_id: str, transfer_id: str) -> dict:
        tx = TransferRepository.get_by_id(db, transfer_id, user_id)
        if not tx:
            raise FintechBaseException(error_code="TRANSFER_NOT_FOUND", status_code=404)
        return tx

    @staticmethod
    def execute_transfer(db, user_id: str, payload) -> dict:
        # 1. Khóa và lấy ví nguồn
        src_wallet = db.execute(
            select(Wallet).where(Wallet.id == payload.source_wallet_id, Wallet.user_id == user_id, Wallet.is_deleted == 0)
        ).scalars().first()
        if not src_wallet:
            raise FintechBaseException(error_code="SOURCE_WALLET_NOT_FOUND", status_code=404)

        # 2. Khóa và lấy ví đích
        dest_wallet = db.execute(
            select(Wallet).where(Wallet.id == payload.destination_wallet_id, Wallet.user_id == user_id, Wallet.is_deleted == 0)
        ).scalars().first()
        if not dest_wallet:
            raise FintechBaseException(error_code="DESTINATION_WALLET_NOT_FOUND", status_code=404)

        # 3. Kiểm tra số dư ví nguồn
        src_bal = Decimal(str(src_wallet.balance))
        dest_bal = Decimal(str(dest_wallet.balance))
        amount = Decimal(str(payload.amount))

        if src_bal < amount:
            raise FintechBaseException(error_code="INSUFFICIENT_WALLET_BALANCE", status_code=400)

        # 4. Cập nhật số dư 2 ví trong cùng 1 Transaction
        src_wallet.balance = src_bal - amount
        src_wallet.updated_at = datetime.now()

        dest_wallet.balance = dest_bal + amount
        dest_wallet.updated_at = datetime.now()

        # 5. Ghi bản ghi Transfer
        transfer_record = TransferRepository.insert_transfer(
            db=db,
            user_id=user_id,
            source_id=payload.source_wallet_id,
            dest_id=payload.destination_wallet_id,
            amount=float(amount),
            fee=0.0,
            description=payload.description or "Chuyển tiền nội bộ giữa các ví"
        )

        db.commit()
        return transfer_record

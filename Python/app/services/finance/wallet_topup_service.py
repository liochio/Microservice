

import uuid
import secrets

from decimal import Decimal
from datetime import datetime
import uuid
import secrets

from sqlalchemy.orm import Session
from sqlalchemy import text

from app.constants import SystemConstants
from app.repositories.finance.wallet_topup_repository import WalletTopupRepository
from app.schemas.wallet_topup.wallet_topup import WalletTopupRequest, WalletTopupResponse
from app.models.finance.transaction import Transaction
from app.core.exceptions.base_exception import FintechBaseException


class WalletTopupService:
    """👑 DỊCH VỤ NẠP TIỀN VÀ KIỂM SOÁT GIAN LẬN TÀI CHÍNH (FRAUD CONTROL TOPUP SERVICE)"""

    @staticmethod
    def execute_topup(
        db: Session,
        req: WalletTopupRequest,
        user_id: str,
        client_ip: str
    ) -> WalletTopupResponse:

        # 1. KIỂM TRA HẠN MỨC NGÀY / THÁNG
        daily_spent = WalletTopupRepository.get_daily_total_topup(db, user_id)

        if daily_spent + req.amount > Decimal("200000000"):
            raise FintechBaseException(
                error_code=SystemConstants.DAILY_TOPUP_LIMIT_EXCEEDED,
                status_code=400
            )

        monthly_spent = WalletTopupRepository.get_monthly_total_topup(db, user_id)

        if monthly_spent + req.amount > Decimal("1000000000"):
            raise FintechBaseException(
                error_code=SystemConstants.MONTHLY_TOPUP_LIMIT_EXCEEDED,
                status_code=400
            )

        # 2. VALIDATOR DUPLICATE REQUEST
        duplicate_tx = WalletTopupRepository.check_duplicate_topup(
            db=db,
            user_id=user_id,
            wallet_account=req.wallet_account,
            amount=req.amount
        )

        if duplicate_tx:
            raise FintechBaseException(
                error_code=SystemConstants.DUPLICATE_TRANSACTION_DETECTED,
                status_code=409
            )

        # 3. ROW LOCKING WALLET
        wallet = WalletTopupRepository.get_wallet_by_account_for_update(
            db,
            req.wallet_account
        )

        if not wallet:
            raise FintechBaseException(
                error_code=SystemConstants.WALLET_NOT_FOUND,
                status_code=404
            )

        if wallet.user_id != user_id:
            raise FintechBaseException(
                error_code=SystemConstants.WALLET_FORBIDDEN_ACCESS,
                status_code=403
            )

        if wallet.status != SystemConstants.ACTIVE:
            raise FintechBaseException(
                error_code=SystemConstants.WALLET_INACTIVE,
                status_code=400
            )

        if getattr(wallet, "is_deleted", False):
            raise FintechBaseException(
                error_code=SystemConstants.WALLET_DELETED,
                status_code=400
            )

        # 4. ROW LOCKING BANK ACCOUNT
        bank = WalletTopupRepository.get_bank_account_by_number_for_update(
            db,
            req.bank_name,
            req.account_number
        )

        if not bank:
            raise FintechBaseException(
                error_code=SystemConstants.BANK_ACCOUNT_NOT_FOUND,
                status_code=404
            )

        if bank.account_name.strip().upper() != req.account_name.strip().upper():
            raise FintechBaseException(
                error_code=SystemConstants.BANK_ACCOUNT_NAME_MISMATCH,
                status_code=400
            )

        if bank.status != SystemConstants.ACTIVE:
            raise FintechBaseException(
                error_code=SystemConstants.BANK_ACCOUNT_INACTIVE,
                status_code=400
            )

        # 5. VALIDATOR CURRENCY MATCH
        if wallet.currency != bank.currency:
            raise FintechBaseException(
                error_code=SystemConstants.CURRENCY_MISMATCH,
                status_code=400
            )

        # 6. VALIDATOR SUFFICIENT BALANCE
        if Decimal(str(bank.balance)) < Decimal(str(req.amount)):
            raise FintechBaseException(
                error_code=SystemConstants.BANK_INSUFFICIENT_BALANCE,
                status_code=400
            )

        # 7. FRAUD DETECTION
        if req.amount >= Decimal("50000000"):
            print(
                f"⚠️ [FRAUD ALERT] "
                f"User={user_id} "
                f"IP={client_ip} "
                f"Amount={req.amount}"
            )

        # 8. CATEGORY SAFE VALIDATION
        cate_row = db.execute(
            text("SELECT id FROM categories LIMIT 1")
        ).fetchone()

        if not cate_row:
            raise FintechBaseException(
                error_code=SystemConstants.CATEGORY_NOT_FOUND,
                status_code=400
            )

        valid_category_id = str(cate_row[0])

        # 9. BALANCE CONSISTENCY
        balance_before = Decimal(str(wallet.balance))
        bank.balance = Decimal(str(bank.balance)) - Decimal(str(req.amount))
        wallet.balance = Decimal(str(wallet.balance)) + Decimal(str(req.amount))
        balance_after = Decimal(str(wallet.balance))

        expected_balance = balance_before + Decimal(str(req.amount))
        if balance_after != expected_balance:
            raise FintechBaseException(
                error_code=SystemConstants.BALANCE_INCONSISTENCY_DETECTED,
                status_code=500
            )

        # 10. GENERATE UNIQUE TRANSACTION CODE
        random_suffix = secrets.token_hex(4).upper()
        tx_code = (
            f"TX-TOPUP-"
            f"{datetime.now().strftime('%Y%m%d%H%M%S')}-"
            f"{random_suffix}"
        )

        # 11. INSERT TRANSACTION
        new_tx = Transaction(
            id=str(uuid.uuid4()),
            user_id=user_id,
            wallet_id=wallet.id,
            category_id=valid_category_id,
            amount=req.amount,
            transaction_type="TOPUP",
            transaction_date=datetime.now(),
            description=req.description,
            status="COMPLETED",
            transaction_code=tx_code,
            balance_before=balance_before,
            balance_after=balance_after,
            source_type="BANK",
            source_name=bank.bank_name,
            reference_id=bank.account_number,
            external_transaction_id=str(uuid.uuid4()).replace("-", "").upper()[:16],
            created_by=user_id
        )

        WalletTopupRepository.save_transaction(db, new_tx)

        # 12. DB COMMIT
        try:
            db.commit()
        except Exception:
            db.rollback()
            raise FintechBaseException(
                error_code=SystemConstants.TRANSACTION_COMMIT_FAILED,
                status_code=500
            )

        db.refresh(new_tx)

        return WalletTopupResponse(
            success=True,
            transaction_code=tx_code,
            amount=req.amount,
            balance_before=balance_before,
            balance_after=balance_after,
            wallet_currency=wallet.currency,
            status="COMPLETED",
            message=""
        )
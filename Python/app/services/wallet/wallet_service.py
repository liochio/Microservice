from typing import List, Dict, Any, Optional
from datetime import datetime
from decimal import Decimal
import uuid
from app.constants import SystemConstants
from app.repositories.wallet.wallet_repository import WalletRepository
from app.repositories.notification.notification_repository import NotificationRepository
from app.repositories.finance.transaction_repository import TransactionRepository
from app.models.finance.category import Category
from app.core.exceptions.base_exception import FintechBaseException
from app.core.validators.generic_validator import GenericValidator
from app.models.wallet.wallet import Wallet
from app.core.logging.logger import DBLogger


class WalletService:
    @staticmethod
    def create_user_wallet(
            db_conn, user_id: str, wallet_code: str, name: str, currency: str,
            wallet_type: str, color: Optional[str], icon: Optional[str],
            description: Optional[str]
    ) -> Dict[str, Any]:
        try:
            # 1. Validation Logic
            GenericValidator.check_duplicate(
                db_conn=db_conn, model_class=Wallet, field_name="wallet_code",
                value=wallet_code, error_code=SystemConstants.WALLET_ALREADY_EXISTS
            )

            # 2. Ghi log tập trung và thực thi qua Repository SQL thô
            DBLogger.execute_repo_log(
                db_conn=db_conn,
                repo_class=WalletRepository,
                method_name="create_wallet",
                console_msg=f"User {user_id} tạo ví {wallet_code}",
                user_id=user_id,
                wallet_code=wallet_code,
                name=name,
                currency=currency,
                wallet_type=wallet_type,
                color=color,
                icon=icon,
                description=description
            )

            # 3. Chèn thông báo hàng đợi Mail liên thông hệ thống
            from app.core.translator.translator_engine import i18n_translator
            mail_title = i18n_translator.translate("vi", SystemConstants.WALLET_CREATE_SUCCESS, msg_type=SystemConstants.MSG_TYPE_MESSAGE)
            NotificationRepository.insert_notification(
                db_conn=db_conn, user_id=user_id, template_code="WALLET_CREATION_MAIL",
                title=mail_title,
                body=f"WALLET_CODE: {wallet_code} | NAME: {name} | CURRENCY: {currency}"
            )

            if hasattr(db_conn, "commit"):
                db_conn.commit()

            return {"status": "SUCCESS", "wallet_code": wallet_code}

        except FintechBaseException as f_exc:
            if hasattr(db_conn, "rollback"):
                db_conn.rollback()
            raise f_exc
        except Exception as e:
            if hasattr(db_conn, "rollback"):
                db_conn.rollback()
            DBLogger.system(db_conn, "ERROR", "WalletService", f"Lỗi: {str(e)}")
            raise FintechBaseException(SystemConstants.INTERNAL_SERVER_ERROR, 500)

    @staticmethod
    def get_wallet_detail_secure(db_conn, user_id: str, wallet_id: str) -> Dict[str, Any]:
        wallet = WalletRepository.get_wallet_by_id(db_conn, wallet_id)
        if not wallet or wallet.is_deleted:
            raise FintechBaseException(SystemConstants.WALLET_NOT_FOUND, 404)

        if wallet.user_id != user_id:
            DBLogger.security(db_conn, user_id, "ILLEGAL_WALLET_ACCESS_ATTEMPT", "HIGH",
                              "INTERNAL", f"Truy cập trái phép ví [{wallet_id}]")
            raise FintechBaseException(SystemConstants.WALLET_FORBIDDEN_ACCESS, 403)

        return {
            "id": str(wallet.id),
            "user_id": str(wallet.user_id),
            "name": wallet.name,
            "wallet_code": wallet.wallet_code,
            "wallet_type": wallet.wallet_type,
            "wallet_account": getattr(wallet, "wallet_account", ""),
            "description": wallet.description,
            "balance": str(wallet.balance) if wallet.balance is not None else "0.0000",
            "currency": wallet.currency,
            "color": wallet.color,
            "icon": wallet.icon,
            "status": wallet.status,
            "created_at": wallet.created_at.isoformat()
        }

    @staticmethod
    def list_user_wallets(db_conn, user_id: str) -> List[Dict[str, Any]]:
        wallets = WalletRepository.get_wallets_by_user_id(db_conn, user_id)
        return [
            {
                "id": str(w.id),
                "user_id": str(w.user_id),
                "name": w.name,
                "wallet_code": w.wallet_code,
                "wallet_type": w.wallet_type,
                "wallet_account": getattr(w, "wallet_account", ""),
                "description": w.description,
                "balance": str(w.balance) if w.balance is not None else "0.0000",
                "currency": w.currency,
                "color": w.color,
                "icon": w.icon,
                "status": w.status
            } for w in wallets
        ]

    @staticmethod
    def update_user_wallet(
            db_conn, user_id: str, wallet_id: str, wallet_code: str, name: str,
            currency: str, wallet_type: str, color: Optional[str], icon: Optional[str],
            description: Optional[str]
    ) -> Dict[str, Any]:
        wallet = WalletRepository.get_wallet_by_id(db_conn, wallet_id)
        if not wallet or wallet.is_deleted:
            raise FintechBaseException(SystemConstants.WALLET_NOT_FOUND, 404)

        if wallet.user_id != user_id:
            DBLogger.security(db_conn, user_id, "ILLEGAL_WALLET_UPDATE_ATTEMPT", "HIGH",
                              "INTERNAL", f"Cố ý cập nhật trái phép ví [{wallet_id}]")
            raise FintechBaseException(SystemConstants.WALLET_FORBIDDEN_ACCESS, 403)

        if str(wallet.status).upper() != SystemConstants.ACTIVE:
            raise FintechBaseException(SystemConstants.WALLET_INVALID_STATUS, 400)

        try:
            if wallet.wallet_code != wallet_code:
                GenericValidator.check_duplicate(
                    db_conn=db_conn, model_class=Wallet, field_name="wallet_code",
                    value=wallet_code, error_code=SystemConstants.WALLET_ALREADY_EXISTS
                )

            DBLogger.execute_repo_log(
                db_conn=db_conn,
                repo_class=WalletRepository,
                method_name="update_wallet",
                console_msg=f"User {user_id} cập nhật thông tin ví ID {wallet_id}",
                wallet_id=wallet_id,
                wallet_code=wallet_code,
                name=name,
                currency=currency,
                wallet_type=wallet_type,
                color=color,
                icon=icon,
                description=description
            )

            if hasattr(db_conn, "commit"):
                db_conn.commit()

            return {"status": "SUCCESS", "wallet_id": wallet_id, "wallet_code": wallet_code}

        except FintechBaseException as f_exc:
            if hasattr(db_conn, "rollback"):
                db_conn.rollback()
            raise f_exc
        except Exception as e:
            if hasattr(db_conn, "rollback"):
                db_conn.rollback()
            DBLogger.system(db_conn, "ERROR", "WalletService", f"Lỗi cập nhật ví: {str(e)}")
            raise FintechBaseException(SystemConstants.INTERNAL_SERVER_ERROR, 500)

    @staticmethod
    def soft_delete_wallet(db_conn, user_id: str, wallet_id: str) -> Dict[str, Any]:
        wallet = WalletRepository.get_wallet_by_id(db_conn, wallet_id)
        if not wallet or wallet.is_deleted:
            raise FintechBaseException(SystemConstants.WALLET_NOT_FOUND, 404)

        if wallet.user_id != user_id:
            DBLogger.security(db_conn, user_id, "ILLEGAL_WALLET_DELETE_ATTEMPT", "HIGH",
                              "INTERNAL", f"Cố ý xóa trái phép ví [{wallet_id}]")
            raise FintechBaseException(SystemConstants.WALLET_FORBIDDEN_ACCESS, 403)

        try:
            DBLogger.execute_repo_log(
                db_conn=db_conn,
                repo_class=WalletRepository,
                method_name="soft_delete_wallet",
                console_msg=f"User {user_id} tiến hành xóa mềm ví ID {wallet_id}",
                wallet_id=wallet_id
            )

            if hasattr(db_conn, "commit"):
                db_conn.commit()

            return {"status": "DELETED", "wallet_id": wallet_id, "is_deleted": True}

        except Exception as e:
            if hasattr(db_conn, "rollback"):
                db_conn.rollback()
            DBLogger.system(db_conn, "ERROR", "WalletService", f"Lỗi xóa ví: {str(e)}")
            raise FintechBaseException(SystemConstants.INTERNAL_SERVER_ERROR, 500)

    @staticmethod
    def transfer_funds(
        db_conn,
        user_id: str,
        source_wallet_id: str,
        target_wallet_id: str,
        amount: float,
        description: str = ""
    ) -> Dict[str, Any]:
        """
        🔒 CHUYỂN TIỀN KHÉP KÍN (CLOSED-LOOP TRANSFER ENGINE)
        - Quy tắc 1: Ví nguồn phải thuộc về User và không bị FROZEN.
        - Quy tắc 2 (CLOSED-LOOP): Nếu ví nguồn là SAVINGS (Heo Đất), CẤM P2P sang User khác!
          Chỉ cho phép INTERNAL_WITHDRAWAL sang ví khác của CHÍNH MÌNH.
        - Quy tắc 3: Kiểm tra số dư và chuyển tiền ACID.
        """
        if amount <= 0:
            raise FintechBaseException(error_code="INVALID_AMOUNT", status_code=400)

        source_wallet = WalletRepository.get_wallet_by_id(db_conn, source_wallet_id)
        if not source_wallet or source_wallet.is_deleted:
            raise FintechBaseException(SystemConstants.WALLET_NOT_FOUND, 404)

        if str(source_wallet.user_id) != str(user_id):
            raise FintechBaseException(SystemConstants.WALLET_FORBIDDEN_ACCESS, 403)

        if str(getattr(source_wallet, "status", "")).upper() == "FROZEN":
            raise FintechBaseException(
                error_code="WALLET_FROZEN",
                status_code=403
            )

        target_wallet = WalletRepository.get_wallet_by_id(db_conn, target_wallet_id)
        if not target_wallet or target_wallet.is_deleted:
            raise FintechBaseException(error_code="TARGET_WALLET_NOT_FOUND", status_code=404)

        # 🛑 QUY TẮC BỌC THÉP CLOSED-LOOP: CHẶN P2P TỪ VÍ SAVINGS
        is_source_savings = (getattr(source_wallet, "wallet_type", "").upper() == "SAVINGS" or "HEO" in getattr(source_wallet, "name", "").upper())
        if is_source_savings:
            if str(target_wallet.user_id) != str(user_id):
                raise FintechBaseException(
                    error_code="P2P_TRANSFER_FORBIDDEN_FROM_SAVINGS",
                    status_code=403
                )

        current_src_bal = float(source_wallet.balance)
        if current_src_bal < amount:
            raise FintechBaseException(error_code="INSUFFICIENT_WALLET_BALANCE", status_code=400)

        # Thực thi chuyển tiền ACID
        new_src_bal = current_src_bal - amount
        new_tgt_bal = float(target_wallet.balance) + amount

        source_wallet.balance = new_src_bal
        target_wallet.balance = new_tgt_bal
        source_wallet.updated_at = datetime.now()
        target_wallet.updated_at = datetime.now()

        transfer_type = "INTERNAL_WITHDRAWAL" if str(target_wallet.user_id) == str(user_id) else "EXTERNAL_P2P_TRANSFER"

        # Ghi nhận sổ cái
        expense_cat = db_conn.query(Category).filter(Category.type == "EXPENSE").first()
        cat_id = expense_cat.id if expense_cat else None

        TransactionRepository.insert_transaction(
            db=db_conn,
            user_id=user_id,
            wallet_id=source_wallet_id,
            category_id=cat_id,
            amount=amount,
            tx_type="EXPENSE",
            tx_date=datetime.now(),
            description=f"[{transfer_type}] Chuyển tiền tới ví {target_wallet.wallet_code}: {description}",
            balance_before=current_src_bal,
            balance_after=new_src_bal
        )

        income_cat = db_conn.query(Category).filter(Category.type == "INCOME").first()
        in_cat_id = income_cat.id if income_cat else None

        TransactionRepository.insert_transaction(
            db=db_conn,
            user_id=target_wallet.user_id,
            wallet_id=target_wallet_id,
            category_id=in_cat_id,
            amount=amount,
            tx_type="INCOME",
            tx_date=datetime.now(),
            description=f"[{transfer_type}] Nhận tiền từ ví {source_wallet.wallet_code}: {description}",
            balance_before=float(target_wallet.balance) - amount,
            balance_after=new_tgt_bal
        )

        if hasattr(db_conn, "commit"):
            db_conn.commit()

        return {
            "status": "TRANSFER_SUCCESS",
            "transfer_type": transfer_type,
            "source_wallet_id": source_wallet_id,
            "target_wallet_id": target_wallet_id,
            "amount": amount,
            "source_balance_after": new_src_bal,
            "target_balance_after": new_tgt_bal,
            "message": f"Chuyển tiền thành công ({transfer_type}). Sổ cái đã cập nhật đối ứng."
        }
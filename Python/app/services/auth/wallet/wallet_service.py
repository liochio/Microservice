# D:\UIT - HK2\FinanceProject\app\services\auth\wallet\wallet_service.py

from typing import List, Dict, Any, Optional
from app.repositories.wallet.wallet_repository import WalletRepository
from app.core.exceptions.base_exception import FintechBaseException


class WalletService:

    @staticmethod
    def _map_wallet_to_dict(wallet: Any) -> Dict[str, Any]:
        """
        👑 HÀM HELPER: Gom nhóm logic chuyển đổi dữ liệu để triệt tiêu lỗi Duplicated Code
        """
        return {
            "id": str(wallet.id),
            "user_id": str(getattr(wallet, "user_id", "")),
            "wallet_code": str(wallet.wallet_code),
            "name": str(wallet.name),
            "wallet_type": str(wallet.wallet_type),
            "balance": str(wallet.balance) if wallet.balance is not None else "0.0000",
            "currency": str(wallet.currency),
            "color": str(wallet.color) if wallet.color else None,
            "icon": str(wallet.icon) if wallet.icon else None,
            "status": str(wallet.status)
        }

    @staticmethod
    def get_wallets_by_user(db_conn: Any, user_id: str) -> List[Dict[str, Any]]:
        print("===========get_wallets_by_user=========", user_id)
        try:
            orm_wallets = WalletRepository.get_wallets_by_user_id(db_conn, user_id)
            return [WalletService._map_wallet_to_dict(w) for w in orm_wallets]
        except Exception as e:
            print(f"[SERVICE_ERROR] Lỗi tại WalletService: {str(e)}")
            return []

    @staticmethod
    def create_user_wallet(db_conn: Any, user_id: str, name: str, currency: str = "VND",
                           wallet_type: str = "CASH", color: Optional[str] = None,
                           icon: Optional[str] = None, trace_id: Optional[str] = None) -> Dict[str, Any]:
        """
        👑 Bổ sung hàm tạo ví liên thông qua WalletRepository
        """
        from app.constants import SystemConstants
        if not name or str(name).strip() == "":
            raise FintechBaseException(SystemConstants.MISSING_WALLET_NAME, 400)

        if not currency or str(currency).upper() not in ["VND", "USD", "EUR"]:
            raise FintechBaseException(SystemConstants.INVALID_CURRENCY_ENUM, 400)

        if not wallet_type:
            wallet_type = "CASH"

        try:
            wallet = WalletRepository.create_wallet(
                db_conn, user_id, name, currency, wallet_type=wallet_type, color=color, icon=icon
            )

            from app.core.translator.translator_engine import i18n_translator
            mail_title = i18n_translator.translate("vi", SystemConstants.WALLET_CREATE_SUCCESS, msg_type=SystemConstants.MSG_TYPE_MESSAGE)
            WalletRepository.insert_wallet_notification(
                db_conn=db_conn,
                user_id=user_id,
                title=mail_title,
                content=f"WALLET_NAME: {name} | CURRENCY: {currency.upper()}"
            )

            if hasattr(db_conn, "commit"):
                db_conn.commit()

            return WalletService._map_wallet_to_dict(wallet)

        except FintechBaseException:
            raise
        except Exception as e:
            if hasattr(db_conn, "rollback"):
                db_conn.rollback()
            print(f"[SERVICE_CRASH] Lỗi nghiêm trọng khi tạo ví: {str(e)} | Trace-ID: {trace_id}")
            raise FintechBaseException(SystemConstants.INTERNAL_SERVER_ERROR, 500)

    @staticmethod
    def get_wallet_detail_secure(db_conn: Any, user_id: str, wallet_id: str, trace_id: Optional[str] = None) -> Dict[str, Any]:
        """
        👑 Bổ sung hàm xem số dư ví bảo mật, chống tấn công rò rỉ ID chéo
        """
        from app.constants import SystemConstants
        wallet = WalletRepository.get_wallet_by_id(db_conn, wallet_id)

        if not wallet or wallet.is_deleted:
            raise FintechBaseException(SystemConstants.WALLET_NOT_FOUND, 404)

        if str(wallet.user_id) != str(user_id):
            print(f"🚨 [SECURITY_BREACH] Người dùng {user_id} cố tình đọc trộm ví {wallet_id}! Trace-ID: {trace_id}")
            raise FintechBaseException(SystemConstants.WALLET_FORBIDDEN_ACCESS, 403)

        return WalletService._map_wallet_to_dict(wallet)

    @staticmethod
    def list_user_wallets(db_conn: Any, user_id: str) -> List[Dict[str, Any]]:
        """
        👑 Bổ sung hàm đồng bộ lấy danh sách ví phục vụ endpoint GET /wallets
        """
        wallets = WalletRepository.get_wallets_by_user_id(db_conn, user_id)
        return [WalletService._map_wallet_to_dict(w) for w in wallets]
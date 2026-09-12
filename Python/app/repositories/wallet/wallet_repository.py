import uuid
from datetime import datetime
from typing import List, Optional, Any
from sqlalchemy import select, text
from app.models.wallet.wallet import Wallet
from app.models.notification.notification import Notification
from app.repositories.wallet.wallet_mapping import WalletFactory


class WalletRepository:
    @staticmethod
    def create_wallet(db_conn: Any, user_id: str, wallet_code: str, name: str, currency: str,
                      wallet_type: str = "CASH", color: Optional[str] = None,
                      icon: Optional[str] = None, description: Optional[str] = None) -> Wallet:
        """👑 Đồng bộ Factory Mapping: Ghi nhận tạo thực thể ví mới bằng chuỗi SQL thô"""
        # Đã nhồi thêm tham số db_conn lên đầu để phục vụ đệ quy đối chiếu chuỗi số
        params = WalletFactory.build_params(
            db_conn=db_conn, user_id=user_id, wallet_code=wallet_code, name=name, currency=currency,
            wallet_type=wallet_type, description=description, color=color, icon=icon
        )

        sql_stmt = WalletFactory.get_insert_sql()
        db_conn.execute(text(sql_stmt), params)

        return Wallet(
            id=params["id"],
            user_id=params["user_id"],
            wallet_code=params["wallet_code"],
            name=params["name"],
            wallet_type=params["wallet_type"],
            wallet_account=params["wallet_account"],
            balance=params["balance"],
            currency=params["currency"],
            color=params["color"],
            icon=params["icon"],
            description=params["description"],
            status=params["status"],
            is_deleted=False,
            created_at=params["created_at"],
            updated_at=params["updated_at"]
        )

    @staticmethod
    def get_wallet_by_id(db_conn: Any, wallet_id: str) -> Optional[Wallet]:
        stmt = select(Wallet).where(Wallet.id == wallet_id)
        return db_conn.execute(stmt).scalars().first()

    @staticmethod
    def get_wallets_by_user_id(db_conn: Any, user_id: str) -> List[Wallet]:
        stmt = select(Wallet).where(Wallet.user_id == user_id, Wallet.is_deleted.is_(False)).order_by(
            Wallet.created_at.desc())
        return list(db_conn.execute(stmt).scalars().all())

    @staticmethod
    def insert_wallet_notification(db_conn: Any, user_id: str, title: str, content: str) -> None:
        from sqlalchemy import insert
        stmt = insert(Notification).values(
            id=str(uuid.uuid4()), user_id=user_id, title=title, content=content,
            notification_type="WALLET_CREATION_MAIL", is_read="UNREAD", status="PENDING",
            created_at=datetime.now(), updated_at=datetime.now()
        )
        db_conn.execute(stmt)

    @staticmethod
    def update_wallet(db_conn: Any, wallet_id: str, wallet_code: str, name: str, currency: str,
                      wallet_type: str, color: Optional[str], icon: Optional[str],
                      description: Optional[str]) -> None:
        """👑 Đồng bộ Factory Mapping: Thực thi cập nhật ví bằng chuỗi SQL thô, đóng dấu updated_at"""
        sql_stmt = WalletFactory.get_update_sql()
        params = {
            "id": wallet_id,
            "wallet_code": wallet_code,
            "name": name,
            "wallet_type": wallet_type,
            "description": description,
            "currency": currency.upper() if currency else "VND",
            "color": color,
            "icon": icon,
            "updated_at": datetime.now()
        }
        db_conn.execute(text(sql_stmt), params)

    @staticmethod
    def soft_delete_wallet(db_conn: Any, wallet_id: str) -> None:
        """👑 Đồng bộ Factory Mapping: Thực thi kích nổ lệnh xóa mềm lật flag bằng chuỗi SQL thô"""
        sql_stmt = WalletFactory.get_delete_sql()
        now = datetime.now()
        params = {
            "id": wallet_id,
            "deleted_at": now,
            "updated_at": now
        }
        db_conn.execute(text(sql_stmt), params)
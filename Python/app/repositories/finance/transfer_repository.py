
import uuid
from datetime import datetime
from sqlalchemy import select, desc
from app.models.finance.transfer import Transfer


class TransferRepository:
    """
    👑 REPOSITORY: QUẢN LÝ BẢNG 'transfers'
    """

    @staticmethod
    def list_by_user(db, user_id: str, limit: int = 50) -> list:
        stmt = select(Transfer).where(Transfer.user_id == user_id).order_by(desc(Transfer.transfer_date)).limit(limit)
        records = db.execute(stmt).scalars().all()
        return [
            {
                "id": t.id,
                "user_id": t.user_id,
                "source_wallet_id": t.source_wallet_id,
                "destination_wallet_id": t.destination_wallet_id,
                "amount": float(t.amount),
                "fee": float(t.fee),
                "transfer_date": t.transfer_date.isoformat() if t.transfer_date else "",
                "description": t.description,
                "status": t.status
            }
            for t in records
        ]

    @staticmethod
    def get_by_id(db, transfer_id: str, user_id: str) -> dict:
        stmt = select(Transfer).where(Transfer.id == transfer_id, Transfer.user_id == user_id)
        t = db.execute(stmt).scalars().first()
        if not t:
            return None
        return {
            "id": t.id,
            "user_id": t.user_id,
            "source_wallet_id": t.source_wallet_id,
            "destination_wallet_id": t.destination_wallet_id,
            "amount": float(t.amount),
            "fee": float(t.fee),
            "transfer_date": t.transfer_date.isoformat() if t.transfer_date else "",
            "description": t.description,
            "status": t.status
        }

    @staticmethod
    def insert_transfer(db, user_id: str, source_id: str, dest_id: str,
                        amount: float, fee: float, description: str) -> dict:
        t_id = str(uuid.uuid4())
        now = datetime.now()
        new_transfer = Transfer(
            id=t_id,
            user_id=user_id,
            source_wallet_id=source_id,
            destination_wallet_id=dest_id,
            amount=amount,
            fee=fee,
            transfer_date=now,
            description=description,
            status="COMPLETED",
            created_at=now,
            updated_at=now
        )
        db.add(new_transfer)
        return {
            "id": new_transfer.id,
            "user_id": new_transfer.user_id,
            "source_wallet_id": new_transfer.source_wallet_id,
            "destination_wallet_id": new_transfer.destination_wallet_id,
            "amount": float(new_transfer.amount),
            "fee": float(new_transfer.fee),
            "transfer_date": new_transfer.transfer_date.isoformat(),
            "description": new_transfer.description,
            "status": new_transfer.status
        }

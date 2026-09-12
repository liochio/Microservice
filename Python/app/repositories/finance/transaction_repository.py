# 📄 Đường dẫn file: app/repositories/finance/transaction_repository.py
import uuid
from datetime import datetime
from sqlalchemy import select, and_, desc, text
from app.models.finance.transaction import Transaction


class TransactionRepository:
    """
    👑 REPOSITORY: TRUY VẤN VÀ QUẢN LÝ BẢNG `transactions`
    🎯 Nguyên tắc Bọc Thép: Chỉ thao tác bảng `transactions`.
    """

    @staticmethod
    def list_transactions(db, user_id: str, wallet_id: str = None, category_id: str = None,
                          tx_type: str = None, start_date: datetime = None, end_date: datetime = None,
                          limit: int = 50, offset: int = 0) -> tuple:
        conditions = [Transaction.user_id == user_id, Transaction.status == "COMPLETED"]
        if wallet_id:
            conditions.append(Transaction.wallet_id == wallet_id)
        if category_id:
            conditions.append(Transaction.category_id == category_id)
        if tx_type:
            conditions.append(Transaction.transaction_type == tx_type.upper())
        if start_date:
            conditions.append(Transaction.transaction_date >= start_date)
        if end_date:
            conditions.append(Transaction.transaction_date <= end_date)

        stmt = select(Transaction).where(and_(*conditions)).order_by(desc(Transaction.transaction_date)).limit(limit).offset(offset)
        records = db.execute(stmt).scalars().all()

        items = [
            {
                "id": t.id,
                "user_id": t.user_id,
                "wallet_id": t.wallet_id,
                "category_id": t.category_id,
                "transaction_code": t.transaction_code,
                "amount": float(t.amount),
                "transaction_type": t.transaction_type,
                "transaction_date": t.transaction_date.isoformat() if t.transaction_date else "",
                "description": t.description,
                "balance_before": float(t.balance_before) if t.balance_before is not None else None,
                "balance_after": float(t.balance_after) if t.balance_after is not None else None,
                "status": t.status
            }
            for t in records
        ]
        return items, len(items)

    @staticmethod
    def get_by_id(db, transaction_id: str, user_id: str) -> dict:
        stmt = select(Transaction).where(Transaction.id == transaction_id, Transaction.user_id == user_id)
        t = db.execute(stmt).scalars().first()
        if not t:
            return None
        return {
            "id": t.id,
            "user_id": t.user_id,
            "wallet_id": t.wallet_id,
            "category_id": t.category_id,
            "transaction_code": t.transaction_code,
            "amount": float(t.amount),
            "transaction_type": t.transaction_type,
            "transaction_date": t.transaction_date.isoformat() if t.transaction_date else "",
            "description": t.description,
            "balance_before": float(t.balance_before) if t.balance_before is not None else None,
            "balance_after": float(t.balance_after) if t.balance_after is not None else None,
            "status": t.status
        }

    @staticmethod
    def insert_transaction(db, user_id: str, wallet_id: str, category_id: str,
                           amount: float, tx_type: str, tx_date: datetime,
                           description: str, balance_before: float, balance_after: float) -> dict:
        tx_id = str(uuid.uuid4())
        tx_code = f"TX-{datetime.now().strftime('%Y%m%d%H%M%S')}-{uuid.uuid4().hex[:4].upper()}"
        new_tx = Transaction(
            id=tx_id,
            user_id=user_id,
            wallet_id=wallet_id,
            category_id=category_id,
            transaction_code=tx_code,
            amount=amount,
            transaction_type=tx_type,
            transaction_date=tx_date,
            description=description,
            balance_before=balance_before,
            balance_after=balance_after,
            status="COMPLETED"
        )
        db.add(new_tx)
        return {
            "id": new_tx.id,
            "user_id": new_tx.user_id,
            "wallet_id": new_tx.wallet_id,
            "category_id": new_tx.category_id,
            "transaction_code": new_tx.transaction_code,
            "amount": float(new_tx.amount),
            "transaction_type": new_tx.transaction_type,
            "transaction_date": new_tx.transaction_date.isoformat() if new_tx.transaction_date else "",
            "description": new_tx.description,
            "balance_before": float(new_tx.balance_before),
            "balance_after": float(new_tx.balance_after),
            "status": new_tx.status
        }

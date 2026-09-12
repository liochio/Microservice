# 📄 Đường dẫn file: app/repositories/finance/budget_repository.py
import uuid
from datetime import datetime
from sqlalchemy import select, update, and_, func
from app.models.finance.budget import Budget
from app.models.finance.transaction import Transaction


class BudgetRepository:
    """
    👑 REPOSITORY: QUẢN LÝ BẢNG `budgets`
    """

    @staticmethod
    def list_by_user(db, user_id: str) -> list:
        stmt = select(Budget).where(Budget.user_id == user_id, Budget.status == "ACTIVE")
        records = db.execute(stmt).scalars().all()
        result = []
        for b in records:
            # Tính toán chi tiêu thực tế từ bảng transactions trong khoảng thời gian
            spent_stmt = select(func.sum(Transaction.amount)).where(
                Transaction.user_id == user_id,
                Transaction.category_id == b.category_id,
                Transaction.transaction_type == "EXPENSE",
                Transaction.status == "COMPLETED",
                Transaction.transaction_date >= b.start_date,
                Transaction.transaction_date <= b.end_date
            )
            real_spent = db.execute(spent_stmt).scalar() or 0.0
            limit_val = float(b.amount_limit)
            pct = round((float(real_spent) / limit_val * 100), 2) if limit_val > 0 else 0.0

            result.append({
                "id": b.id,
                "user_id": b.user_id,
                "category_id": b.category_id,
                "category_name": None,
                "amount_limit": limit_val,
                "current_spent": float(real_spent),
                "spent_percentage": pct,
                "is_exceeded": float(real_spent) > limit_val,
                "start_date": b.start_date.isoformat() if b.start_date else "",
                "end_date": b.end_date.isoformat() if b.end_date else "",
                "status": b.status
            })
        return result

    @staticmethod
    def get_by_id(db, budget_id: str, user_id: str) -> dict:
        stmt = select(Budget).where(Budget.id == budget_id, Budget.user_id == user_id, Budget.status == "ACTIVE")
        b = db.execute(stmt).scalars().first()
        if not b:
            return None
        return {
            "id": b.id,
            "user_id": b.user_id,
            "category_id": b.category_id,
            "amount_limit": float(b.amount_limit),
            "current_spent": float(b.current_spent),
            "spent_percentage": round((float(b.current_spent) / float(b.amount_limit) * 100), 2) if float(b.amount_limit) > 0 else 0.0,
            "is_exceeded": float(b.current_spent) > float(b.amount_limit),
            "start_date": b.start_date.isoformat() if b.start_date else "",
            "end_date": b.end_date.isoformat() if b.end_date else "",
            "status": b.status
        }

    @staticmethod
    def insert_budget(db, user_id: str, category_id: str, amount_limit: float, start_date: datetime, end_date: datetime) -> dict:
        b_id = str(uuid.uuid4())
        now = datetime.now()
        new_b = Budget(
            id=b_id,
            user_id=user_id,
            category_id=category_id,
            amount_limit=amount_limit,
            current_spent=0.0,
            start_date=start_date,
            end_date=end_date,
            status="ACTIVE",
            created_at=now,
            updated_at=now
        )
        db.add(new_b)
        db.commit()
        db.refresh(new_b)
        return {
            "id": new_b.id,
            "user_id": new_b.user_id,
            "category_id": new_b.category_id,
            "amount_limit": float(new_b.amount_limit),
            "current_spent": 0.0,
            "spent_percentage": 0.0,
            "is_exceeded": False,
            "start_date": new_b.start_date.isoformat(),
            "end_date": new_b.end_date.isoformat(),
            "status": new_b.status
        }

    @staticmethod
    def update_budget(db, budget_id: str, user_id: str, amount_limit: float) -> dict:
        stmt = select(Budget).where(Budget.id == budget_id, Budget.user_id == user_id)
        b = db.execute(stmt).scalars().first()
        if not b:
            return None
        b.amount_limit = amount_limit
        b.updated_at = datetime.now()
        db.commit()
        return BudgetRepository.get_by_id(db, budget_id, user_id)

    @staticmethod
    def delete_budget(db, budget_id: str, user_id: str) -> bool:
        stmt = select(Budget).where(Budget.id == budget_id, Budget.user_id == user_id)
        b = db.execute(stmt).scalars().first()
        if not b:
            return False
        b.status = "DELETED"
        b.updated_at = datetime.now()
        db.commit()
        return True

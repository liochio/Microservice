# 📄 Đường dẫn file: app/repositories/finance/financial_goal_repository.py
import uuid
from datetime import datetime
from typing import List, Optional
from sqlalchemy.orm import Session
from app.models.finance.financial_goal import FinancialGoal
from app.models.smart_piggy.smart_piggy_goal import SmartPiggyGoal


class FinancialGoalRepository:
    """Repository quản lý bảng financial_goals và smart_piggy_goals"""

    @staticmethod
    def create_goal(db: Session, user_id: str, name: str, target_amount: float, deadline: Optional[datetime] = None, smart_piggy_device_id: Optional[str] = None) -> FinancialGoal:
        goal_id = str(uuid.uuid4())
        new_goal = FinancialGoal(
            id=goal_id,
            user_id=user_id,
            name=name,
            target_amount=target_amount,
            current_amount=0.0,
            deadline=deadline,
            status="ACTIVE"
        )
        db.add(new_goal)

        if smart_piggy_device_id:
            piggy_goal = SmartPiggyGoal(
                id=str(uuid.uuid4()),
                smart_piggy_device_id=smart_piggy_device_id,
                goal_name=name,
                target_amount=target_amount,
                current_amount=0.0,
                deadline=deadline,
                status="ACTIVE"
            )
            db.add(piggy_goal)

        db.commit()
        db.refresh(new_goal)
        return new_goal

    @staticmethod
    def get_by_id(db: Session, goal_id: str, user_id: str) -> Optional[FinancialGoal]:
        return db.query(FinancialGoal).filter(FinancialGoal.id == goal_id, FinancialGoal.user_id == user_id).first()

    @staticmethod
    def list_user_goals(db: Session, user_id: str) -> List[FinancialGoal]:
        return db.query(FinancialGoal).filter(FinancialGoal.user_id == user_id).order_by(FinancialGoal.created_at.desc()).all()

    @staticmethod
    def update_goal(db: Session, goal: FinancialGoal, name: Optional[str], target_amount: Optional[float], deadline: Optional[datetime], status: Optional[str]) -> FinancialGoal:
        if name is not None:
            goal.name = name
        if target_amount is not None:
            goal.target_amount = target_amount
        if deadline is not None:
            goal.deadline = deadline
        if status is not None:
            goal.status = status
        goal.updated_at = datetime.now()
        db.commit()
        db.refresh(goal)
        return goal

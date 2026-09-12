# 📄 Đường dẫn file: app/services/finance/financial_goal_service.py
from sqlalchemy.orm import Session
from typing import List, Optional
from datetime import datetime
import uuid

from app.constants import SystemConstants
from app.core.exceptions.base_exception import FintechBaseException
from app.repositories.finance.financial_goal_repository import FinancialGoalRepository
from app.schemas.requests.financial_goal import FinancialGoalCreateRequest, FinancialGoalUpdateRequest, GoalLockRequest
from app.models.wallet.wallet import Wallet
from app.models.smart_piggy.smart_piggy_device import SmartPiggyDevice


class FinancialGoalService:
    """👑 FINANCIAL GOAL & PIGGY LOCKING SERVICE"""

    @staticmethod
    def create_goal(db: Session, user_id: str, payload: FinancialGoalCreateRequest) -> dict:
        goal = FinancialGoalRepository.create_goal(
            db=db,
            user_id=user_id,
            name=payload.name,
            target_amount=payload.target_amount,
            deadline=payload.deadline,
            smart_piggy_device_id=payload.smart_piggy_device_id
        )
        return FinancialGoalService._serialize_goal(goal)

    @staticmethod
    def get_user_goals(db: Session, user_id: str) -> List[dict]:
        goals = FinancialGoalRepository.list_user_goals(db, user_id)
        # Đồng bộ current_amount từ tổng số dư ví Heo đất (nếu có)
        piggy_devices = db.query(SmartPiggyDevice).filter(SmartPiggyDevice.user_id == user_id).all()
        total_piggy_savings = sum(float(d.total_coins_dropped) for d in piggy_devices)

        results = []
        for g in goals:
            data = FinancialGoalService._serialize_goal(g, total_piggy_savings)
            results.append(data)
        return results

    @staticmethod
    def get_goal_detail(db: Session, user_id: str, goal_id: str) -> dict:
        goal = FinancialGoalRepository.get_by_id(db, goal_id, user_id)
        if not goal:
            raise FintechBaseException(error_code=SystemConstants.TRANSACTION_NOT_FOUND, status_code=404)
        return FinancialGoalService._serialize_goal(goal)

    @staticmethod
    def update_goal(db: Session, user_id: str, goal_id: str, payload: FinancialGoalUpdateRequest) -> dict:
        goal = FinancialGoalRepository.get_by_id(db, goal_id, user_id)
        if not goal:
            raise FintechBaseException(error_code=SystemConstants.TRANSACTION_NOT_FOUND, status_code=404)
        updated = FinancialGoalRepository.update_goal(
            db=db,
            goal=goal,
            name=payload.name,
            target_amount=payload.target_amount,
            deadline=payload.deadline,
            status=payload.status
        )
        return FinancialGoalService._serialize_goal(updated)

    @staticmethod
    def set_goal_lock(db: Session, user_id: str, goal_id: str, payload: GoalLockRequest) -> dict:
        """Khóa hoặc mở khóa Heo đất gắn với mục tiêu để rèn kỷ luật tài chính"""
        goal = FinancialGoalRepository.get_by_id(db, goal_id, user_id)
        if not goal:
            raise FintechBaseException(error_code=SystemConstants.TRANSACTION_NOT_FOUND, status_code=404)
        
        goal.status = "LOCKED" if payload.is_locked else "ACTIVE"
        goal.updated_at = datetime.now()
        db.commit()
        db.refresh(goal)
        return FinancialGoalService._serialize_goal(goal)

    @staticmethod
    def _serialize_goal(goal, dynamic_current_amount: Optional[float] = None) -> dict:
        target = float(goal.target_amount) if goal.target_amount else 1.0
        current = dynamic_current_amount if dynamic_current_amount is not None else float(goal.current_amount)
        progress = round(min(100.0, (current / target) * 100.0), 1) if target > 0 else 0.0

        return {
            "id": goal.id,
            "user_id": goal.user_id,
            "name": goal.name,
            "target_amount": target,
            "current_amount": current,
            "progress_percentage": progress,
            "deadline": goal.deadline,
            "is_locked": (goal.status == "LOCKED"),
            "status": goal.status,
            "created_at": goal.created_at,
            "updated_at": goal.updated_at
        }

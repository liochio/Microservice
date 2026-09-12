# 📄 Đường dẫn file: app/schemas/responses/financial_goal.py
from pydantic import BaseModel
from typing import Optional, List
from datetime import datetime
from app.schemas.responses.base_response import BaseResponse


class FinancialGoalItem(BaseModel):
    id: str
    user_id: str
    name: str
    target_amount: float
    current_amount: float
    progress_percentage: float
    deadline: Optional[datetime] = None
    is_locked: bool = False
    status: str
    created_at: Optional[datetime] = None
    updated_at: Optional[datetime] = None


class FinancialGoalResponse(BaseResponse):
    data: Optional[FinancialGoalItem] = None


class FinancialGoalListResponse(BaseResponse):
    data: List[FinancialGoalItem] = []
    total_goals: int = 0

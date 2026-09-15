
from pydantic import BaseModel
from typing import List, Optional, Any


class BudgetItem(BaseModel):
    id: str
    user_id: str
    category_id: str
    category_name: Optional[str] = None
    amount_limit: float
    current_spent: float = 0.0
    spent_percentage: float = 0.0
    is_exceeded: bool = False
    start_date: str
    end_date: str
    status: str = "ACTIVE"


class BudgetListResponse(BaseModel):
    success: bool = True
    error_code: str = "BUDGET_FETCH_SUCCESS"
    message: str
    data: List[BudgetItem]
    trace_id: Optional[str] = None


class BudgetDetailResponse(BaseModel):
    success: bool = True
    error_code: str = "BUDGET_CREATE_SUCCESS"
    message: str
    data: BudgetItem
    trace_id: Optional[str] = None

# 📄 Đường dẫn file: app/schemas/requests/financial_goal.py
from pydantic import BaseModel, Field
from typing import Optional
from datetime import datetime


class FinancialGoalCreateRequest(BaseModel):
    name: str = Field(..., min_length=2, max_length=255, description="Tên mục tiêu tích lũy")
    target_amount: float = Field(..., gt=0, description="Số tiền mục tiêu (VND)")
    deadline: Optional[datetime] = Field(None, description="Hạn chót hoàn thành mục tiêu")
    smart_piggy_device_id: Optional[str] = Field(None, description="ID thiết bị Heo đất liên kết (nếu có)")


class FinancialGoalUpdateRequest(BaseModel):
    name: Optional[str] = Field(None, min_length=2, max_length=255)
    target_amount: Optional[float] = Field(None, gt=0)
    deadline: Optional[datetime] = None
    status: Optional[str] = Field(None, description="ACTIVE, COMPLETED, CANCELLED, LOCKED")


class GoalLockRequest(BaseModel):
    is_locked: bool = Field(True, description="True: Khóa Heo đất chống rút tiền, False: Mở khóa")
    reason: Optional[str] = Field(None, description="Lý do khóa mục tiêu")

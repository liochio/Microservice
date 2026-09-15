
from pydantic import BaseModel
from typing import Optional, List
from datetime import datetime
from app.schemas.responses.base_response import BaseResponse


class ParentMatchingRuleItem(BaseModel):
    id: str
    parent_user_id: str
    child_user_id: str
    matching_percentage: float
    max_monthly_bonus: float
    current_monthly_bonus: float
    is_active: bool
    created_at: Optional[datetime] = None


class ParentMatchingRuleResponse(BaseResponse):
    data: Optional[ParentMatchingRuleItem] = None


class FamilyMemberSavings(BaseModel):
    user_id: str
    user_name: str
    role: str
    total_piggy_saved: float
    total_bonus_received: float
    piggy_level: int
    piggy_title: str


class FamilyDashboardResponse(BaseResponse):
    total_family_savings: float = 0.0
    total_parent_bonus_granted: float = 0.0
    members: List[FamilyMemberSavings] = []

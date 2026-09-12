# 📄 Đường dẫn file: app/schemas/requests/parent_matching.py
from pydantic import BaseModel, Field
from typing import Optional


class ParentMatchingRuleCreateRequest(BaseModel):
    child_user_id: str = Field(..., description="ID tài khoản của con")
    matching_percentage: float = Field(50.0, ge=0.0, le=200.0, description="Tỷ lệ cha mẹ thưởng thêm (% theo số tiền con đút heo)")
    max_monthly_bonus: float = Field(1000000.0, gt=0, description="Hạn mức thưởng tối đa mỗi tháng (VND)")
    parent_wallet_id: Optional[str] = Field(None, description="Ví nguồn của cha mẹ để trích thưởng")
    is_active: bool = Field(True, description="Trạng thái kích hoạt")

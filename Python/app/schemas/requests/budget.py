# 📄 Đường dẫn file: app/schemas/requests/budget.py
from pydantic import BaseModel, model_validator
from typing import Optional, Any
from datetime import datetime
from decimal import Decimal
from app.constants import SystemConstants
from app.core.exceptions.base_exception import FintechBaseException


class CreateBudgetRequest(BaseModel):
    """👑 DTO TẠO NGÂN SÁCH CHI TIÊU"""
    category_id: Optional[Any] = None
    amount_limit: Optional[Any] = None
    start_date: Optional[Any] = None
    end_date: Optional[Any] = None

    @model_validator(mode="after")
    def validate_budget(self):
        if not self.category_id:
            raise FintechBaseException(error_code="MISSING_CATEGORY_ID", status_code=400)

        if self.amount_limit is None:
            raise FintechBaseException(error_code="INVALID_BUDGET_AMOUNT", status_code=400)

        try:
            val = Decimal(str(self.amount_limit))
            if val <= Decimal("0"):
                raise FintechBaseException(error_code="INVALID_BUDGET_AMOUNT", status_code=400)
            self.amount_limit = float(val)
        except Exception:
            raise FintechBaseException(error_code="INVALID_BUDGET_AMOUNT", status_code=400)

        if not self.start_date:
            self.start_date = datetime.now().replace(day=1, hour=0, minute=0, second=0)
        if not self.end_date:
            self.end_date = datetime.now().replace(day=28, hour=23, minute=59, second=59)

        return self


class UpdateBudgetRequest(BaseModel):
    """👑 DTO ĐIỀU CHỈNH HẠN MỨC NGÂN SÁCH"""
    amount_limit: Optional[Any] = None

    @model_validator(mode="after")
    def validate_amount(self):
        if self.amount_limit is not None:
            try:
                val = Decimal(str(self.amount_limit))
                if val <= Decimal("0"):
                    raise FintechBaseException(error_code="INVALID_BUDGET_AMOUNT", status_code=400)
                self.amount_limit = float(val)
            except Exception:
                raise FintechBaseException(error_code="INVALID_BUDGET_AMOUNT", status_code=400)
        return self

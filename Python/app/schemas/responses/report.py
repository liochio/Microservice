
from pydantic import BaseModel
from typing import Optional, List, Dict
from app.schemas.responses.base_response import BaseResponse


class CashFlowPoint(BaseModel):
    date: str
    total_income: float
    total_expense: float
    net_savings: float


class CashFlowReportResponse(BaseResponse):
    period: str = "30_DAYS"
    total_income: float = 0.0
    total_expense: float = 0.0
    net_savings: float = 0.0
    daily_cash_flows: List[CashFlowPoint] = []


class CategoryBreakdownItem(BaseModel):
    category_id: Optional[str] = None
    category_name: str
    category_type: str
    total_amount: float
    percentage: float
    color: Optional[str] = None


class CategoryBreakdownResponse(BaseResponse):
    total_expense: float = 0.0
    categories: List[CategoryBreakdownItem] = []


class FinancialSummaryResponse(BaseResponse):
    total_wallets_balance: float = 0.0
    total_piggy_savings: float = 0.0
    monthly_income: float = 0.0
    monthly_expense: float = 0.0
    savings_rate_percentage: float = 0.0
    financial_health_score: int = 100

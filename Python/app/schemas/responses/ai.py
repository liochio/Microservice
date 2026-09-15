
from pydantic import BaseModel
from typing import List, Optional, Any


class AiScoreData(BaseModel):
    user_id: str
    financial_health_score: int  # 0 - 100
    rating: str                  # EXCELLENT, GOOD, FAIR, POOR
    monthly_income: float
    monthly_expense: float
    savings_rate: float          # %
    analysis_summary: str


class AiScoreResponse(BaseModel):
    success: bool = True
    error_code: str = "AI_SCORE_CALCULATE_SUCCESS"
    message: str
    data: AiScoreData
    trace_id: Optional[str] = None


class AiRecommendationItem(BaseModel):
    category: str
    severity: str  # HIGH, MEDIUM, LOW
    suggestion: str
    potential_savings: float


class AiRecommendationResponse(BaseModel):
    success: bool = True
    error_code: str = "AI_RECOMMENDATION_SUCCESS"
    message: str
    data: List[AiRecommendationItem]
    trace_id: Optional[str] = None

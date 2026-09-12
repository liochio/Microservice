# 📄 Đường dẫn file: app/schemas/responses/smart_piggy.py
from pydantic import BaseModel, Field
from typing import Optional, List, Dict, Any
from datetime import datetime


class PiggyDeviceItem(BaseModel):
    id: str
    mac_address: str
    device_name: str
    wallet_id: str
    total_coins_dropped: float
    status: str
    created_at: datetime
    updated_at: datetime


class PiggyDeviceListResponse(BaseModel):
    success: bool
    error_code: str
    message: str
    data: List[PiggyDeviceItem]
    trace_id: Optional[str] = None


class PiggyDeviceDetailResponse(BaseModel):
    success: bool
    error_code: str
    message: str
    data: PiggyDeviceItem
    trace_id: Optional[str] = None


class PiggyDropMoneyData(BaseModel):
    device_id: str
    device_name: str
    wallet_id: str
    coin_value_deposited: float
    parent_matching_bonus: float = 0.0
    total_credited: float
    new_wallet_balance: float
    gamification_points_earned: int
    current_total_points: int
    current_level: int
    level_title: str
    hardware_command: Dict[str, Any] = Field(
        default_factory=lambda: {
            "led_rgb": "#00FF00",
            "led_effect": "HAPPY_PULSE",
            "buzzer_beeps": 2,
            "display_text": "OINK! +50,000"
        }
    )


class PiggyDropMoneyResponse(BaseModel):
    success: bool
    error_code: str
    message: str
    data: PiggyDropMoneyData
    trace_id: Optional[str] = None


class PiggyCoinLogItem(BaseModel):
    id: str
    coin_value: float
    status: str
    created_at: datetime


class PiggyHistoryResponse(BaseModel):
    success: bool
    error_code: str
    message: str
    data: List[PiggyCoinLogItem]
    total_drops: int
    total_amount: float
    trace_id: Optional[str] = None


class PiggyAiForecastData(BaseModel):
    device_id: str
    current_balance: float
    target_amount: float
    progress_percentage: float
    average_daily_saving: float
    estimated_days_remaining: int
    estimated_completion_date: str
    confidence_score: float
    ai_financial_advice: str


class PiggyAiForecastResponse(BaseModel):
    success: bool
    error_code: str
    message: str
    data: PiggyAiForecastData
    trace_id: Optional[str] = None


class PiggyAiBehaviorData(BaseModel):
    user_id: str
    saving_streak_days: int
    most_frequent_day_of_week: str
    favorite_coin_denomination: float
    saving_consistency_score: int
    behavior_persona: str
    ai_recommendation_tips: List[str]


class PiggyAiBehaviorResponse(BaseModel):
    success: bool
    error_code: str
    message: str
    data: PiggyAiBehaviorData
    trace_id: Optional[str] = None


class PiggyGamificationStatusData(BaseModel):
    user_id: str
    current_level: int
    level_name: str
    current_points: int
    points_to_next_level: int
    streak_days: int
    badges_unlocked: List[Dict[str, Any]]
    ranking_title: str


class PiggyGamificationResponse(BaseModel):
    success: bool
    error_code: str
    message: str
    data: PiggyGamificationStatusData
    trace_id: Optional[str] = None

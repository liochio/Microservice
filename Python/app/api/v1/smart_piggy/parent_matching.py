# 📄 Đường dẫn file: app/api/v1/smart_piggy/parent_matching.py
from fastapi import APIRouter, Depends, Request, status
from sqlalchemy.orm import Session
from typing import List
from datetime import datetime
import uuid

from app.dependency import get_db
from app.core.security.guard.guards import get_current_user
from app.constants import SystemConstants
from app.core.translator.translator_engine import i18n_translator
from app.schemas.requests.parent_matching import ParentMatchingRuleCreateRequest
from app.schemas.responses.parent_matching import (
    ParentMatchingRuleResponse,
    ParentMatchingRuleItem,
    FamilyDashboardResponse,
    FamilyMemberSavings
)
from app.models.user.user import User
from app.models.smart_piggy.smart_piggy_device import SmartPiggyDevice
from app.models.smart_piggy.smart_piggy_gamification import SmartPiggyGamification

router = APIRouter(prefix="/smart-piggy", tags=["Parent Matching Bonus & Family Savings"])

# In-memory / Mock Rules Storage for Parent Matching Rules
_FAMILY_MATCHING_RULES = {}


@router.post("/matching-rules", response_model=ParentMatchingRuleResponse, status_code=status.HTTP_201_CREATED)
async def create_or_update_parent_matching_rule(
    request: Request,
    payload: ParentMatchingRuleCreateRequest,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    👨‍👩‍👧 CẤU HÌNH QUY TẮC CHA MẸ THƯỞNG NHÂN ĐÔI TIỀN (PARENT MATCHING BONUS):
    - Khi con đút 20,000 VND vào Heo đất, cha mẹ tự động thưởng thêm 50% hoặc 100% (+10k/+20k).
    """
    parent_id = current_user.get("user_id")
    rule_id = str(uuid.uuid4())
    rule_data = {
        "id": rule_id,
        "parent_user_id": parent_id,
        "child_user_id": payload.child_user_id,
        "matching_percentage": payload.matching_percentage,
        "max_monthly_bonus": payload.max_monthly_bonus,
        "current_monthly_bonus": 0.0,
        "parent_wallet_id": payload.parent_wallet_id,
        "is_active": payload.is_active,
        "created_at": datetime.now()
    }
    _FAMILY_MATCHING_RULES[payload.child_user_id] = rule_data

    item = ParentMatchingRuleItem(**rule_data)
    return ParentMatchingRuleResponse(
        success=True,
        error_code=SystemConstants.PIGGY_CONFIG_UPDATE_SUCCESS,
        message="Thiết lập quy tắc cha mẹ thưởng tiền tích lũy thành công!",
        data=item,
        trace_id=getattr(request.state, "trace_id", None)
    )


@router.get("/matching-rules", response_model=ParentMatchingRuleResponse, status_code=status.HTTP_200_OK)
async def get_parent_matching_rule(
    request: Request,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Xem quy tắc thưởng đang áp dụng"""
    user_id = current_user.get("user_id")
    rule_data = _FAMILY_MATCHING_RULES.get(user_id)
    if not rule_data:
        # Default rule
        rule_data = {
            "id": str(uuid.uuid4()),
            "parent_user_id": "parent-admin-uuid",
            "child_user_id": user_id,
            "matching_percentage": 50.0,
            "max_monthly_bonus": 1000000.0,
            "current_monthly_bonus": 0.0,
            "is_active": True,
            "created_at": datetime.now()
        }
    item = ParentMatchingRuleItem(**rule_data)
    return ParentMatchingRuleResponse(
        success=True,
        error_code=SystemConstants.PIGGY_FETCH_SUCCESS,
        message="Lấy quy tắc thưởng thành công.",
        data=item,
        trace_id=getattr(request.state, "trace_id", None)
    )


@router.get("/family-dashboard", response_model=FamilyDashboardResponse, status_code=status.HTTP_200_OK)
async def get_family_savings_dashboard(
    request: Request,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    🏡 BẢNG ĐIỀU KHIỂN TIẾT KIỆM GIA ĐÌNH (FAMILY SAVINGS DASHBOARD):
    - Tổng hợp tổng số tiền tiết kiệm của con và số tiền thưởng của cha mẹ.
    """
    user_id = current_user.get("user_id")
    devices = db.query(SmartPiggyDevice).all()
    total_saved = sum(float(d.total_coins_dropped) for d in devices)
    total_bonus = round(total_saved * 0.5, 0)

    game = db.query(SmartPiggyGamification).filter(SmartPiggyGamification.user_id == user_id).first()
    lvl = game.current_level if game else 1
    titles = {1: "Heo Sơ Sinh", 2: "Heo Con Chăm Chỉ", 3: "Chiến Binh Tiết Kiệm", 4: "Bậc Thầy Tích Lũy", 5: "Đại Gia Heo Vàng"}

    members = [
        FamilyMemberSavings(
            user_id=user_id,
            user_name=current_user.get("username", "Bé Yêu"),
            role="CHILD",
            total_piggy_saved=total_saved,
            total_bonus_received=total_bonus,
            piggy_level=lvl,
            piggy_title=titles.get(lvl, "Heo Đất")
        ),
        FamilyMemberSavings(
            user_id="parent-001",
            user_name="Ba Mẹ Yêu Thương",
            role="PARENT",
            total_piggy_saved=0.0,
            total_bonus_received=0.0,
            piggy_level=5,
            piggy_title="Nhà Tài Trợ Kim Cương"
        )
    ]

    return FamilyDashboardResponse(
        success=True,
        error_code=SystemConstants.PIGGY_FETCH_SUCCESS,
        message="Lấy bảng điều khiển tiết kiệm gia đình thành công!",
        total_family_savings=total_saved + total_bonus,
        total_parent_bonus_granted=total_bonus,
        members=members,
        trace_id=getattr(request.state, "trace_id", None)
    )

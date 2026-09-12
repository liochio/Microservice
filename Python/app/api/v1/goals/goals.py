# 📄 Đường dẫn file: app/api/v1/goals/goals.py
from fastapi import APIRouter, Depends, Request, status
from sqlalchemy.orm import Session
from typing import List

from app.dependency import get_db
from app.core.security.guard.guards import get_current_user
from app.constants import SystemConstants
from app.core.translator.translator_engine import i18n_translator
from app.schemas.requests.financial_goal import FinancialGoalCreateRequest, FinancialGoalUpdateRequest, GoalLockRequest
from app.schemas.responses.financial_goal import FinancialGoalResponse, FinancialGoalListResponse
from app.services.finance.financial_goal_service import FinancialGoalService

router = APIRouter(prefix="/goals", tags=["Financial Goals & Piggy Locking"])


@router.post("", response_model=FinancialGoalResponse, status_code=status.HTTP_201_CREATED)
async def create_financial_goal(
    request: Request,
    payload: FinancialGoalCreateRequest,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Tạo mục tiêu tích lũy tài chính mới (Mua laptop, du lịch, tiết kiệm...)"""
    user_id = current_user.get("user_id")
    goal = FinancialGoalService.create_goal(db, user_id, payload)
    msg = i18n_translator.translate(request, SystemConstants.TRANSACTION_CREATE_SUCCESS, msg_type=SystemConstants.MSG_TYPE_MESSAGE)
    return FinancialGoalResponse(
        success=True,
        error_code=SystemConstants.TRANSACTION_CREATE_SUCCESS,
        message="Tạo mục tiêu tài chính thành công!",
        data=goal,
        trace_id=getattr(request.state, "trace_id", None)
    )


@router.get("", response_model=FinancialGoalListResponse, status_code=status.HTTP_200_OK)
async def list_financial_goals(
    request: Request,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Lấy danh sách các mục tiêu tích lũy và % tiến độ hoàn thành"""
    user_id = current_user.get("user_id")
    goals = FinancialGoalService.get_user_goals(db, user_id)
    msg = i18n_translator.translate(request, SystemConstants.TRANSACTION_FETCH_SUCCESS, msg_type=SystemConstants.MSG_TYPE_MESSAGE)
    return FinancialGoalListResponse(
        success=True,
        error_code=SystemConstants.TRANSACTION_FETCH_SUCCESS,
        message=msg,
        data=goals,
        total_goals=len(goals),
        trace_id=getattr(request.state, "trace_id", None)
    )


@router.get("/{goal_id}", response_model=FinancialGoalResponse, status_code=status.HTTP_200_OK)
async def get_financial_goal_detail(
    goal_id: str,
    request: Request,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Xem chi tiết 1 mục tiêu tài chính"""
    user_id = current_user.get("user_id")
    goal = FinancialGoalService.get_goal_detail(db, user_id, goal_id)
    msg = i18n_translator.translate(request, SystemConstants.TRANSACTION_FETCH_SUCCESS, msg_type=SystemConstants.MSG_TYPE_MESSAGE)
    return FinancialGoalResponse(
        success=True,
        error_code=SystemConstants.TRANSACTION_FETCH_SUCCESS,
        message=msg,
        data=goal,
        trace_id=getattr(request.state, "trace_id", None)
    )


@router.put("/{goal_id}", response_model=FinancialGoalResponse, status_code=status.HTTP_200_OK)
async def update_financial_goal(
    goal_id: str,
    request: Request,
    payload: FinancialGoalUpdateRequest,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Cập nhật mục tiêu tài chính"""
    user_id = current_user.get("user_id")
    goal = FinancialGoalService.update_goal(db, user_id, goal_id, payload)
    return FinancialGoalResponse(
        success=True,
        error_code=SystemConstants.PIGGY_CONFIG_UPDATE_SUCCESS,
        message="Cập nhật mục tiêu tài chính thành công!",
        data=goal,
        trace_id=getattr(request.state, "trace_id", None)
    )


@router.post("/{goal_id}/lock", response_model=FinancialGoalResponse, status_code=status.HTTP_200_OK)
async def lock_unlock_goal_piggy(
    goal_id: str,
    request: Request,
    payload: GoalLockRequest,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    🔒 CƠ CHẾ KHÓA HEO ĐẤT (PIGGY LOCK):
    - Khóa không cho phép rút tiền từ Heo đất cho đến khi đạt 100% mục tiêu để rèn tính kỷ luật.
    """
    user_id = current_user.get("user_id")
    goal = FinancialGoalService.set_goal_lock(db, user_id, goal_id, payload)
    action_str = "Khóa" if payload.is_locked else "Mở khóa"
    return FinancialGoalResponse(
        success=True,
        error_code=SystemConstants.PIGGY_CONFIG_UPDATE_SUCCESS,
        message=f"{action_str} Heo đất tích lũy thành công!",
        data=goal,
        trace_id=getattr(request.state, "trace_id", None)
    )

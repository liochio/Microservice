# 📄 Đường dẫn file: app/api/v1/budgets/budget.py
from fastapi import APIRouter, Depends, Request, status
from sqlalchemy.orm import Session
from app.dependency import get_db
from app.constants import SystemConstants
from app.core.security.guard.guards import get_current_user
from app.core.translator.translator_engine import i18n_translator
from app.schemas.requests.budget import CreateBudgetRequest, UpdateBudgetRequest
from app.schemas.responses.budget import BudgetListResponse, BudgetDetailResponse
from app.schemas.responses.user import UserActionResponse
from app.services.finance.budget_service import BudgetService

router = APIRouter(prefix="/budgets", tags=["Budgets"])


@router.get("", response_model=BudgetListResponse, status_code=status.HTTP_200_OK)
async def list_budgets(
    request: Request,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    🎯 Lấy danh sách ngân sách chi tiêu trong tháng:
       - Tự động tính toán số tiền đã tiêu thực tế và % tiến độ ngân sách.
    """
    user_id = current_user.get("user_id")
    budgets = BudgetService.get_user_budgets(db, user_id)
    msg = i18n_translator.translate(request, SystemConstants.BUDGET_FETCH_SUCCESS, msg_type=SystemConstants.MSG_TYPE_MESSAGE)
    return BudgetListResponse(
        success=True,
        error_code=SystemConstants.BUDGET_FETCH_SUCCESS,
        message=msg,
        data=budgets,
        trace_id=getattr(request.state, "trace_id", None)
    )


@router.post("", response_model=BudgetDetailResponse, status_code=status.HTTP_201_CREATED)
async def create_budget(
    request: Request,
    payload: CreateBudgetRequest,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    🎯 Thiết lập hạn mức ngân sách mới cho một danh mục:
       - Ví dụ: Ngân sách Ăn uống tối đa 3,000,000đ/tháng.
    """
    user_id = current_user.get("user_id")
    new_b = BudgetService.create_budget(db, user_id, payload)
    msg = i18n_translator.translate(request, SystemConstants.BUDGET_CREATE_SUCCESS, msg_type=SystemConstants.MSG_TYPE_MESSAGE)
    return BudgetDetailResponse(
        success=True,
        error_code=SystemConstants.BUDGET_CREATE_SUCCESS,
        message=msg,
        data=new_b,
        trace_id=getattr(request.state, "trace_id", None)
    )


@router.put("/{budget_id}", response_model=BudgetDetailResponse, status_code=status.HTTP_200_OK)
async def update_budget(
    budget_id: str,
    request: Request,
    payload: UpdateBudgetRequest,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    🎯 Điều chỉnh hạn mức ngân sách chi tiêu.
    """
    user_id = current_user.get("user_id")
    updated = BudgetService.update_budget(db, user_id, budget_id, payload)
    msg = i18n_translator.translate(request, SystemConstants.BUDGET_UPDATE_SUCCESS, msg_type=SystemConstants.MSG_TYPE_MESSAGE)
    return BudgetDetailResponse(
        success=True,
        error_code=SystemConstants.BUDGET_UPDATE_SUCCESS,
        message=msg,
        data=updated,
        trace_id=getattr(request.state, "trace_id", None)
    )


@router.delete("/{budget_id}", response_model=UserActionResponse, status_code=status.HTTP_200_OK)
async def delete_budget(
    budget_id: str,
    request: Request,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    🎯 Xóa ngân sách chi tiêu.
    """
    user_id = current_user.get("user_id")
    BudgetService.delete_budget(db, user_id, budget_id)
    msg = i18n_translator.translate(request, SystemConstants.BUDGET_DELETE_SUCCESS, msg_type=SystemConstants.MSG_TYPE_MESSAGE)
    return UserActionResponse(
        success=True,
        error_code=SystemConstants.BUDGET_DELETE_SUCCESS,
        message=msg,
        trace_id=getattr(request.state, "trace_id", None)
    )

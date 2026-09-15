
from fastapi import APIRouter, Depends, Request, status
from sqlalchemy.orm import Session
from app.dependency import get_db
from app.constants import SystemConstants
from app.core.security.guard.guards import get_current_user
from app.core.translator.translator_engine import i18n_translator
from app.schemas.requests.category import CreateCategoryRequest
from app.schemas.responses.category import CategoryListResponse, CategoryDetailResponse
from app.services.finance.category_service import CategoryService

router = APIRouter(prefix="/categories", tags=["Categories"])


@router.get("", response_model=CategoryListResponse, status_code=status.HTTP_200_OK)
async def list_categories(
    request: Request,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    🎯 Lấy toàn bộ danh mục Thu/Chi (Hệ thống + Danh mục cá nhân):
       - Trả về danh sách phân loại EXPENSE và INCOME kèm icon, màu sắc.
    """
    user_id = current_user.get("user_id")
    categories = CategoryService.get_categories(db, user_id)
    message = i18n_translator.translate(request, SystemConstants.CATEGORY_FETCH_SUCCESS, msg_type=SystemConstants.MSG_TYPE_MESSAGE)
    return CategoryListResponse(
        success=True,
        error_code=SystemConstants.CATEGORY_FETCH_SUCCESS,
        message=message,
        data=categories,
        trace_id=getattr(request.state, "trace_id", None)
    )


@router.post("", response_model=CategoryDetailResponse, status_code=status.HTTP_201_CREATED)
async def create_user_category(
    request: Request,
    payload: CreateCategoryRequest,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    🎯 Tạo danh mục chi tiêu cá nhân mới:
       - Phân loại: EXPENSE (Chi tiêu) hoặc INCOME (Thu nhập).
    """
    user_id = current_user.get("user_id")
    new_cat = CategoryService.create_category(db, user_id, payload)
    message = i18n_translator.translate(request, SystemConstants.CATEGORY_CREATE_SUCCESS, msg_type=SystemConstants.MSG_TYPE_MESSAGE)
    return CategoryDetailResponse(
        success=True,
        error_code=SystemConstants.CATEGORY_CREATE_SUCCESS,
        message=message,
        data=new_cat,
        trace_id=getattr(request.state, "trace_id", None)
    )

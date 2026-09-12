# 📄 Đường dẫn file: app/api/v1/transactions/transaction.py
from fastapi import APIRouter, Depends, Request, Query, status
from sqlalchemy.orm import Session
from typing import Optional
from app.dependency import get_db
from app.constants import SystemConstants
from app.core.security.guard.guards import get_current_user
from app.core.translator.translator_engine import i18n_translator
from app.schemas.requests.transaction import CreateTransactionRequest
from app.schemas.responses.transaction import TransactionListResponse, TransactionDetailResponse
from app.schemas.responses.user import UserActionResponse
from app.services.finance.transaction_service import TransactionService

router = APIRouter(prefix="/transactions", tags=["Transactions"])


@router.get("", response_model=TransactionListResponse, status_code=status.HTTP_200_OK)
async def list_transactions(
    request: Request,
    wallet_id: Optional[str] = Query(None, description="Lọc theo mã ví"),
    category_id: Optional[str] = Query(None, description="Lọc theo danh mục"),
    type: Optional[str] = Query(None, description="Lọc theo loại EXPENSE / INCOME"),
    start_date: Optional[str] = Query(None, description="Từ ngày (YYYY-MM-DD)"),
    end_date: Optional[str] = Query(None, description="Đến ngày (YYYY-MM-DD)"),
    limit: int = Query(50, ge=1, le=100),
    offset: int = Query(0, ge=0),
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    🎯 Lấy danh sách lịch sử giao dịch Thu/Chi:
       - Hỗ trợ lọc theo ví, danh mục, khoảng ngày, loại thu/chi và phân trang.
    """
    user_id = current_user.get("user_id")
    items, total = TransactionService.get_user_transactions(
        db=db, user_id=user_id, wallet_id=wallet_id, category_id=category_id,
        tx_type=type, start_date=start_date, end_date=end_date, limit=limit, offset=offset
    )
    msg = i18n_translator.translate(request, SystemConstants.TRANSACTION_FETCH_SUCCESS, msg_type=SystemConstants.MSG_TYPE_MESSAGE)
    return TransactionListResponse(
        success=True,
        error_code=SystemConstants.TRANSACTION_FETCH_SUCCESS,
        message=msg,
        data=items,
        total_count=total,
        trace_id=getattr(request.state, "trace_id", None)
    )


@router.get("/{transaction_id}", response_model=TransactionDetailResponse, status_code=status.HTTP_200_OK)
async def get_transaction_detail(
    transaction_id: str,
    request: Request,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    🎯 Xem chi tiết 1 giao dịch cụ thể theo ID:
       - Chống IDOR: Chỉ xem được giao dịch chính chủ.
    """
    user_id = current_user.get("user_id")
    tx = TransactionService.get_transaction_detail(db, user_id, transaction_id)
    msg = i18n_translator.translate(request, SystemConstants.TRANSACTION_FETCH_SUCCESS, msg_type=SystemConstants.MSG_TYPE_MESSAGE)
    return TransactionDetailResponse(
        success=True,
        error_code=SystemConstants.TRANSACTION_FETCH_SUCCESS,
        message=msg,
        data=tx,
        trace_id=getattr(request.state, "trace_id", None)
    )


@router.post("", response_model=TransactionDetailResponse, status_code=status.HTTP_201_CREATED)
async def create_transaction(
    request: Request,
    payload: CreateTransactionRequest,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    🎯 Tạo giao dịch Thu / Chi mới:
       - Tự động cộng/trừ số dư ví và kiểm tra số dư an toàn trong 1 SQL Transaction.
    """
    user_id = current_user.get("user_id")
    new_tx = TransactionService.create_transaction(db, user_id, payload)
    msg = i18n_translator.translate(request, SystemConstants.TRANSACTION_CREATE_SUCCESS, msg_type=SystemConstants.MSG_TYPE_MESSAGE)
    return TransactionDetailResponse(
        success=True,
        error_code=SystemConstants.TRANSACTION_CREATE_SUCCESS,
        message=msg,
        data=new_tx,
        trace_id=getattr(request.state, "trace_id", None)
    )


@router.delete("/{transaction_id}", response_model=UserActionResponse, status_code=status.HTTP_200_OK)
async def delete_transaction(
    transaction_id: str,
    request: Request,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    🎯 Xóa / Hủy giao dịch:
       - Tự động hoàn lại số dư ví ban đầu.
    """
    user_id = current_user.get("user_id")
    TransactionService.delete_transaction(db, user_id, transaction_id)
    msg = i18n_translator.translate(request, SystemConstants.TRANSACTION_CANCEL_SUCCESS, msg_type=SystemConstants.MSG_TYPE_MESSAGE)
    return UserActionResponse(
        success=True,
        error_code=SystemConstants.TRANSACTION_CANCEL_SUCCESS,
        message=msg,
        trace_id=getattr(request.state, "trace_id", None)
    )

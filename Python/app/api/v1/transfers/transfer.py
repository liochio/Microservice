# 📄 Đường dẫn file: app/api/v1/transfers/transfer.py
from fastapi import APIRouter, Depends, Request, Query, status
from sqlalchemy.orm import Session
from app.dependency import get_db
from app.constants import SystemConstants
from app.core.security.guard.guards import get_current_user
from app.core.translator.translator_engine import i18n_translator
from app.schemas.requests.transfer import CreateTransferRequest
from app.schemas.responses.transfer import TransferListResponse, TransferDetailResponse
from app.services.finance.transfer_service import TransferService

router = APIRouter(prefix="/transfers", tags=["Transfers"])


@router.get("", response_model=TransferListResponse, status_code=status.HTTP_200_OK)
async def list_transfers(
    request: Request,
    limit: int = Query(50, ge=1, le=100),
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    🎯 Xem lịch sử các lần chuyển tiền nội bộ:
       - Trả về danh sách lệnh chuyển tiền giữa các ví.
    """
    user_id = current_user.get("user_id")
    transfers = TransferService.get_user_transfers(db, user_id, limit)
    msg = i18n_translator.translate(request, SystemConstants.TRANSFER_FETCH_SUCCESS, msg_type=SystemConstants.MSG_TYPE_MESSAGE)
    return TransferListResponse(
        success=True,
        error_code=SystemConstants.TRANSFER_FETCH_SUCCESS,
        message=msg,
        data=transfers,
        trace_id=getattr(request.state, "trace_id", None)
    )


@router.get("/{transfer_id}", response_model=TransferDetailResponse, status_code=status.HTTP_200_OK)
async def get_transfer_detail(
    transfer_id: str,
    request: Request,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    🎯 Xem chi tiết 1 lệnh chuyển tiền cụ thể:
       - Chống IDOR: Chỉ xem được lệnh chuyển tiền của chính mình.
    """
    user_id = current_user.get("user_id")
    t = TransferService.get_transfer_detail(db, user_id, transfer_id)
    msg = i18n_translator.translate(request, SystemConstants.TRANSFER_FETCH_SUCCESS, msg_type=SystemConstants.MSG_TYPE_MESSAGE)
    return TransferDetailResponse(
        success=True,
        error_code=SystemConstants.TRANSFER_FETCH_SUCCESS,
        message=msg,
        data=t,
        trace_id=getattr(request.state, "trace_id", None)
    )


@router.post("", response_model=TransferDetailResponse, status_code=status.HTTP_201_CREATED)
async def execute_transfer(
    request: Request,
    payload: CreateTransferRequest,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    🎯 Thực hiện chuyển tiền nội bộ giữa 2 ví (ACID Transaction):
       - Trừ tiền ví nguồn và cộng tiền ví đích trong 1 transaction an toàn tuyệt đối.
    """
    user_id = current_user.get("user_id")
    new_t = TransferService.execute_transfer(db, user_id, payload)
    msg = i18n_translator.translate(request, SystemConstants.TRANSFER_EXECUTE_SUCCESS, msg_type=SystemConstants.MSG_TYPE_MESSAGE)
    return TransferDetailResponse(
        success=True,
        error_code=SystemConstants.TRANSFER_EXECUTE_SUCCESS,
        message=msg,
        data=new_t,
        trace_id=getattr(request.state, "trace_id", None)
    )

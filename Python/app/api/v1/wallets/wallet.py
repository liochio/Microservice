# 📄 Đường dẫn file: app/api/v1/wallets/wallet.py
from fastapi import APIRouter, Request, Depends, status
from fastapi.responses import JSONResponse
from fastapi.encoders import jsonable_encoder
from pydantic import BaseModel, Field
from typing import Optional, Any
from sqlalchemy.orm import Session

from app.schemas.requests.wallet import CreateWalletRequest
from app.schemas.responses.wallet import WalletResponse, WalletListResponse
from app.services.wallet.wallet_service import WalletService
from app.services.finance.transfer_service import TransferService
from app.schemas.requests.transfer import CreateTransferRequest
from app.core.exceptions.base_exception import FintechBaseException
from app.constants import SystemConstants
from app.core.translator.translator_engine import i18n_translator
from app.core.security.guard.guards import get_current_user
from app.dependency import get_db

router = APIRouter(
    prefix="/wallets",
    tags=["Wallet Management"],
    dependencies=[Depends(get_current_user)]
)


class TransferFundsRequest(BaseModel):
    """👑 DTO ĐẦU VÀO CHUYỂN TIỀN GIỮA CÁC VÍ (CLOSED-LOOP ENGINE)"""
    fromWalletType: Optional[str] = Field(None, description="Loại ví nguồn: AVAILABLE, SAVINGS, ESCROW")
    toWalletType: Optional[str] = Field(None, description="Loại ví đích: AVAILABLE, SAVINGS, ESCROW")
    source_wallet_id: Optional[str] = Field(None, description="ID ví nguồn")
    target_wallet_id: Optional[str] = Field(None, description="ID ví đích")
    toUserId: Optional[str] = Field(None, description="ID người nhận hoặc 'SELF'")
    amount: float = Field(..., gt=0, description="Số tiền cần chuyển (VND)")
    note: Optional[str] = Field("", max_length=255, description="Nội dung chuyển khoản")


class FintechResponse:
    @staticmethod
    def success(
        request: Request,
        error_code: str,
        response_schema: Any,
        status_code: int = status.HTTP_200_OK
    ) -> JSONResponse:
        message = i18n_translator.translate(request, error_code, msg_type=SystemConstants.MSG_TYPE_MESSAGE)
        if hasattr(response_schema, "message"):
            response_schema.message = message

        return JSONResponse(
            status_code=status_code,
            content=jsonable_encoder(response_schema)
        )


# ==============================================================================
# 👑 API 1: LẤY DANH SÁCH VÍ CỦA TÀI KHOẢN (PUBLIC CHO USER ĐÃ ĐĂNG NHẬP)
# ==============================================================================
@router.get("", response_model=WalletListResponse, status_code=status.HTTP_200_OK)
@router.get("/user/{user_id}", response_model=WalletListResponse, status_code=status.HTTP_200_OK)
async def list_wallets_endpoint(
    request: Request,
    user_id: Optional[str] = None,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    target_uid = user_id if (user_id and current_user.get("role") == "ADMIN") else current_user.get("user_id")

    try:
        result = WalletService.list_user_wallets(db_conn=db, user_id=str(target_uid))

        response_data = WalletListResponse(
            success=True,
            error_code=SystemConstants.WALLET_FETCH_SUCCESS,
            message="Lấy danh sách ví thành công",
            data=result
        )

        return FintechResponse.success(
            request=request,
            error_code=SystemConstants.WALLET_FETCH_SUCCESS,
            response_schema=response_data
        )
    except FintechBaseException as exc:
        raise exc


# ==============================================================================
# 👑 API 2: CHUYỂN TIỀN NỘI BỘ VÀ CLOSED-LOOP TRANSFER
# ==============================================================================
@router.post("/transfer", status_code=status.HTTP_200_OK)
async def transfer_wallets_endpoint(
    request: Request,
    body: TransferFundsRequest,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    user_id = str(current_user.get("user_id"))

    # Ràng buộc bảo mật Closed-Loop: Ví SAVINGS không thể chuyển P2P sang user khác
    if body.fromWalletType == "SAVINGS" and body.toUserId and body.toUserId != "SELF" and body.toUserId != user_id:
        return JSONResponse(
            status_code=status.HTTP_400_BAD_REQUEST,
            content={
                "success": False,
                "error_code": "CLOSED_LOOP_RESTRICTION",
                "message": "LỖI BẢO MẬT: Ví Tiết Kiệm (SAVINGS) thuộc cơ chế Khép Kín. Cấm chuyển P2P sang tài khoản khác!",
                "data": None
            }
        )

    # Tìm ID ví nguồn và ví đích
    user_wallets = WalletService.list_user_wallets(db_conn=db, user_id=user_id)
    source_id = body.source_wallet_id
    target_id = body.target_wallet_id

    if not source_id and user_wallets:
        for w in user_wallets:
            w_type = (w.get("wallet_type") or "").upper()
            if body.fromWalletType == "SAVINGS" and ("SAVING" in w_type or "PIGGY" in w_type):
                source_id = w.get("id")
            elif body.fromWalletType == "AVAILABLE" and ("CASH" in w_type or "AVAIL" in w_type):
                source_id = w.get("id")
            elif body.fromWalletType == "ESCROW" and "ESCROW" in w_type:
                source_id = w.get("id")

    if not target_id and user_wallets:
        for w in user_wallets:
            w_type = (w.get("wallet_type") or "").upper()
            if body.toWalletType == "SAVINGS" and ("SAVING" in w_type or "PIGGY" in w_type):
                target_id = w.get("id")
            elif body.toWalletType == "AVAILABLE" and ("CASH" in w_type or "AVAIL" in w_type):
                target_id = w.get("id")
            elif body.toWalletType == "ESCROW" and "ESCROW" in w_type:
                target_id = w.get("id")

    if not source_id and user_wallets:
        source_id = user_wallets[0].get("id")
    if not target_id and user_wallets:
        target_id = user_wallets[-1].get("id")

    try:
        transfer_dto = CreateTransferRequest(
            source_wallet_id=source_id or "default_source",
            target_wallet_id=target_id or "default_target",
            amount=body.amount,
            description=body.note or f"Chuyển tiền từ ví {body.fromWalletType} sang {body.toWalletType}"
        )
        res = TransferService.execute_transfer(db, user_id, transfer_dto)
        return {
            "success": True,
            "error_code": SystemConstants.SYSTEM_SUCCESS,
            "message": f"Chuyển thành công {body.amount:,.0f} đ",
            "data": res
        }
    except FintechBaseException as exc:
        raise exc
    except Exception as e:
        return JSONResponse(
            status_code=status.HTTP_400_BAD_REQUEST,
            content={
                "success": False,
                "error_code": "TRANSFER_FAILED",
                "message": f"Giao dịch chuyển tiền thất bại: {str(e)}",
                "data": None
            }
        )


# ==============================================================================
# 👑 API 3: KHỞI TẠO VÍ TÀI KHOẢN MỚI
# ==============================================================================
@router.post("", status_code=status.HTTP_201_CREATED)
async def create_wallet_endpoint(
    request: Request,
    body: CreateWalletRequest,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    user_id = current_user.get("user_id")

    try:
        result = WalletService.create_user_wallet(
            db_conn=db,
            user_id=str(user_id),
            wallet_code=body.wallet_code,
            name=body.name,
            currency=body.currency,
            wallet_type=body.wallet_type,
            color=body.color,
            icon=body.icon,
            description=body.description
        )

        response_data = WalletResponse(
            success=True,
            error_code=SystemConstants.WALLET_CREATE_SUCCESS,
            message="Tạo ví mới thành công",
            data=result
        )

        return FintechResponse.success(
            request=request,
            error_code=SystemConstants.WALLET_CREATE_SUCCESS,
            response_schema=response_data,
            status_code=status.HTTP_201_CREATED
        )
    except FintechBaseException as exc:
        raise exc


# ==============================================================================
# 👑 API 4: LẤY CHI TIẾT SỐ DƯ VÍ
# ==============================================================================
@router.get("/{wallet_id}", status_code=status.HTTP_200_OK)
async def get_wallet_detail_endpoint(
    request: Request,
    wallet_id: str,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    user_id = current_user.get("user_id")

    try:
        result = WalletService.get_wallet_detail_secure(db_conn=db, user_id=str(user_id), wallet_id=wallet_id)

        response_data = WalletResponse(
            success=True,
            error_code=SystemConstants.WALLET_FETCH_SUCCESS,
            message="",
            data=result
        )

        return FintechResponse.success(
            request=request,
            error_code=SystemConstants.WALLET_FETCH_SUCCESS,
            response_schema=response_data
        )
    except FintechBaseException as exc:
        raise exc

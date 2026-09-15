
from fastapi import APIRouter, Depends, Request, status
from sqlalchemy.orm import Session
from typing import List

from app.dependency import get_db
from app.core.security.guard.guards import get_current_user
from app.constants import SystemConstants
from app.core.translator.translator_engine import i18n_translator
from app.schemas.requests.payment import VietQrCreateRequest, PaymentWebhookPayload
from app.schemas.responses.payment import VietQrResponse, PaymentHistoryResponse
from app.schemas.responses.base_response import BaseResponse
from app.services.payment.payment_service import PaymentService

router = APIRouter(prefix="/payment", tags=["VietQR & Online Payment Gateway"])


@router.post("/create-vietqr", response_model=VietQrResponse, status_code=status.HTTP_201_CREATED)
async def create_vietqr_deposit(
    request: Request,
    payload: VietQrCreateRequest,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    💳 SINH MÃ NẠP TIỀN VIETQR CHUẨN NAPAS 247:
    - Trả về mã Order ID, nội dung chuyển khoản tự động và URL ảnh mã VietQR.
    """
    user_id = current_user.get("user_id")
    qr_data = PaymentService.create_vietqr(db, user_id, payload)
    return VietQrResponse(
        success=True,
        error_code=SystemConstants.WALLET_TOPUP_SUCCESS,
        message="Khởi tạo mã VietQR nạp tiền thành công!",
        data=qr_data,
        trace_id=getattr(request.state, "trace_id", None)
    )


@router.post("/webhook", response_model=BaseResponse, status_code=status.HTTP_200_OK)
async def process_payment_webhook(
    request: Request,
    payload: PaymentWebhookPayload,
    db: Session = Depends(get_db)
):
    """
    📡 WEBHOOK TỰ ĐỘNG CỘNG TIỀN TỪ CỔNG THANH TOÁN (SEPAY/CASSO/VNPAY):
    - Tự động nhận diện nội dung chuyển khoản, cộng tiền vào ví người dùng theo chuẩn ACID.
    """
    result = PaymentService.process_webhook(db, payload)
    return BaseResponse(
        success=True,
        error_code=SystemConstants.WALLET_TOPUP_SUCCESS,
        message="Xử lý webhook thanh toán thành công!",
        data=result,
        trace_id=getattr(request.state, "trace_id", None)
    )


@router.get("/history", response_model=PaymentHistoryResponse, status_code=status.HTTP_200_OK)
async def get_payment_history(
    request: Request,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Xem lịch sử nạp tiền trực tuyến"""
    user_id = current_user.get("user_id")
    txs = PaymentService.get_user_payment_history(db, user_id)
    return PaymentHistoryResponse(
        success=True,
        error_code=SystemConstants.TRANSACTION_FETCH_SUCCESS,
        message="Lấy lịch sử thanh toán thành công.",
        data=txs,
        total_count=len(txs),
        trace_id=getattr(request.state, "trace_id", None)
    )


from typing import Any
from fastapi import APIRouter, Request, Depends, status, Header
from fastapi.responses import JSONResponse
from fastapi.encoders import jsonable_encoder
from sqlalchemy.orm import Session

from app.constants import SystemConstants
from app.schemas.wallet_topup.wallet_topup import WalletTopupRequest, WalletTopupResponse
from app.services.finance.wallet_topup_service import WalletTopupService
from app.core.exceptions.base_exception import FintechBaseException
from app.core.translator.translator_engine import i18n_translator
from app.core.security.guard.guards import get_current_user, PermissionGuard
from app.dependency import get_db

router = APIRouter(
    prefix="/wallet_topup",
    tags=["Wallet Topup Management"],
    dependencies=[Depends(get_current_user)]
)


class FintechResponse:
    """👑 ĐIỀU PHỐI ĐÓNG GÓI RESPONSE CHUẨN ĐA NGÔN NGỮ"""

    @staticmethod
    def success(
        request: Request,
        error_code: str,
        response_schema: Any,
        status_code: int = status.HTTP_200_OK
    ) -> JSONResponse:
        message = i18n_translator.translate(request, error_code, msg_type=SystemConstants.MSG_TYPE_MESSAGE)
        response_schema.message = message

        return JSONResponse(
            status_code=status_code,
            content=jsonable_encoder(response_schema)
        )


# ==============================================================================
# 👑 API: NẠP TIỀN VÀO VÍ TÀI KHOẢN (Mã quyền: WALLET_TOPUP)
# ==============================================================================
@router.post(
    "",
    response_model=WalletTopupResponse,
    status_code=status.HTTP_200_OK,
    dependencies=[Depends(PermissionGuard(SystemConstants.PERMISSION_WALLET_TOPUP))]
)
async def wallet_topup_endpoint(
    request: Request,
    payload: WalletTopupRequest,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db),
    idempotency_key: str = Header(None, alias="Idempotency-Key"),
) -> JSONResponse:
    """
    🎯 API Nạp tiền vào ví người dùng:
       - Kiểm tra mã quyền WALLET_TOPUP.
       - Hỗ trợ Idempotency-Key chống giao dịch trùng lặp (Double Spending).
       - Cập nhật số dư nguyên tử (Atomic Balance Update) và ghi vết Transaction Log.
    """
    user_id = str(current_user.get("user_id", SystemConstants.UNKNOWN))
    client_ip = str(getattr(request.state, "client_ip", "127.0.0.1"))

    try:
        result = WalletTopupService.execute_topup(
            db=db,
            req=payload,
            user_id=user_id,
            client_ip=client_ip
        )

        response_data = WalletTopupResponse(
            success=True,
            transaction_code=result.transaction_code,
            amount=result.amount,
            balance_before=result.balance_before,
            balance_after=result.balance_after,
            wallet_currency=result.wallet_currency,
            status=result.status,
            message=""
        )

        return FintechResponse.success(
            request=request,
            error_code=SystemConstants.WALLET_TOPUP_SUCCESS,
            response_schema=response_data,
            status_code=status.HTTP_200_OK
        )

    except FintechBaseException:
        raise
    except Exception:
        raise FintechBaseException(
            error_code=SystemConstants.TOPUP_INTERNAL_CRASH,
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR
        )
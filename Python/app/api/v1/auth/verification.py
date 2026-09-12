# 📄 Đường dẫn file: app/api/v1/auth/verification.py
from typing import Optional
from fastapi import APIRouter, Request, Depends, status
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field
from sqlalchemy.orm import Session

from app.constants import SystemConstants
from app.services.auth.auth.verification_service import VerificationService
from app.core.exceptions.base_exception import FintechBaseException
from app.core.translator.translator_engine import i18n_translator
from app.dependency import get_db

router = APIRouter(prefix="/auth", tags=["Verification"])


class VerifyOtpRequest(BaseModel):
    """👑 DTO ĐẦU VÀO XÁC THỰC MÃ OTP KÍCH HOẠT TÀI KHOẢN"""
    username: Optional[str] = Field(None, description="Tên đăng nhập hoặc Email")
    user_id: Optional[str] = Field(None, description="ID người dùng")
    notification_id: Optional[str] = Field(None, description="ID thông báo")
    otp_code: Optional[str] = Field(None, description="Mã OTP 6 chữ số")
    otpCode: Optional[str] = Field(None, description="Mã OTP (camelCase)")
    otp: Optional[str] = Field(None, description="Mã OTP")
    token: Optional[str] = Field(None, description="Mã OTP hoặc Token")


# ==============================================================================
# 👑 API: KÍCH HOẠT LIÊN KẾT EMAIL (ACTIVATE LINK)
# ==============================================================================
@router.get(
    "/activate",
    status_code=status.HTTP_200_OK,
)
async def activate_link_endpoint(
    request: Request,
    token: str,
    db: Session = Depends(get_db),
):
    trace_id = getattr(request.state, "trace_id", SystemConstants.UNKNOWN)
    try:
        result = VerificationService.process_verification_token(db, token, trace_id=trace_id)

        message = i18n_translator.translate(
            request,
            error_code=SystemConstants.TOKEN_VALID_OTP_GENERATED,
            msg_type=SystemConstants.MSG_TYPE_MESSAGE
        )

        return JSONResponse(
            status_code=status.HTTP_200_OK,
            content={
                "success": True,
                "error_code": SystemConstants.TOKEN_VALID_OTP_GENERATED,
                "message": message,
                "context": result
            }
        )
    except FintechBaseException:
        raise
    except Exception:
        raise FintechBaseException(
            error_code=SystemConstants.ACTIVATE_LINK_CRASH,
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR
        )


# ==============================================================================
# 👑 API: XÁC THỰC MÃ OTP CHỐT HẠ KÍCH HOẠT TÀI KHOẢN (VERIFY OTP)
# ==============================================================================
@router.post(
    "/verify-otp",
    status_code=status.HTTP_200_OK,
)
async def verify_otp_endpoint(
    request: Request,
    body: VerifyOtpRequest,
    db: Session = Depends(get_db),
):
    trace_id = getattr(request.state, "trace_id", SystemConstants.UNKNOWN)
    effective_otp = body.otp_code or body.otpCode or body.otp or body.token
    if not effective_otp or len(str(effective_otp).strip()) == 0:
        raise FintechBaseException(
            error_code=SystemConstants.INVALID_OTP_FORMAT,
            status_code=status.HTTP_400_BAD_REQUEST
        )

    try:
        VerificationService.validate_and_activate_user(
            db_conn=db,
            user_id=body.user_id,
            username=body.username,
            notification_id=body.notification_id,
            otp_code=str(effective_otp).strip(),
            trace_id=trace_id
        )

        message = i18n_translator.translate(
            request,
            error_code=SystemConstants.ACCOUNT_ACTIVATION_SUCCESS,
            msg_type=SystemConstants.MSG_TYPE_MESSAGE
        )

        return JSONResponse(
            status_code=status.HTTP_200_OK,
            content={
                "success": True,
                "error_code": SystemConstants.ACCOUNT_ACTIVATION_SUCCESS,
                "message": message or "Kích hoạt tài khoản thành công!",
                "context": {}
            }
        )
    except FintechBaseException:
        raise
    except Exception as e:
        raise FintechBaseException(
            error_code=SystemConstants.OTP_VERIFICATION_CRASH,
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR
        )

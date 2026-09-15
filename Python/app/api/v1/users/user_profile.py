
from fastapi import APIRouter, Depends, Request, status
from sqlalchemy.orm import Session
from app.dependency import get_db
from app.constants import SystemConstants
from app.core.security.guard.guards import get_current_user
from app.core.translator.translator_engine import i18n_translator
from app.schemas.requests.user import (
    UpdateProfileRequest,
    ChangePasswordRequest,
    ForgotPasswordRequest,
    ResetPasswordRequest
)
from app.schemas.responses.user import UserProfileResponse, UserActionResponse
from app.services.auth.user.user_service import UserService

router = APIRouter(prefix="/users", tags=["User Profile & Security"])


@router.get("/me", response_model=UserProfileResponse, status_code=status.HTTP_200_OK)
async def get_my_profile(
    request: Request,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    🎯 Lấy thông tin hồ sơ của tài khoản đang đăng nhập:
       - Trả về: Họ tên, email, sđt, ngày sinh, giới tính, vai trò và trạng thái.
    """
    user_id = current_user.get("user_id")
    profile = UserService.get_profile(db, user_id)
    msg = i18n_translator.translate(request, SystemConstants.USER_PROFILE_FETCH_SUCCESS, msg_type=SystemConstants.MSG_TYPE_MESSAGE)
    return UserProfileResponse(
        success=True,
        error_code=SystemConstants.USER_PROFILE_FETCH_SUCCESS,
        message=msg,
        data=profile,
        trace_id=getattr(request.state, "trace_id", None)
    )


@router.put("/me", response_model=UserProfileResponse, status_code=status.HTTP_200_OK)
async def update_my_profile(
    request: Request,
    payload: UpdateProfileRequest,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    🎯 Cập nhật thông tin hồ sơ cá nhân:
       - Cho phép sửa: Họ tên, Ngày sinh, Giới tính (Chống XSS mã độc).
    """
    user_id = current_user.get("user_id")
    updated = UserService.update_profile(db, user_id, payload)
    msg = i18n_translator.translate(request, SystemConstants.USER_PROFILE_UPDATE_SUCCESS, msg_type=SystemConstants.MSG_TYPE_MESSAGE)
    return UserProfileResponse(
        success=True,
        error_code=SystemConstants.USER_PROFILE_UPDATE_SUCCESS,
        message=msg,
        data=updated,
        trace_id=getattr(request.state, "trace_id", None)
    )


@router.post("/change-password", response_model=UserActionResponse, status_code=status.HTTP_200_OK)
async def change_password(
    request: Request,
    payload: ChangePasswordRequest,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    🎯 Đổi mật khẩu tài khoản:
       - Đối chiếu mật khẩu cũ và cập nhật mật khẩu mới mã hóa Bcrypt.
    """
    user_id = current_user.get("user_id")
    UserService.change_password(db, user_id, payload)
    msg = i18n_translator.translate(request, SystemConstants.AUTH_PASSWORD_CHANGED_SUCCESS, msg_type=SystemConstants.MSG_TYPE_MESSAGE)
    return UserActionResponse(
        success=True,
        error_code=SystemConstants.AUTH_PASSWORD_CHANGED_SUCCESS,
        message=msg,
        trace_id=getattr(request.state, "trace_id", None)
    )


@router.post("/forgot-password", response_model=UserActionResponse, status_code=status.HTTP_200_OK)
async def forgot_password(
    request: Request,
    payload: ForgotPasswordRequest,
    db: Session = Depends(get_db)
):
    """
    🎯 Quên mật khẩu:
       - Gửi mã OTP xác thực 6 số tới Email người dùng.
    """
    result = UserService.forgot_password_request(db, payload.email)
    msg = i18n_translator.translate(request, SystemConstants.AUTH_OTP_SENT_SUCCESS, msg_type=SystemConstants.MSG_TYPE_MESSAGE)
    return UserActionResponse(
        success=True,
        error_code=SystemConstants.AUTH_OTP_SENT_SUCCESS,
        message=msg,
        data=result,
        trace_id=getattr(request.state, "trace_id", None)
    )


@router.post("/reset-password", response_model=UserActionResponse, status_code=status.HTTP_200_OK)
async def reset_password(
    request: Request,
    payload: ResetPasswordRequest,
    db: Session = Depends(get_db)
):
    """
    🎯 Đặt lại mật khẩu mới bằng mã OTP:
       - Kiểm tra OTP và đổi mật khẩu mới.
    """
    UserService.reset_password_with_otp(db, payload)
    msg = i18n_translator.translate(request, SystemConstants.AUTH_PASSWORD_RESET_SUCCESS, msg_type=SystemConstants.MSG_TYPE_MESSAGE)
    return UserActionResponse(
        success=True,
        error_code=SystemConstants.AUTH_PASSWORD_RESET_SUCCESS,
        message=msg,
        trace_id=getattr(request.state, "trace_id", None)
    )

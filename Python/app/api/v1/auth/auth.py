# 📄 Đường dẫn file: app/api/v1/auth/auth.py
from fastapi import APIRouter, Depends, Request, status
from sqlalchemy.orm import Session

from app.constants import SystemConstants
from app.core.exceptions.base_exception import FintechBaseException
from app.core.security.guard.guards import get_current_user
from app.core.translator.translator_engine import i18n_translator
from app.dependency import get_db
from app.schemas.requests.auth import (
    UserLoginRequest,
    UserRegisterRequest,
    RefreshTokenRequest,
)
from app.schemas.responses.auth import UserRegisterResponse
from app.services.auth.auth.auth_service import AuthService
from app.services.auth.auth.token_service import TokenService

router = APIRouter(prefix="/auth", tags=["Authentication"])


# ==============================================================================
# 👑 API 1: ĐĂNG KÝ TÀI KHOẢN MỚI (PUBLIC ENDPOINT)
# ==============================================================================
@router.post(
    "/register",
    response_model=UserRegisterResponse,
    status_code=status.HTTP_201_CREATED,
)
async def register_account(
    request: Request,
    payload: UserRegisterRequest,
    db: Session = Depends(get_db),
):
    """
    🎯 API Đăng ký tài khoản người dùng mới:
       - Validate định dạng dữ liệu đầu vào tuần tự.
       - Tạo bản ghi User trạng thái PENDING, Notification email và phát hành link token.
       - Tự động Commit dữ liệu vào Database.
    """
    try:
        result_data = AuthService.register_new_user(db, payload)

        i18n_message = i18n_translator.translate(
            request,
            error_code=SystemConstants.USER_REGISTER_SUCCESS,
            msg_type=SystemConstants.MSG_TYPE_MESSAGE
        )

        return UserRegisterResponse(
            success=True,
            error_code=SystemConstants.USER_REGISTER_SUCCESS,
            message=i18n_message,
            data=result_data,
        )

    except FintechBaseException:
        raise

    except Exception:
        raise FintechBaseException(
            error_code=SystemConstants.REGISTRATION_INTERNAL_CRASH,
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        )


# ==============================================================================
# 👑 API 2: ĐĂNG NHẬP HỆ THỐNG (PUBLIC ENDPOINT)
# ==============================================================================
@router.post(
    "/login",
    status_code=status.HTTP_200_OK,
)
async def login_account(
    request: Request,
    payload: UserLoginRequest,
    db: Session = Depends(get_db),
):
    """
    🎯 API Đăng nhập hệ thống:
       - Đối chiếu thông tin đăng nhập (email & mật khẩu Bcrypt).
       - Kiểm tra trạng thái tài khoản (ACTIVE / BLOCKED / PENDING).
       - Cấp cặp bài trùng Token JTI (Access Token 30m + Refresh Token 7d) chứa Ma trận quyền.
       - Khóa xích Session xuống bảng `user_sessions`.
    """
    try:
        result_data = AuthService.login_user(db, payload)

        print("DA TOI DAY!")

        i18n_message = i18n_translator.translate(
            request,
            error_code=SystemConstants.USER_LOGIN_SUCCESS,
            msg_type=SystemConstants.MSG_TYPE_MESSAGE
        )

        return {
            "success": True,
            "error_code": SystemConstants.USER_LOGIN_SUCCESS,
            "message": i18n_message,
            "data": result_data,
        }

    except FintechBaseException:
        raise

    except Exception:
        raise FintechBaseException(
            error_code=SystemConstants.LOGIN_INTERNAL_CRASH,
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        )


# ==============================================================================
# 👑 API 3: LÀM MỚI ACCESS TOKEN (REFRESH TOKEN ROTATION)
# ==============================================================================
@router.post(
    "/refresh-token",
    status_code=status.HTTP_200_OK,
)
async def refresh_access_token(
    request: Request,
    payload: RefreshTokenRequest,
    db: Session = Depends(get_db),
):
    """
    🎯 API Làm mới Token (Token Rotation):
       - Kiểm tra tính hợp lệ của Refresh Token (chống Replay Attack).
       - Thu hồi JTI cũ và cấp cặp Token mới.
       - TỰ ĐỘNG NẠP LẠI ĐẦY ĐỦ QUYỀN HẠN (roles, modules, permissions) của User từ DB.
    """
    device = getattr(request.state, "user_agent", SystemConstants.UNKNOWN)
    client_ip = getattr(request.state, "client_ip", "127.0.0.1")

    try:
        new_token_data = TokenService.rotate_refresh_token(
            db_conn=db,
            old_refresh_token=payload.refresh_token,
            device=device,
            ip=client_ip
        )

        i18n_message = i18n_translator.translate(
            request,
            error_code=SystemConstants.TOKEN_REFRESH_SUCCESS,
            msg_type=SystemConstants.MSG_TYPE_MESSAGE
        )

        return {
            "success": True,
            "error_code": SystemConstants.TOKEN_REFRESH_SUCCESS,
            "message": i18n_message,
            "data": new_token_data
        }

    except FintechBaseException:
        raise
    except Exception:
        raise FintechBaseException(
            error_code=SystemConstants.TOKEN_REFRESH_CRASH,
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR
        )


# ==============================================================================
# 👑 API 4: ĐĂNG XUẤT HỆ THỐNG (REVOKE SESSION / LOGOUT)
# ==============================================================================
@router.post(
    "/logout",
    status_code=status.HTTP_200_OK,
)
async def logout_account(
    request: Request,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    """
    🎯 API Đăng xuất hệ thống:
       - Yêu cầu Bearer Token hợp lệ.
       - Thu hồi phiên làm việc (is_revoked = 1) trong bảng `user_sessions`.
       - Vô hiệu hóa quyền truy cập của Token.
    """
    user_id = current_user.get("user_id")

    try:
        TokenService.revoke_session(db_conn=db, user_id=user_id)

        i18n_message = i18n_translator.translate(
            request,
            error_code=SystemConstants.AUTH_LOGOUT_SUCCESS,
            msg_type=SystemConstants.MSG_TYPE_MESSAGE
        )

        return {
            "success": True,
            "error_code": SystemConstants.AUTH_LOGOUT_SUCCESS,
            "message": i18n_message
        }

    except FintechBaseException:
        raise
    except Exception:
        raise FintechBaseException(
            error_code=SystemConstants.LOGOUT_INTERNAL_CRASH,
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR
        )
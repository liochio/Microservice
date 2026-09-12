# 📄 Đường dẫn file: app/core/security/guard/guards.py
import jwt
from typing import Dict, Any, Optional
from fastapi import Request, Depends, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials

from app.core.config.settings import settings
from app.core.security.jwt.jwt_service import JwtService
from app.core.exceptions.base_exception import FintechBaseException
from app.constants import SystemConstants

# Khởi tạo HTTPBearer Security Scheme để Swagger UI tự động hiển thị nút Authorize 🔒
security_scheme = HTTPBearer(auto_error=False)


async def get_current_user(
    request: Request,
    credentials: Optional[HTTPAuthorizationCredentials] = Depends(security_scheme)
) -> Dict[str, Any]:
    """
    👑 DEPENDENCY XÁC THỰC NGƯỜI DÙNG HIỆN TẠI (LOCAL RS256 JWT VERIFICATION):
    🎯 Mục đích:
       - Kiểm tra Bearer Token JWT trong Header Authorization.
       - Giải mã chữ ký RS256 JWT bằng Public Key (lấy từ JWKS của liochio-core).
       - Nạp thông tin định danh (user_id, username, permissions, roles, modules) vào `request.state`.
       - Ném lỗi 401 Unauthorized nếu token thiếu, hết hạn hoặc sai chữ ký.
    """
    # Nếu middleware đã giải mã thành công user_id hợp lệ
    current_user_id = getattr(request.state, "user_id", "ANONYMOUS")
    if current_user_id != "ANONYMOUS":
        return {
            "user_id": current_user_id,
            "username": getattr(request.state, "username", "ANONYMOUS"),
            "permissions": getattr(request.state, "user_permissions", []),
            "roles": getattr(request.state, "user_roles", []),
            "modules": getattr(request.state, "user_modules", [])
        }

    # Bóc tách token từ credentials hoặc Header
    token = credentials.credentials if credentials and credentials.credentials else None
    if not token:
        auth_header = request.headers.get("Authorization") or request.headers.get("authorization")
        if auth_header:
            if auth_header.lower().startswith("bearer "):
                token = auth_header[7:].strip()
            elif "." in auth_header:
                token = auth_header.strip()
        if not token:
            token = request.headers.get("X-Access-Token") or request.headers.get("x-access-token")

    if not token:
        raise FintechBaseException(
            error_code=SystemConstants.AUTH_UNAUTHORIZED,
            status_code=status.HTTP_401_UNAUTHORIZED
        )
    try:
        payload = JwtService.decode_token(token)
        user_id = payload.get("sub")
        if not user_id:
            raise FintechBaseException(error_code=SystemConstants.AUTH_INVALID_TOKEN, status_code=status.HTTP_401_UNAUTHORIZED)

        # Nạp lại vào request.state để dùng chung xuyên suốt router
        request.state.user_id = user_id
        request.state.username = payload.get("username", "ANONYMOUS")
        request.state.user_permissions = payload.get("permissions", [])
        request.state.permissions = request.state.user_permissions
        request.state.user_roles = payload.get("roles", [])
        request.state.user_modules = payload.get("modules", [])
        request.state.modules = request.state.user_modules

        return {
            "user_id": user_id,
            "username": request.state.username,
            "permissions": request.state.user_permissions,
            "roles": request.state.user_roles,
            "modules": request.state.user_modules
        }

    except jwt.ExpiredSignatureError:
        raise FintechBaseException(error_code=SystemConstants.AUTH_TOKEN_EXPIRED, status_code=status.HTTP_401_UNAUTHORIZED)
    except jwt.PyJWTError:
        raise FintechBaseException(error_code=SystemConstants.AUTH_INVALID_TOKEN, status_code=status.HTTP_401_UNAUTHORIZED)


class RoleBasedGuard:
    """
    👑 MODULE-LEVEL GUARD (PHÂN QUYỀN THEO PHÂN HỆ MODULE HOẶC ROLE):
    """
    def __init__(self, required_module: str):
        self.required_module = required_module

    async def __call__(self, request: Request, current_user: dict = Depends(get_current_user)) -> dict:
        user_id = current_user.get("user_id", "ANONYMOUS")
        user_modules = current_user.get("modules", [])
        user_roles = current_user.get("roles", [])
        trace_id = getattr(request.state, "trace_id", SystemConstants.UNKNOWN)

        if user_id == "ANONYMOUS":
            raise FintechBaseException(error_code=SystemConstants.AUTH_UNAUTHORIZED, status_code=status.HTTP_401_UNAUTHORIZED)

        # Admin có toàn quyền
        if "ROLE_ADMIN" in user_roles:
            return {"user_id": user_id, "module": self.required_module}

        if self.required_module not in user_modules and self.required_module not in user_roles:
            print(f"\n🚫 [GUARD_MODULE_DENIED] Chặn truy cập! User: {user_id} | Thiếu Module/Role: '{self.required_module}' | TraceID: {trace_id}")
            raise FintechBaseException(error_code=SystemConstants.ACCESS_DENIED_MODULE_RESTRICTED, status_code=status.HTTP_403_FORBIDDEN)

        return {"user_id": user_id, "module": self.required_module}


class PermissionGuard:
    """
    👑 ACTION-LEVEL GUARD (PHÂN QUYỀN CHI TIẾT THEO HÀNH ĐỘNG):
    """
    def __init__(self, required_permission: str):
        self.required_permission = required_permission

    async def __call__(self, request: Request, current_user: dict = Depends(get_current_user)) -> dict:
        user_id = current_user.get("user_id", "ANONYMOUS")
        user_permissions = current_user.get("permissions", [])
        user_roles = current_user.get("roles", [])
        trace_id = getattr(request.state, "trace_id", SystemConstants.UNKNOWN)

        if user_id == "ANONYMOUS":
            raise FintechBaseException(error_code=SystemConstants.AUTH_UNAUTHORIZED, status_code=status.HTTP_401_UNAUTHORIZED)

        # Admin có toàn quyền
        if "ROLE_ADMIN" in user_roles:
            return {"user_id": user_id, "permission": self.required_permission}

        if self.required_permission not in user_permissions:
            print(f"\n🚫 [GUARD_PERMISSION_DENIED] Chặn truy cập! User: {user_id} | Thiếu Quyền: '{self.required_permission}' | TraceID: {trace_id}")
            raise FintechBaseException(error_code=SystemConstants.AUTH_PERMISSION_DENIED, status_code=status.HTTP_403_FORBIDDEN)

        return {"user_id": user_id, "permission": self.required_permission}


class RequireActionTokenGuard:
    """
    👑 SMART OTP ACTION TOKEN GUARD:
    Xác thực header `X-Action-Token` trước khi thực hiện các giao dịch nhạy cảm (rút tiền ví, mở khóa heo).
    """
    def __init__(self, action_type: str = "FINTECH_TRANSACTION"):
        self.action_type = action_type

    async def __call__(self, request: Request, current_user: dict = Depends(get_current_user)) -> dict:
        action_token = request.headers.get("X-Action-Token") or request.headers.get("x-action-token")
        if not action_token:
            raise FintechBaseException(
                error_code="SMART_OTP_ACTION_TOKEN_REQUIRED",
                status_code=status.HTTP_403_FORBIDDEN
            )

        try:
            payload = JwtService.decode_token(action_token)
            token_type = payload.get("token_type")
            sub = payload.get("sub")

            if token_type != "ACTION_TOKEN" or sub != current_user.get("user_id"):
                raise FintechBaseException(
                    error_code="SMART_OTP_INVALID_ACTION_TOKEN",
                    status_code=status.HTTP_403_FORBIDDEN
                )

            return {"action_token": action_token, "sub": sub, "payload": payload}
        except Exception:
            raise FintechBaseException(
                error_code="SMART_OTP_INVALID_ACTION_TOKEN",
                status_code=status.HTTP_403_FORBIDDEN
            )
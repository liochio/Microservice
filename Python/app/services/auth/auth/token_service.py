# 📄 Đường dẫn file: app/services/auth/auth/token_service.py
import hashlib
import uuid
import jwt
from datetime import datetime, timedelta
from sqlalchemy import select, update, insert

from app.models.auth.user_session import UserSession
from app.models.user.user import User
from app.core.config.settings import settings
from app.core.exceptions.base_exception import FintechBaseException
from app.core.security.jwt.jwt_service import JwtService
from app.repositories.module.module_repository import ModuleRepository
from app.services.auth.permission.permission_service import PermissionService


class TokenService:
    """
    👑 SYSTEM SESSION & TOKEN ROTATION WORKER (ENTERPRISE FINTECH)
    🎯 Mục đích & Nhiệm vụ:
       1. Lưu trữ và khóa xích phiên đăng nhập (UserSession) với chữ ký JTI và Hash Token.
       2. Xoay vòng Refresh Token an toàn: Hủy JTI cũ, cấp Token mới VẪN GIỮ NGUYÊN ĐẦY ĐỦ QUYỀN HẠN của User.
       3. Thu hồi phiên đăng nhập (Revoke Session / Logout) để chống tái sử dụng token bị đánh cắp.
       4. Đảm bảo toàn vẹn giao dịch (Commit Transaction) xuống Database.
    """

    @staticmethod
    def _hash_token(token: str) -> str:
        """Băm mã Refresh Token bằng SHA-256 trước khi lưu xuống SQL để tăng cường bảo mật"""
        return hashlib.sha256(token.encode("utf-8")).hexdigest()

    @staticmethod
    def register_session(
        db_conn,
        user_id: str,
        refresh_token: str,
        jti: str,
        device: str,
        ip: str,
        expires_at: datetime
    ) -> None:
        """
        👑 ĐĂNG KÝ PHIÊN ĐĂNG NHẬP MỚI VÀO DATABASE:
        Lưu bản ghi vào bảng user_sessions và commit an toàn vào DB.
        """
        token_hash = TokenService._hash_token(refresh_token)
        now = datetime.now()

        stmt = insert(UserSession).values(
            id=str(uuid.uuid4()),
            user_id=str(user_id),
            jti=str(jti),
            refresh_token_hash=token_hash,
            device_info=str(device)[:255] if device else "UNKNOWN",
            ip_address=str(ip)[:50] if ip else "127.0.0.1",
            is_revoked=0,
            expires_at=expires_at,
            login_at=now,
            created_at=now,
            updated_at=now
        )
        db_conn.execute(stmt)

        # 👑 ĐẢM BẢO TRANSACTION ĐƯỢC COMMIT BỀN VỮNG
        if hasattr(db_conn, "commit"):
            db_conn.commit()

    @staticmethod
    def rotate_refresh_token(
        db_conn,
        old_refresh_token: str,
        device: str = "UNKNOWN",
        ip: str = "127.0.0.1"
    ) -> dict:
        """
        👑 XOAY VÒNG REFRESH TOKEN (TOKEN ROTATION WITH FULL PERMISSIONS):
        1. Giải mã Refresh Token, kiểm tra type=='refresh' và hạn sử dụng.
        2. Đối chiếu JTI trong bảng user_sessions (nếu đã bị revoked -> phát hiện tấn công replay).
        3. Thu hồi JTI cũ (is_revoked = 1).
        4. Nạp lại thông tin User thực tế (username, roles, permissions, modules) từ Database.
        5. Đúc cặp Token mới đầy đủ quyền lực và lưu session mới.
        """
        try:
            payload = jwt.decode(
                old_refresh_token,
                settings.JWT_REFRESH_SECRET_KEY,
                algorithms=[settings.ALGORITHM],
                options={"verify_exp": True, "verify_iat": False, "leeway": 60}
            )
            if payload.get("type") != "refresh":
                raise jwt.PyJWTError("Token type must be refresh")
        except jwt.ExpiredSignatureError:
            raise FintechBaseException(error_code=SystemConstants.AUTH_TOKEN_EXPIRED, status_code=401)
        except jwt.PyJWTError:
            raise FintechBaseException(error_code=SystemConstants.AUTH_INVALID_TOKEN, status_code=401)

        jti = payload.get("jti")
        user_id = str(payload.get("sub"))

        # Kiểm tra phiên trong Database
        stmt = select(UserSession).where(UserSession.jti == jti).limit(1)
        session_record = db_conn.execute(stmt).scalars().first()

        # Phát hiện Replay Attack: Nếu session không tồn tại hoặc đã bị thu hồi trước đó
        if not session_record or session_record.is_revoked == 1 or datetime.now() > session_record.expires_at:
            if session_record:
                # Thu hồi toàn bộ phiên của user này để bảo vệ tài khoản
                db_conn.execute(update(UserSession).where(UserSession.user_id == user_id).values(is_revoked=1))
                if hasattr(db_conn, "commit"):
                    db_conn.commit()
            raise FintechBaseException(error_code=SystemConstants.AUTH_SESSION_REVOKED, status_code=401)

        # 1. Hủy phiên làm việc cũ
        db_conn.execute(
            update(UserSession)
            .where(UserSession.jti == jti)
            .values(is_revoked=1, updated_at=datetime.now())
        )

        # 2. Truy vấn thông tin thực thể User và nạp lại Ma trận quyền hạn mới nhất
        stmt_user = select(User).where(User.id == user_id).limit(1)
        user_obj = db_conn.execute(stmt_user).scalars().first()
        if not user_obj or str(user_obj.status).upper() == SystemConstants.BLOCKED:
            if hasattr(db_conn, "commit"):
                db_conn.commit()
            raise FintechBaseException(error_code=SystemConstants.USER_ACCOUNT_BLOCKED, status_code=403)

        username = user_obj.username

        # Nạp lại Module và Permissions của User từ Database
        flat_modules = ModuleRepository.fetch_all_modules_recursive(db_conn, user_id)
        user_module_codes = [str(m["code"]) for m in flat_modules]
        _, perms_token = PermissionService.get_permissions_and_token_list(db_conn, user_id)

        # 3. Sinh cặp Token mới giữ nguyên đầy đủ quyền hạn
        new_pair = JwtService.generate_token_pair(
            user_id=user_id,
            username=username,
            permissions=perms_token,
            modules=user_module_codes
        )

        # 4. Đăng ký session mới vào Database
        expire_date = datetime.now() + timedelta(days=settings.REFRESH_TOKEN_EXPIRE_DAYS)
        TokenService.register_session(
            db_conn=db_conn,
            user_id=user_id,
            refresh_token=new_pair["refresh_token"],
            jti=new_pair["refresh_jti"],
            device=device,
            ip=ip,
            expires_at=expire_date
        )

        return {
            "access_token": new_pair["access_token"],
            "refresh_token": new_pair["refresh_token"],
            "token_type": "Bearer",
            "expires_in": settings.ACCESS_TOKEN_EXPIRE_MINUTES * 60
        }

    @staticmethod
    def revoke_session(db_conn, user_id: str, jti: Optional[str] = None) -> bool:
        """
        👑 THU HỒI PHIÊN ĐĂNG NHẬP (LOGOUT WORKER):
        - Nếu có JTI: Thu hồi đúng phiên của thiết bị hiện tại.
        - Nếu không có JTI: Thu hồi toàn bộ phiên của User trên mọi thiết bị.
        """
        now = datetime.now()
        if jti:
            db_conn.execute(
                update(UserSession)
                .where(UserSession.jti == jti, UserSession.user_id == user_id)
                .values(is_revoked=1, updated_at=now)
            )
        else:
            db_conn.execute(
                update(UserSession)
                .where(UserSession.user_id == user_id)
                .values(is_revoked=1, updated_at=now)
            )

        if hasattr(db_conn, "commit"):
            db_conn.commit()
        return True
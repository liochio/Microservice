# 📄 Đường dẫn file: app/services/auth/user/user_service.py
from sqlalchemy import select, update, text
from datetime import datetime
from app.models.user.user import User
from app.models.role.role import Role
from app.models.user_role.user_role import UserRole
from app.services.common.crypto_service import CryptoService
from app.services.auth.auth.otp_service import OtpService
from app.constants import SystemConstants
from app.core.exceptions.base_exception import FintechBaseException


class UserService:
    """
    👑 SERVICE: QUẢN LÝ HỒ SƠ NGƯỜI DÙNG & BẢO MẬT TÀI KHOẢN
    """

    @staticmethod
    def get_profile(db, user_id: str) -> dict:
        user = db.execute(select(User).where(User.id == user_id)).scalars().first()
        if not user:
            raise FintechBaseException(error_code=SystemConstants.USER_NOT_FOUND, status_code=404)

        # Lấy danh sách tên vai trò
        roles_stmt = select(Role.name).join(UserRole, Role.id == UserRole.role_id).where(UserRole.user_id == user_id)
        role_names = db.execute(roles_stmt).scalars().all()

        return {
            "user_id": user.id,
            "username": user.username,
            "email": user.email,
            "phone_number": user.phone_number,
            "full_name": user.full_name,
            "date_of_birth": str(user.date_of_birth) if user.date_of_birth else None,
            "gender": str(user.gender) if user.gender else None,
            "status": user.status,
            "is_active": user.is_active,
            "roles": list(role_names)
        }

    @staticmethod
    def update_profile(db, user_id: str, payload) -> dict:
        user = db.execute(select(User).where(User.id == user_id)).scalars().first()
        if not user:
            raise FintechBaseException(error_code=SystemConstants.USER_NOT_FOUND, status_code=404)

        update_values = {}
        if payload.full_name is not None:
            update_values["full_name"] = payload.full_name
        if payload.date_of_birth is not None:
            update_values["date_of_birth"] = payload.date_of_birth
        if payload.gender is not None:
            update_values["gender"] = str(payload.gender).upper()

        if update_values:
            update_values["updated_at"] = datetime.now()
            db.execute(update(User).where(User.id == user_id).values(**update_values))
            db.commit()

        return UserService.get_profile(db, user_id)

    @staticmethod
    def change_password(db, user_id: str, payload) -> bool:
        user = db.execute(select(User).where(User.id == user_id)).scalars().first()
        if not user:
            raise FintechBaseException(error_code=SystemConstants.USER_NOT_FOUND, status_code=404)

        # 1. So khớp mật khẩu cũ
        if not CryptoService.verify_password(payload.old_password, user.password_hash):
            raise FintechBaseException(error_code="OLD_PASSWORD_INCORRECT", status_code=400)

        # 2. Cập nhật mật khẩu mới băm Bcrypt
        new_hashed = CryptoService.hash_password(payload.new_password)
        db.execute(update(User).where(User.id == user_id).values(password_hash=new_hashed, updated_at=datetime.now()))
        db.commit()
        return True

    @staticmethod
    def forgot_password_request(db, email: str) -> dict:
        user = db.execute(select(User).where(User.email == email)).scalars().first()
        if not user:
            # Chống dò quét email: Vẫn trả về thành công giả định
            return {"status": "OTP_SENT", "email": email}

        # Sinh mã OTP 6 số bảo mật
        otp = OtpService.create_secure_otp(db, user.id, "FORGOT_PASSWORD", 5)
        return {"status": "OTP_SENT", "email": email}

    @staticmethod
    def reset_password_with_otp(db, payload) -> bool:
        user = db.execute(select(User).where(User.email == payload.email)).scalars().first()
        if not user:
            raise FintechBaseException(error_code=SystemConstants.USER_NOT_FOUND, status_code=404)

        # Xác thực và tiêu hủy mã OTP
        OtpService.verify_and_consume_otp(db, user.id, str(payload.otp_code).strip(), "FORGOT_PASSWORD")

        new_hashed = CryptoService.hash_password(payload.new_password)
        db.execute(update(User).where(User.id == user.id).values(password_hash=new_hashed, updated_at=datetime.now()))
        db.commit()
        return True

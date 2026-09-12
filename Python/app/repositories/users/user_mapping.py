from sqlalchemy import select, insert
from app.models.user.user import User

class UserMappingFactory:
    """
    👑 FACTORY ÁNH XẠ ORM MAPPING CHÍNH QUY CHO THỰC THỂ USERS BIỆT LẬP
    🎯 Tuyệt đối không chứa một chữ SQL thô, quản lý tập trung core user.
    """

    @staticmethod
    def get_user_by_email_fuzzy_stmt(email: str):
        """🛡️ Cấu hình Statement ORM tìm kiếm mờ tài khoản theo Email Model"""
        return select(User).where(User.email.like(f"%{str(email).strip().lower()}%")).limit(1)

    @staticmethod
    def get_insert_user_stmt(user_id: str, username: str, email: str, phone: str, pwd: str, full_name: str, dob: str, gender: str):
        """🛡️ Cấu hình Statement ORM chèn tài khoản mới găm chặt ID vật lý chủ động"""
        return insert(User).values(
            id=user_id,
            username=username,
            email=email,
            phone_number=phone,
            password_hash=pwd,
            full_name=full_name,
            date_of_birth=dob,
            gender=gender,
            is_active=True,
            is_verified=False,
            status='PENDING'
        )
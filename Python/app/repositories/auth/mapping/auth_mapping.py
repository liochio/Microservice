from sqlalchemy import select, insert, func
from app.models.user.user import User

class AuthMappingFactory:
    """
    👑 FACTORY ÁNH XẠ ORM MAPPING CHÍNH QUY CỦA SẾP
    🎯 Tuyệt đối không chứa một chữ SQL thô text() rác rưởi
    """
    @staticmethod
    def get_check_email_stmt(email: str):
        return select(User.id).where(User.email == email).limit(1)

    @staticmethod
    def get_check_phone_stmt(phone_number: str):
        return select(User.id).where(User.phone_number == phone_number).limit(1)

    @staticmethod
    def get_check_username_stmt(username: str):
        return select(User.id).where(User.username == username).limit(1)

    @staticmethod
    def get_insert_user_stmt(username, email, phone, pwd, full_name, dob, gender):
        return insert(User).values(
            username=username,
            email=email,
            phone_number=phone,
            password_hash=pwd,
            full_name=full_name,
            date_of_birth=dob,
            gender=gender,
            is_active=True,
            is_verified=False,
            status='ACTIVE'
        )

    @staticmethod
    def get_user_by_email_stmt(email: str):
        clean_val = str(email).strip().lower()
        return select(User).where(
            (func.lower(User.email) == clean_val) | (func.lower(User.username) == clean_val)
        ).limit(1)

    # 👑 PHẦN PHÁT SINH MỚI CHUẨN HÓA: Statement tìm kiếm mờ chính quy qua Model
    @staticmethod
    def get_user_by_email_fuzzy_stmt(email: str):
        return select(User).where(User.email.like(f"%{str(email).strip().lower()}%")).limit(1)

    # ==============================================================================
    # 🎯 ĐÁNH DẤU CHỈNH SỬA: BỔ SUNG STATEMENT BACKUP THUẦN ORM ĐỂ KHAI TỬ TEXT() THÔ
    # ==============================================================================
    @staticmethod
    def get_user_backup_stmt(email: str):
        """🛡️ Statement cứu hộ chuẩn ORM mapping không phân biệt hoa thường, hỗ trợ cả email lẫn username"""
        clean_val = str(email).strip().lower()
        return select(User.id, User.username, User.email, User.password_hash, User.status).where(
            (func.lower(User.email) == clean_val) | (func.lower(User.username) == clean_val)
        ).limit(1)
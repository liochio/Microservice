from app.repositories.users.user_mapping import UserMappingFactory

class UserRepository:
    """
    👑 REPOSITORY QUẢN LÝ NGHIỆP VỤ USERS ĐỘC LẬP
    🎯 Thực thi 100% qua ORM Mapping chính quy của sếp, dẹp sạch rác rưởi SQL thô tại Service.
    """

    @staticmethod
    def get_user_by_email_fuzzy(db_conn, email: str):
        """🛡️ Mắt thần cứu hộ mờ luồng Login: Trả về Object đa hình thuần Python"""
        stmt = UserMappingFactory.get_user_by_email_fuzzy_stmt(email)
        result = db_conn.execute(stmt).fetchone()

        if result:
            row_data = result._mapping
            raw_hash = row_data.get("password_hash") or row_data.get("password")

            class PlainUserDTO:
                def __init__(self, **kwargs):
                    for k, v in kwargs.items():
                        setattr(self, k, v)

            return PlainUserDTO(
                id=str(row_data.get("id")),
                user_id=str(row_data.get("id")),
                username=str(row_data.get("username")),
                email=str(row_data.get("email")),
                password_hash=raw_hash,
                password=raw_hash,
                status=str(row_data.get("status", "ACTIVE"))
            )
        return None

    @staticmethod
    def insert_new_user(db_conn, user_id: str, username: str, email: str, phone: str, hashed_pwd: str, full_name: str, dob: str, gender: str) -> None:
        """🛡️ Thực thi thêm mới người dùng vào DB, găm cứng ID tránh lệch khóa ngoại bảng con"""
        stmt = UserMappingFactory.get_insert_user_stmt(user_id, username, email, phone, hashed_pwd, full_name, dob, gender)
        db_conn.execute(stmt)
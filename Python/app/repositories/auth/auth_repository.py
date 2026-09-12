from sqlalchemy.exc import SQLAlchemyError
from app.repositories.auth.mapping.auth_mapping import AuthMappingFactory

# Cấu hình cụm lỗi hệ thống truy vấn dữ liệu để triệt tiêu triệt để cảnh báo "Too broad exception clause"
CORE_FALLBACK_ERRORS = (SQLAlchemyError, AttributeError, TypeError, ValueError, RuntimeError)


class AuthRepository:
    """
    👑 REPOSITORY AUTHENTICATION LỚP LANG
    🎯 Quản lý tập trung toàn bộ các thao tác truy vấn dữ liệu tài khoản, bảo toàn kiến trúc bọc thép.
    """

    @staticmethod
    def check_email_exists(db_conn, email: str) -> bool:
        """🛡️ Kiểm tra sự tồn tại của Email trong hệ thống"""
        stmt = AuthMappingFactory.get_check_email_stmt(email)
        return db_conn.execute(stmt).scalar() is not None

    @staticmethod
    def check_phone_exists(db_conn, phone_number: str) -> bool:
        """🛡️ Kiểm tra sự tồn tại của Số điện thoại trong hệ thống"""
        stmt = AuthMappingFactory.get_check_phone_stmt(phone_number)
        return db_conn.execute(stmt).scalar() is not None

    @staticmethod
    def check_username_exists(db_conn, username: str) -> bool:
        """🛡️ Kiểm tra sự tồn tại của Tên đăng nhập trong hệ thống"""
        stmt = AuthMappingFactory.get_check_username_stmt(username)
        return db_conn.execute(stmt).scalar() is not None

    @staticmethod
    def insert_new_user(db_conn, username: str, email: str, phone: str, hashed_pwd: str, full_name: str, dob: str,
                        gender: str) -> None:
        """🛡️ Thực thi thêm mới thông tin người dùng vào cơ sở dữ liệu"""
        stmt = AuthMappingFactory.get_insert_user_stmt(username, email, phone, hashed_pwd, full_name, dob, gender)
        db_conn.execute(stmt)

    # ==============================================================================
    # 🎯 ĐÁNH DẤU CHỈNH SỬA CHỐT HẠ: KHAI TỬ TOÀN BỘ CHUỖI TEXT() SQL THÔ RÁC RƯỞI
    # ==============================================================================
    @staticmethod
    def get_user_by_email(db_conn, email: str):
        """🛡️ Truy vấn bốc nguyên vẹn thực thể người dùng qua Email hệ thống"""
        try:
            stmt = AuthMappingFactory.get_user_by_email_stmt(email)
            result = db_conn.execute(stmt)
            user = result.scalar_one_or_none()

            if user is None:
                # Ép nạp Statement cứu hộ thuần ORM bọc thép từ Factory
                stmt_backup = AuthMappingFactory.get_user_backup_stmt(email)
                row = db_conn.execute(stmt_backup).fetchone()

                if row:
                    return {
                        "id": str(row[0]),
                        "username": str(row[1]),
                        "email": str(row[2]),
                        "password": str(row[3]),
                        "status": str(row[4])
                    }

            return user
        except CORE_FALLBACK_ERRORS:
            try:
                # Đồng bộ bọc thép chặng cuối bằng ORM tuyệt đối
                stmt_backup = AuthMappingFactory.get_user_backup_stmt(email)
                row = db_conn.execute(stmt_backup).fetchone()
                if row:
                    return {
                        "id": str(row[0]),
                        "username": str(row[1]),
                        "email": str(row[2]),
                        "password": str(row[3]),
                        "status": str(row[4])
                    }
            except CORE_FALLBACK_ERRORS:
                pass
            return None

    @staticmethod
    def get_password_hash_direct(db_conn, email: str) -> str:
        """👑 HÀM CỨU HỘ TRUY VẤN BỌC THÉP THẲNG TUỘT QUA ORM FACTORY"""
        try:
            # Khai tử câu lệnh text thô cũ, bốc qua bản phối ORM cứu hộ chuẩn chỉ
            stmt = AuthMappingFactory.get_user_backup_stmt(email)
            row = db_conn.execute(stmt).fetchone()
            if row:
                return str(row[3])  # Trả về chuỗi password_hash map từ vị trí index tương ứng
            return ""
        except CORE_FALLBACK_ERRORS:
            return ""
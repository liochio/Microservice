import uuid
import secrets
from datetime import datetime, timedelta
from sqlalchemy import insert
from app.constants import SystemConstants
from app.models.user_otp.user_otp import UserOtp
from app.core.exceptions.base_exception import FintechBaseException


class OtpHelper:
    """
    👑 SYSTEM OTP UTILITY HELPER
    🎯 ĐẦU NÃO VẠN NĂNG: ĐÓNG GÓI TRỌN GÓI LOGIC SINH MÃ VÀ LƯU THẲNG XUỐNG DB QUA ORM MAPPING
    """

    @staticmethod
    def create_and_save_otp(db_conn, user_id: str, otp_type: str, expire_minutes: int = 5) -> str:
        """
        👑 HÀM TẠO VÀ LƯU OTP DÙNG CHUNG TOÀN HỆ THỐNG
        🎯 Bất kỳ Service nào sau này chỉ cần import và gọi đúng 1 dòng:
           otp_code = OtpHelper.create_and_save_otp(db_conn, user_id, "FORGET_PWD")
        """
        # 1. Sinh mã số OTP bảo mật tối cao bằng secrets
        otp_code = "".join([str(secrets.randbelow(10)) for _ in range(6)])

        # 2. Tính toán mốc thời gian sống
        now_time = datetime.now()
        expire_at = now_time + timedelta(minutes=expire_minutes)

        # 3. 👑 THỰC THI GĂM THẲNG THỰC THỂ XUỐNG BẢNG USER_OTPS BẰNG BIỂU THỨC ORM CHÍNH QUY
        try:
            stmt_insert_otp = insert(UserOtp).values(
                id=str(uuid.uuid4()),
                user_id=user_id,
                otp_code=otp_code,
                type=otp_type,
                content=f"OTP_{otp_type}:{otp_code}",
                is_used=0,
                status=SystemConstants.ACTIVE,
                expired_at=expire_at,
                created_at=now_time,
                updated_at=now_time
            )
            db_conn.execute(stmt_insert_otp)

            if hasattr(db_conn, "commit"):
                db_conn.commit()

            return otp_code
        except Exception:
            if hasattr(db_conn, "rollback"):
                db_conn.rollback()
            raise FintechBaseException(error_code=SystemConstants.OTP_GENERATION_DATABASE_FAILED, status_code=500)
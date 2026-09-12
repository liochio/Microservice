import uuid
import secrets
from datetime import datetime, timedelta
from sqlalchemy import select, update, insert

from app.constants import SystemConstants
from app.models.user_otp.user_otp import UserOtp
from app.core.exceptions.base_exception import FintechBaseException


class OtpService:
    """
    👑 OTP SERVICE FINAL (FIXED ORM + PLAIN OTP 6 DIGITS + RETRY)
    """

    @staticmethod
    def create_secure_otp(db_conn, user_id: str, otp_type: str, expire_minutes: int = 5) -> str:
        """
        ✅ Tạo OTP số vật lý nguyên bản 6 ký tự lưu trực tiếp vào bảng user_otps
        """
        plain_otp = "".join(str(secrets.randbelow(10)) for _ in range(6))
        now = datetime.now()

        stmt = insert(UserOtp).values(
            id=str(uuid.uuid4()),
            user_id=user_id,
            otp_code=plain_otp,
            type=otp_type,
            content=f"OTP_{otp_type}:{plain_otp}",
            is_used=0,
            status=SystemConstants.ACTIVE,
            retry_count=0,
            max_retries=3,
            expired_at=now + timedelta(minutes=expire_minutes),
            created_at=now,
            updated_at=now
        )
        db_conn.execute(stmt)
        return plain_otp

    @staticmethod
    def verify_and_consume_otp(db_conn, user_id: str, plain_otp: str, otp_type: str) -> bool:
        """
        ✅ VERIFY OTP FULL CHUẨN ĐỐI CHIẾU MÃ VẬT LÝ 6 SỐ
        """
        now = datetime.now()

        stmt = (
            select(UserOtp)
            .where(
                UserOtp.user_id == user_id,
                UserOtp.type == otp_type,
                UserOtp.is_used == 0,
                UserOtp.status == SystemConstants.ACTIVE
            )
            .order_by(UserOtp.created_at.desc())
            .limit(1)
        )
        otp_record = db_conn.execute(stmt).scalars().first()

        if not otp_record:
            raise FintechBaseException(SystemConstants.INVALID_OR_EXPIRED_OTP, 400)

        if otp_record.is_used == 1:
            raise FintechBaseException(SystemConstants.INVALID_OR_EXPIRED_OTP, 400)

        if otp_record.retry_count >= otp_record.max_retries or otp_record.status == "LOCKED":
            raise FintechBaseException(SystemConstants.OTP_BRUTE_FORCE_LOCKED, 403)

        if now > otp_record.expired_at:
            db_conn.execute(
                update(UserOtp)
                .where(UserOtp.id == otp_record.id)
                .values(status="TIMEOUT", updated_at=now)
            )
            raise FintechBaseException(SystemConstants.OTP_TIMEOUT_EXPIRED, 400)

        if otp_record.otp_code != plain_otp:
            new_retry = otp_record.retry_count + 1
            is_locked = new_retry >= otp_record.max_retries
            db_conn.execute(
                update(UserOtp)
                .where(UserOtp.id == otp_record.id)
                .values(
                    retry_count=new_retry,
                    updated_at=now,
                    status="LOCKED" if is_locked else SystemConstants.ACTIVE
                )
            )
            if is_locked:
                raise FintechBaseException(SystemConstants.OTP_BRUTE_FORCE_LOCKED, 403)
            raise FintechBaseException(SystemConstants.INVALID_OTP_CODE, 400)

        db_conn.execute(
            update(UserOtp)
            .where(UserOtp.id == otp_record.id)
            .values(is_used=1, status="VERIFIED", updated_at=now)
        )
        return True
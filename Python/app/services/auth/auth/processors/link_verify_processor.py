import re
from datetime import datetime, timedelta
from sqlalchemy import text
from app.constants import SystemConstants
from app.core.exceptions.base_exception import FintechBaseException
from app.services.auth.auth.otp_service import OtpService

class LinkVerifyProcessor:
    @staticmethod
    def process(db_conn, token: str) -> dict:
        query_check = text("SELECT nl.id, n.user_id, nl.gateway_response FROM notification_logs nl JOIN notifications n ON nl.notification_id = n.id WHERE nl.gateway_response LIKE :token AND nl.status = 'PENDING' LIMIT 1")
        record = db_conn.execute(query_check, {"token": f'%"{token}"%'}).fetchone()

        if not record:
            raise FintechBaseException(error_code=SystemConstants.INVALID_OR_USED_LINK_TOKEN, status_code=400)
        log_id, user_id, gateway_response = record[0], record[1], record[2]

        try:
            exp_str = re.search(r'"expire_at":\s*"([^"]+)"', gateway_response).group(1)
            expire_at = datetime.strptime(exp_str, "%Y-%m-%d %H:%M:%S")
        except Exception:
            expire_at = datetime.now() - timedelta(seconds=1)

        if datetime.now() > expire_at:
            db_conn.execute(text("UPDATE notification_logs SET status = 'FAILED' WHERE id = :id"), {"id": log_id})
            raise FintechBaseException(error_code=SystemConstants.LINK_TOKEN_EXPIRED, status_code=400)

        # 👑 ĐÁNH DẤU CHỈNH SỬA: Gọi OTP Service chuẩn hóa
        plain_otp = OtpService.create_secure_otp(db_conn, str(user_id), "REGISTER", 5)
        db_conn.execute(text("UPDATE notification_logs SET status = 'SUCCESS' WHERE id = :id"), {"id": log_id})

        return {"user_id": user_id, "otp_code": plain_otp, "expired_at": (datetime.now() + timedelta(minutes=5)).strftime("%Y-%m-%d %H:%M:%S")}
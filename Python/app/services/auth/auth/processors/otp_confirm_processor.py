import uuid
import random
from sqlalchemy import text
from app.constants import SystemConstants
from app.repositories.auth.auth_repository import AuthRepository
from app.services.auth.auth.otp_service import OtpService
from app.core.exceptions.base_exception import FintechBaseException

class OtpConfirmProcessor:
    @staticmethod
    def process(db_conn, payload) -> dict:
        user_data = AuthRepository.get_user_by_email(db_conn, str(payload.email).strip().lower())
        if not user_data:
            raise FintechBaseException(error_code=SystemConstants.USER_NOT_FOUND, status_code=404)

        user_id = str(getattr(user_data, "id", None) or user_data.get("id"))

        # 👑 Triệu hồi Verify OTP bảo mật cao
        OtpService.verify_and_consume_otp(db_conn, str(user_id), str(payload.otp).strip(), "REGISTER")

        # 1. Kích hoạt trạng thái người dùng sang ACTIVE
        db_conn.execute(
            text("UPDATE users SET status = 'ACTIVE', is_active = 1, is_verified = 1 WHERE id = :u_id"),
            {"u_id": user_id}
        )

        # 2. Gán Vai trò mặc định 'USER' nếu chưa có
        user_role_id = db_conn.execute(
            text("SELECT id FROM roles WHERE name = 'USER' LIMIT 1")
        ).scalar()
        if user_role_id:
            existing_ur = db_conn.execute(
                text("SELECT id FROM user_roles WHERE user_id = :u_id AND role_id = :r_id LIMIT 1"),
                {"u_id": user_id, "r_id": user_role_id}
            ).fetchone()
            if not existing_ur:
                db_conn.execute(
                    text("INSERT INTO user_roles (id, user_id, role_id) VALUES (:id, :u_id, :r_id)"),
                    {"id": str(uuid.uuid4()), "u_id": user_id, "r_id": user_role_id}
                )

        # 3. Tạo Ví tiền mặt mặc định 'CASH' nếu chưa có ví
        existing_wallet = db_conn.execute(
            text("SELECT id FROM wallets WHERE user_id = :u_id AND is_deleted = 0 LIMIT 1"),
            {"u_id": user_id}
        ).fetchone()
        if not existing_wallet:
            rand_acc = f"{random.randint(1000000000, 9999999999)}"
            short_u = user_id[:8].upper()
            db_conn.execute(
                text("""
                    INSERT INTO wallets (id, user_id, wallet_code, name, wallet_type, wallet_account, balance, currency, color, icon, description, status, is_deleted, is_default)
                    VALUES (:id, :user_id, :wallet_code, :name, :wallet_type, :wallet_account, :balance, :currency, :color, :icon, :description, 'ACTIVE', 0, 1)
                """),
                {
                    "id": str(uuid.uuid4()),
                    "user_id": user_id,
                    "wallet_code": f"CASH_{short_u}",
                    "name": "Ví Tiền Mặt Chính",
                    "wallet_type": "CASH",
                    "wallet_account": rand_acc,
                    "balance": 0.0,
                    "currency": "VND",
                    "color": "#388E3C",
                    "icon": "cash",
                    "description": "Ví tiền mặt mặc định khi khởi tạo tài khoản"
                }
            )

        # 4. Commit bảo vệ toàn vẹn giao dịch vật lý
        if hasattr(db_conn, "commit"):
            db_conn.commit()

        return {"user_id": user_id, "status": SystemConstants.ACTIVE}
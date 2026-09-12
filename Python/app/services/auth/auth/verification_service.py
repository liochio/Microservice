from datetime import datetime
from typing import Any, Optional
import json
import uuid
import random
from sqlalchemy import select, update, text

from app.constants import SystemConstants
from app.models.user.user import User
from app.models.notification.notification import Notification
from app.models.notification.notification_log import NotificationLog

from app.core.exceptions.base_exception import FintechBaseException
from app.repositories.notification.notification_repository import NotificationRepository
from app.repositories.notification.notification_log_query_repository import NotificationLogQueryRepository


class VerificationService:
    """
    👑 VERIFICATION SERVICE (ENTERPRISE FORENSIC VERSION)
    """

    @staticmethod
    def process_verification_token(db_conn, token: str, trace_id: Any = None) -> dict:
        now = datetime.now()

        if not token or str(token).strip() == "":
            NotificationRepository.insert_pure_security_log(
                db_conn, user_id=None, event_type="EMPTY_TOKEN_SUBMISSION",
                description=f"[{trace_id}] Nguoi dung gui token rong hoac null.", severity="MEDIUM"
            )
            raise FintechBaseException(SystemConstants.TOKEN_NULL, 400)

        log_row, payload = NotificationLogQueryRepository.find_pending_token(db_conn, token)

        if not log_row:
            NotificationRepository.insert_pure_security_log(
                db_conn, user_id=None, event_type="TOKEN_NOT_FOUND",
                description=f"[{trace_id}] Token {token} khong ton tai trong he thong.", severity="HIGH"
            )
            raise FintechBaseException(SystemConstants.TOKEN_NOT_FOUND, 400)

        expire_at = payload.get("expire_at") if isinstance(payload, dict) else None
        if expire_at:
            try:
                exp_str = str(expire_at).strip()
                if " " in exp_str:
                    exp_dt = datetime.strptime(exp_str, "%Y-%m-%d %H:%M:%S")
                else:
                    exp_dt = datetime.fromisoformat(exp_str)

                if datetime.now() > exp_dt:
                    raise FintechBaseException(SystemConstants.TOKEN_EXPIRED, 400)
            except FintechBaseException:
                raise
            except Exception:
                pass

        notification_id = log_row.notification_id

        stmt = select(Notification.user_id).where(Notification.id == notification_id)
        user_id = db_conn.execute(stmt).scalar_one_or_none()

        if not user_id:
            raise FintechBaseException(SystemConstants.USER_NOT_FOUND, 404)

        user = db_conn.execute(
            select(User).where(User.id == user_id)
        ).scalar_one_or_none()

        if not user:
            raise FintechBaseException(SystemConstants.USER_NOT_FOUND, 404)

        if user.status == SystemConstants.ACTIVE:
            raise FintechBaseException(SystemConstants.USER_ALREADY_ACTIVE, 400)

        return {
            "notification_id": notification_id,
            "user_id": user_id
        }

    @staticmethod
    def validate_and_activate_user(
        db_conn,
        user_id: Optional[str] = None,
        username: Optional[str] = None,
        notification_id: Optional[str] = None,
        otp_code: Optional[str] = None,
        trace_id: Any = None
    ):
        """
        👑 STEP: verify OTP -> activate user đồng bộ Core và FinTech
        """
        now = datetime.now()

        if not otp_code or str(otp_code).strip() == "":
            raise FintechBaseException(SystemConstants.INVALID_OTP_FORMAT, 400)

        # 1. TÌM KIẾM USER
        user = None
        if user_id:
            user = db_conn.execute(select(User).where(User.id == user_id)).scalar_one_or_none()
        
        if not user and username:
            clean_name = str(username).strip()
            user = db_conn.execute(
                select(User).where((User.username == clean_name) | (User.email == clean_name.lower()))
            ).scalar_one_or_none()

        if not user and notification_id:
            stmt_uid = select(Notification.user_id).where(Notification.id == notification_id)
            found_uid = db_conn.execute(stmt_uid).scalar_one_or_none()
            if found_uid:
                user = db_conn.execute(select(User).where(User.id == found_uid)).scalar_one_or_none()

        if not user:
            raise FintechBaseException(SystemConstants.USER_NOT_FOUND, 404)

        resolved_user_id = user.id

        # 2. KIỂM TRA TRẠNG THÁI ACTIVE
        if user.status == SystemConstants.ACTIVE and user.is_active and user.is_verified:
            # Người dùng đã kích hoạt rồi, hoàn tất thành công
            return

        # 3. TÌM THÔNG BÁO & LOG CHỨA MÃ OTP
        log_record = None
        target_notification_id = notification_id

        if not target_notification_id:
            stmt_noti = select(Notification.id).where(
                Notification.user_id == resolved_user_id
            ).order_by(Notification.created_at.desc()).limit(1)
            target_notification_id = db_conn.execute(stmt_noti).scalar()

        if target_notification_id:
            stmt_log = select(NotificationLog).where(
                NotificationLog.notification_id == target_notification_id
            ).order_by(NotificationLog.created_at.desc()).limit(1)
            log_record = db_conn.execute(stmt_log).scalar_one_or_none()

        if not log_record:
            # Tìm bất kỳ notification log nào gần nhất của user này
            stmt_any_log = select(NotificationLog).join(
                Notification, NotificationLog.notification_id == Notification.id
            ).where(Notification.user_id == resolved_user_id).order_by(NotificationLog.created_at.desc()).limit(1)
            log_record = db_conn.execute(stmt_any_log).scalar_one_or_none()

        if not log_record:
            raise FintechBaseException(SystemConstants.OTP_NOT_FOUND, 404)

        # 4. ĐỐI CHIẾU MÃ OTP VÀ KIỂM TRA THỜI GIAN HẾT HẠN
        expected_otp = None
        expire_dt = None

        try:
            payload = json.loads(log_record.gateway_response or "{}")
            expected_otp = payload.get("token") or payload.get("otp") or payload.get("otp_code")
            exp_str = payload.get("expire_at")
            if exp_str:
                exp_str = str(exp_str).strip()
                if " " in exp_str:
                    expire_dt = datetime.strptime(exp_str, "%Y-%m-%d %H:%M:%S")
                else:
                    expire_dt = datetime.fromisoformat(exp_str)
        except Exception:
            expected_otp = log_record.gateway_response

        if expire_dt and datetime.now() > expire_dt:
            db_conn.execute(
                update(NotificationLog).where(NotificationLog.id == log_record.id).values(status="TIMEOUT", updated_at=now)
            )
            if hasattr(db_conn, "commit"):
                db_conn.commit()
            raise FintechBaseException(SystemConstants.OTP_TIMEOUT_EXPIRED, 400)

        if not expected_otp or str(expected_otp).strip() != str(otp_code).strip():
            NotificationRepository.insert_pure_security_log(
                db_conn, user_id=resolved_user_id, event_type="INVALID_OTP_SUBMISSION",
                description=f"[{trace_id}] Nhap sai OTP cho user {user.username}.",
                severity="MEDIUM"
            )
            if hasattr(db_conn, "commit"):
                db_conn.commit()
            raise FintechBaseException(SystemConstants.INVALID_OTP_CODE, 400)

        # 5. KÍCH HOẠT THÀNH CÔNG VÀ ĐỒNG BỘ TOÀN HỆ THỐNG
        try:
            # Đổi trạng thái Log sang USED
            db_conn.execute(
                update(NotificationLog)
                .where(NotificationLog.id == log_record.id)
                .values(status="USED", updated_at=now)
            )

            # Đổi trạng thái Notification sang COMPLETED
            if target_notification_id:
                db_conn.execute(
                    update(Notification)
                    .where(Notification.id == target_notification_id)
                    .values(status="COMPLETED", is_read="READ", updated_at=now)
                )

            # Kích hoạt User trong liochio_fintech_db
            db_conn.execute(
                update(User)
                .where(User.id == resolved_user_id)
                .values(status=SystemConstants.ACTIVE, is_active=1, is_verified=1, updated_at=now)
            )

            # Đồng bộ sang liochio_auth_db.users
            try:
                db_conn.execute(text("""
                    UPDATE `liochio_auth_db`.`users`
                    SET `status` = 'ACTIVE', `is_email_verified` = 1, `updated_at` = NOW()
                    WHERE `username` = :uname
                """), {"uname": user.username})
            except Exception:
                pass

            # Gán Role USER mặc định nếu chưa có
            user_role_id = db_conn.execute(
                text("SELECT id FROM roles WHERE name IN ('USER', 'ROLE_USER', 'PARENT') LIMIT 1")
            ).scalar()
            if user_role_id:
                existing_ur = db_conn.execute(
                    text("SELECT id FROM user_roles WHERE user_id = :u_id AND role_id = :r_id LIMIT 1"),
                    {"u_id": resolved_user_id, "r_id": user_role_id}
                ).fetchone()
                if not existing_ur:
                    db_conn.execute(
                        text("INSERT INTO user_roles (id, user_id, role_id) VALUES (:id, :u_id, :r_id)"),
                        {"id": str(uuid.uuid4()), "u_id": resolved_user_id, "r_id": user_role_id}
                    )

            # Tạo Ví tiền mặt mặc định CASH nếu chưa có ví
            existing_wallet = db_conn.execute(
                text("SELECT id FROM wallets WHERE user_id = :u_id AND is_deleted = 0 LIMIT 1"),
                {"u_id": resolved_user_id}
            ).fetchone()
            if not existing_wallet:
                rand_acc = f"{random.randint(1000000000, 9999999999)}"
                short_u = str(resolved_user_id)[:8].upper()
                db_conn.execute(
                    text("""
                        INSERT INTO wallets (id, user_id, wallet_code, name, wallet_type, wallet_account, balance, currency, color, icon, description, status, is_deleted, is_default)
                        VALUES (:id, :user_id, :wallet_code, :name, :wallet_type, :wallet_account, :balance, :currency, :color, :icon, :description, 'ACTIVE', 0, 1)
                    """),
                    {
                        "id": str(uuid.uuid4()),
                        "user_id": resolved_user_id,
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

            # Đồng bộ Sổ cái Kép (Ledger Accounts) bên liochio_auth_db
            try:
                res_auth_user = db_conn.execute(
                    text("SELECT id FROM `liochio_auth_db`.`users` WHERE username = :uname LIMIT 1"),
                    {"uname": user.username}
                ).fetchone()
                if res_auth_user:
                    auth_uid = res_auth_user[0]
                    for acc_t in ['USER_AVAILABLE', 'USER_HOLDING', 'USER_ESCROW']:
                        acc_num = f"LEDGER_{acc_t}_{auth_uid}"
                        db_conn.execute(text("""
                            INSERT IGNORE INTO `liochio_auth_db`.`ledger_accounts` (
                                `tenant_id`, `account_number`, `user_id`, `account_type`, `currency`, `balance`, `status`, `version`, `created_at`, `updated_at`
                            ) VALUES (
                                'default', :acc_num, :uid, :acc_type, 'VND', 0.00, 'ACTIVE', 0, NOW(), NOW()
                            );
                        """), {"acc_num": acc_num, "uid": auth_uid, "acc_type": acc_t})
            except Exception:
                pass

            NotificationRepository.insert_pure_security_log(
                db_conn, user_id=resolved_user_id, event_type="USER_ACTIVATION_SUCCESS",
                description=f"[{trace_id}] Kích hoạt OTP thành công cho user {user.username}.",
                severity="INFO"
            )

            if hasattr(db_conn, "commit"):
                db_conn.commit()

        except FintechBaseException as f_exc:
            if hasattr(db_conn, "rollback"):
                db_conn.rollback()
            raise f_exc
        except Exception as e:
            if hasattr(db_conn, "rollback"):
                db_conn.rollback()
            raise FintechBaseException(SystemConstants.ACTIVATION_TX_COMMIT_FAILED, 500)

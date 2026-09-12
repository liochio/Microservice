import time
import uuid
import secrets
import smtplib
import json
import sys
from datetime import datetime, timedelta
from typing import Any
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
from sqlalchemy import select

if hasattr(sys.stdout, 'reconfigure'):
    try:
        sys.stdout.reconfigure(encoding='utf-8')
        sys.stderr.reconfigure(encoding='utf-8')
    except Exception:
        pass

from app.constants import SystemConstants
from app.db.session import SessionLocal
from app.models.notification.notification import Notification
from app.models.notification.notification_log import NotificationLog
from app.models.user.user import User
from app.repositories.notification.notification_repository import NotificationRepository
from app.core.config.settings import settings
from app.core.translator.translator_engine import i18n_translator


class NotificationWorker:
    """
    👑 NOTIFICATION BACKGROUND WORKER JOB (ENTERPRISE MIDDLEWARE VERSION)
    🎯 Vòng lặp ngầm quét bảng notifications liên tục, bốc queue gửi Mail qua SMTP vật lý ăn theo file .env.
    """

    SMTP_HOST = settings.SMTP_HOST
    SMTP_PORT = settings.SMTP_PORT
    SMTP_USER = settings.SMTP_USER
    SMTP_PASS = settings.SMTP_PASSWORD

    BASE_URL = getattr(settings, "APP_URL", "http://localhost:8000")

    @classmethod
    def send_email_via_smtp(cls, to_email: str, subject: str, html_content: str) -> str:
        """⚡ HÀM VẠN NĂNG: Khởi tạo socket và nện mail Rich Text HTML qua tổng đài SMTP vật lý"""
        try:
            msg = MIMEMultipart("alternative")
            msg["Subject"] = subject
            msg["From"] = cls.SMTP_USER
            msg["To"] = to_email

            part_html = MIMEText(html_content, "html", "utf-8")
            msg.attach(part_html)

            with smtplib.SMTP(cls.SMTP_HOST, cls.SMTP_PORT, timeout=15) as server:
                server.starttls()
                server.login(cls.SMTP_USER, cls.SMTP_PASS)
                server.sendmail(cls.SMTP_USER, to_email, msg.as_string())

            return "SUCCESS"
        except Exception as e:
            return f"SMTP_FAILED: {str(e)}"

    @classmethod
    def start_job_loop(cls, interval_seconds: int = 5):
        """👑 CORE LOOP: Đóng đinh vòng lặp vô hạn quét hàng đợi thông báo định kỳ"""
        print(f"[WORKER_START] Notification Background Worker dang khoi dong. Tan suat: {interval_seconds}s/lan.")
        print(f"[WORKER_CONFIG] Su dung tong dai mail: {cls.SMTP_USER}")

        while True:
            db_conn = SessionLocal()

            # 👑 FAKE MIDDLEWARE CONTEXT: Tự tạo trace_id riêng biệt cho mỗi phiên càn quét của Job để ghi vết log đồng bộ
            trace_id = f"WORKER-{str(uuid.uuid4())[:8].upper()}"

            try:
                # 1. Quét tìm danh sách các thông báo đang PENDING cần gửi Mail kích hoạt
                stmt_select = (
                    select(Notification)
                    .where(
                        Notification.status == "PENDING",
                        Notification.notification_type == "EMAIL_VERIFICATION"
                    )
                    .order_by(Notification.created_at.asc())
                    .limit(10)
                )
                pending_notifications = db_conn.execute(stmt_select).scalars().all()

                if not pending_notifications:
                    db_conn.close()
                    time.sleep(interval_seconds)
                    continue

                print(
                    f"[{trace_id}][WORKER_PROCESSING] Tim thay {len(pending_notifications)} thong bao dang cho xu ly...")

                for noti in pending_notifications:
                    current_time = datetime.now()

                    # 1. 👑 TRUY VẤN EMAIL THỰC TẾ CỦA NGƯỜI DÙNG TỪ DATABASE
                    stmt_user = select(User).where(User.id == noti.user_id).limit(1)
                    user_record = db_conn.execute(stmt_user).scalars().first()
                    user_email = user_record.email if (user_record and user_record.email) else "user@example.com"

                    # 2. 👑 ĐA NGÔN NGỮ (i18n): Dịch tiêu đề email theo cấu hình hệ thống
                    mock_request: Any = type('MockRequest', (object,), {"headers": {"accept-language": "vi"}})()
                    mail_subject = i18n_translator.translate(
                        mock_request,
                        error_code=SystemConstants.REGISTER_VERIFY_MAIL_SUBJECT,
                        msg_type=SystemConstants.MSG_TYPE_MESSAGE
                    )

                    # 3. 👑 LẤY TOKEN KÍCH HOẠT ĐÃ ĐƯỢC SINH TỪ BẢNG NOTIFICATION_LOGS
                    stmt_log = (
                        select(NotificationLog)
                        .where(NotificationLog.notification_id == noti.id, NotificationLog.status == "PENDING")
                        .limit(1)
                    )
                    log_record = db_conn.execute(stmt_log).scalars().first()

                    generated_token = None
                    if log_record and log_record.gateway_response:
                        try:
                            parsed_payload = json.loads(log_record.gateway_response)
                            generated_token = parsed_payload.get("token")
                        except Exception:
                            pass

                    if not generated_token:
                        generated_token = secrets.token_urlsafe(32)
                        expire_time_raw = current_time + timedelta(minutes=15)
                        expire_time_str = expire_time_raw.strftime("%Y-%m-%d %H:%M:%S")
                        gateway_payload = {
                            "token": generated_token,
                            "expire_at": expire_time_str,
                            "email": user_email
                        }
                        NotificationRepository.insert_worker_notification_log(
                            db_conn=db_conn,
                            notification_id=noti.id,
                            recipient=user_email,
                            gateway_response=json.dumps(gateway_payload),
                            status="PENDING"
                        )
                    else:
                        expire_time_raw = current_time + timedelta(minutes=15)
                        expire_time_str = expire_time_raw.strftime("%Y-%m-%d %H:%M:%S")

                    # Xây dựng URL tuyệt đối để click kích hoạt
                    full_activation_url = f"{cls.BASE_URL}/api/v1/auth/activate?token={generated_token}"

                    # Chuẩn bị nội dung HTML email
                    raw_db_template = noti.content if noti.content else ""
                    try:
                        rich_html_content = raw_db_template.format(
                            full_activation_url=full_activation_url,
                            expire_time_str=expire_time_str
                        )
                    except (KeyError, ValueError, IndexError):
                        rich_html_content = raw_db_template.replace("{full_activation_url}", full_activation_url).replace("{expire_time_str}", expire_time_str)

                    if not rich_html_content:
                        rich_html_content = f"""
                        <h2>Chào mừng bạn đến với Nền tảng Tài chính FinTech</h2>
                        <p>Vui lòng bấm vào liên kết sau để kích hoạt tài khoản của bạn:</p>
                        <p><a href="{full_activation_url}">{full_activation_url}</a></p>
                        <p>Liên kết có hiệu lực trong vòng 15 phút.</p>
                        """

                    # 4. GỬI MAIL QUA SMTP (HOẶC IN CONSOLE NẾU DEV)
                    if cls.SMTP_USER and cls.SMTP_PASS:
                        gateway_res = cls.send_email_via_smtp(
                            to_email=user_email,
                            subject=mail_subject,
                            html_content=rich_html_content
                        )
                    else:
                        # Chế độ Dev: In link ra console để test trực tiếp
                        print(f"\n📧 [DEV_EMAIL_SIMULATOR] Gửi mail tới: {user_email}")
                        print(f"👉 Link kích hoạt: {full_activation_url}\n")
                        gateway_res = "SUCCESS"

                    # 5. CẬP NHẬT TRẠNG THÁI NOTIFICATION
                    if gateway_res == "SUCCESS":
                        NotificationRepository.update_notification_status(db_conn, noti.id, "COMPLETED")
                        print(f"[{trace_id}][WORKER_SUCCESS] Da gui mail thanh cong toi [{user_email}].")
                    else:
                        NotificationRepository.update_notification_status(db_conn, noti.id, "FAILED")
                        print(f"[{trace_id}][WORKER_FAILED] Gui mail that bai cho ID [{noti.id}]: {gateway_res}")

                # Đóng đinh transaction an toàn toàn vẹn dữ liệu
                if hasattr(db_conn, "commit"):
                    db_conn.commit()

            except Exception as e:
                if hasattr(db_conn, "rollback"):
                    db_conn.rollback()
                try:
                    NotificationRepository.insert_pure_system_log(
                        db_conn, error_type="CRITICAL",
                        stack_trace=f"[{trace_id}] WORKER_CORE_LOOP_CRASH",
                        component=f"WORKER_ERROR: {str(e)[:40]}"
                    )
                except Exception:
                    pass
                print(f"[{trace_id}][WORKER_CRASH] Vong quet gap loi: {str(e)}")
            finally:
                db_conn.close()

            time.sleep(interval_seconds)


if __name__ == "__main__":
    NotificationWorker.start_job_loop(interval_seconds=5)
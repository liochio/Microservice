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


from sqlalchemy import select, text


class NotificationWorker:
    """
    👑 NOTIFICATION BACKGROUND WORKER JOB (ENTERPRISE MIDDLEWARE VERSION)
    🎯 Vòng lặp ngầm quét bảng notifications liên tục, bốc queue gửi Mail qua SMTP động theo cấu hình DB & .env.
    """

    BASE_URL = getattr(settings, "APP_URL", "http://localhost:8000")

    @classmethod
    def get_db_config(cls, db_conn, key: str, default: Any = "") -> str:
        """Đọc tham số cấu hình từ Database (system_settings / system_configs) với fallback sang default"""
        if db_conn is not None:
            try:
                row = db_conn.execute(
                    text("SELECT `value` FROM liochio_app_db.system_settings WHERE `key` = :k AND status = 'ACTIVE' LIMIT 1"),
                    {"k": key}
                ).fetchone()
                if row and row[0] is not None:
                    return str(row[0]).strip()
            except Exception:
                pass
            try:
                row = db_conn.execute(
                    text("SELECT config_value FROM liochio_core_db.system_configs WHERE config_key = :k AND is_active = 1 LIMIT 1"),
                    {"k": key}
                ).fetchone()
                if row and row[0] is not None:
                    return str(row[0]).strip()
            except Exception:
                pass
        return str(default) if default is not None else ""

    @classmethod
    def is_dummy_or_nonexistent_email(cls, email: str) -> bool:
        """Kiểm tra email có phải dạng kiểm thử, tài khoản ảo hoặc không tồn tại (vd: abc@gmail.com, test@...)"""
        if not email or not isinstance(email, str):
            return True
        clean = email.strip().lower()
        if "@" not in clean or "." not in clean or len(clean) < 6:
            return True
        dummy_list = [
            "abc@gmail.com", "test@gmail.com", "user@gmail.com", "demo@gmail.com",
            "dummy@gmail.com", "sample@gmail.com", "test@example.com", "user@example.com"
        ]
        if clean in dummy_list:
            return True
        if clean.startswith("test") or clean.startswith("dummy") or clean.startswith("fake") or "example.com" in clean or clean.endswith(".test"):
            return True
        return False

    @classmethod
    def resolve_recipient(cls, to_email: str, db_conn=None) -> tuple[str, bool]:
        """
        Xác định hòm thư nhận thực tế:
        Nếu email người nhận là test/không tồn tại (vd: abc@gmail.com) và cấu hình mail.fallback_to_default_recipient = true (mặc định là có),
        hệ thống sẽ chuyển tiếp gửi tới mail mặc định (mail.default_recipient từ DB).
        """
        fallback_enabled = cls.get_db_config(db_conn, "mail.fallback_to_default_recipient", "true")
        if fallback_enabled.lower() in ("1", "true", "yes", "on"):
            if cls.is_dummy_or_nonexistent_email(to_email):
                default_recipient = cls.get_db_config(db_conn, "mail.default_recipient", "voduylebt99@gmail.com")
                print(f"[NOTI_WORKER] ⚠️ Người nhận [{to_email}] là email test/không có thật. "
                      f"Kích hoạt chuyển tiếp mail mặc định tới: [{default_recipient}]")
                return default_recipient, True
        return to_email, False

    @classmethod
    def send_email_via_smtp(cls, to_email: str, subject: str, html_content: str, db_conn=None) -> str:
        """⚡ Khởi tạo socket và gửi mail Rich Text HTML qua tổng đài SMTP lấy từ DB cấu hình"""
        smtp_host = cls.get_db_config(db_conn, "smtp.host", settings.SMTP_HOST or "smtp.gmail.com")
        smtp_port_val = cls.get_db_config(db_conn, "smtp.port", settings.SMTP_PORT or 587)
        try:
            smtp_port = int(smtp_port_val)
        except ValueError:
            smtp_port = 587
        smtp_user = cls.get_db_config(db_conn, "smtp.username", settings.SMTP_USER)
        smtp_pass = cls.get_db_config(db_conn, "smtp.password", settings.SMTP_PASSWORD)
        from_email = cls.get_db_config(db_conn, "smtp.from_email", smtp_user or "no-reply@liochio.vn")

        effective_recipient, is_redirected = cls.resolve_recipient(to_email, db_conn)
        final_subject = f"[Chuyển tiếp từ: {to_email}] {subject}" if is_redirected else subject
        prefix_banner = ""
        if is_redirected:
            prefix_banner = (
                f'<div style="background-color: #fef3c7; border: 1px solid #f59e0b; color: #92400e; padding: 12px; border-radius: 8px; margin-bottom: 16px; font-family: sans-serif;">'
                f'⚠️ <strong>Thông báo Hệ thống:</strong> Email này được chuyển tiếp tự động từ người nhận không tồn tại/kiểm thử: <code>{to_email}</code> tới hòm thư mặc định được cấu hình trong DB.'
                f'</div>'
            )

        if not smtp_user or not smtp_pass:
            print(f"📧 [DEV_EMAIL_SIMULATOR] Gửi mail tới: {effective_recipient} (Gốc: {to_email}) | Subject: {final_subject}")
            return "SUCCESS"

        try:
            msg = MIMEMultipart("alternative")
            msg["Subject"] = final_subject
            msg["From"] = from_email
            msg["To"] = effective_recipient

            full_html = prefix_banner + html_content
            part_html = MIMEText(full_html, "html", "utf-8")
            msg.attach(part_html)

            with smtplib.SMTP(smtp_host, smtp_port, timeout=15) as server:
                server.starttls()
                server.login(smtp_user, smtp_pass)
                server.sendmail(from_email, effective_recipient, msg.as_string())

            print(f"[NOTI_WORKER] 📨 Đã gửi mail SMTP thành công tới [{effective_recipient}] (Gốc: [{to_email}])")
            return "SUCCESS"
        except Exception as e:
            print(f"[NOTI_WORKER] Lỗi gửi SMTP tới [{effective_recipient}]: {e}")
            # Thử gửi fallback lần 2 tới default_recipient nếu người nhận đầu tiên thất bại
            default_recipient = cls.get_db_config(db_conn, "mail.default_recipient", "voduylebt99@gmail.com")
            fallback_enabled = cls.get_db_config(db_conn, "mail.fallback_to_default_recipient", "true")
            if effective_recipient != default_recipient and fallback_enabled.lower() in ("1", "true", "yes", "on"):
                try:
                    print(f"[NOTI_WORKER] 🔄 Đang gửi lại tới email mặc định dự phòng [{default_recipient}]...")
                    msg2 = MIMEMultipart("alternative")
                    msg2["Subject"] = f"[DỰ PHÒNG CHUYỂN TIẾP - Gốc: {to_email}] {subject}"
                    msg2["From"] = from_email
                    msg2["To"] = default_recipient
                    notice = f'<p style="color:red;"><b>Ghi chú:</b> Gửi tới hòm thư gốc {to_email} thất bại ({e}), hệ thống tự động chuyển tiếp tới hòm thư mặc định.</p>'
                    msg2.attach(MIMEText(notice + html_content, "html", "utf-8"))
                    with smtplib.SMTP(smtp_host, smtp_port, timeout=15) as server:
                        server.starttls()
                        server.login(smtp_user, smtp_pass)
                        server.sendmail(from_email, default_recipient, msg2.as_string())
                    print(f"[NOTI_WORKER] 📨 Đã gửi lại thành công tới hòm thư mặc định [{default_recipient}]")
                    return "SUCCESS"
                except Exception as ex2:
                    return f"SMTP_FAILED: {str(e)} -> FALLBACK_FAILED: {str(ex2)}"
            return f"SMTP_FAILED: {str(e)}"
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

                    # 4. GỬI MAIL QUA SMTP ĐỘNG (HOẶC IN CONSOLE NẾU DEV)
                    gateway_res = cls.send_email_via_smtp(
                        to_email=user_email,
                        subject=mail_subject,
                        html_content=rich_html_content,
                        db_conn=db_conn
                    )

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

    @classmethod
    def process_pending_notifications(cls):
        """Hàm chạy 1 chu kỳ quét thông báo hàng chờ cho scheduler bên ngoài"""
        db_conn = SessionLocal()
        trace_id = f"CRON-{str(uuid.uuid4())[:8].upper()}"
        try:
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
                return

            print(f"[{trace_id}][CRON_NOTIFICATION] Tim thay {len(pending_notifications)} thong bao can gui...")
            for noti in pending_notifications:
                current_time = datetime.now()
                stmt_user = select(User).where(User.id == noti.user_id).limit(1)
                user_record = db_conn.execute(stmt_user).scalars().first()
                user_email = user_record.email if (user_record and user_record.email) else "user@example.com"

                mail_subject = "[Liochio FinTech] Kich Hoat Tai Khoan Nguoi Dung"

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
                    expire_time_str = (current_time + timedelta(minutes=15)).strftime("%Y-%m-%d %H:%M:%S")
                    gateway_payload = {"token": generated_token, "expire_at": expire_time_str, "email": user_email}
                    NotificationRepository.insert_worker_notification_log(
                        db_conn=db_conn, notification_id=noti.id, recipient=user_email,
                        gateway_response=json.dumps(gateway_payload), status="PENDING"
                    )
                else:
                    expire_time_str = (current_time + timedelta(minutes=15)).strftime("%Y-%m-%d %H:%M:%S")

                full_activation_url = f"{cls.BASE_URL}/api/v1/auth/activate?token={generated_token}"
                rich_html_content = f"""
                <div style="font-family: Arial, sans-serif; padding: 20px; background: #0f172a; color: #e2e8f0; border-radius: 12px;">
                    <h2 style="color: #10b981;">Chào mừng đến với Liochio FinTech!</h2>
                    <p>Vui lòng bấm vào liên kết sau để kích hoạt tài khoản của bạn:</p>
                    <p><a href="{full_activation_url}" style="color: #38bdf8;">Kích Hoạt Tài Khoản Ngay</a></p>
                    <p style="font-size: 12px; color: #64748b;">Liên kết có hiệu lực trong vòng 15 phút ({expire_time_str}).</p>
                </div>
                """

                gateway_res = cls.send_email_via_smtp(
                    to_email=user_email,
                    subject=mail_subject,
                    html_content=rich_html_content,
                    db_conn=db_conn
                )
                if gateway_res == "SUCCESS":
                    NotificationRepository.update_notification_status(db_conn, noti.id, "COMPLETED")
                else:
                    NotificationRepository.update_notification_status(db_conn, noti.id, "FAILED")

            if hasattr(db_conn, "commit"):
                db_conn.commit()
        except Exception as e:
            if hasattr(db_conn, "rollback"):
                db_conn.rollback()
            print(f"[{trace_id}][CRON_ERROR] {e}")
        finally:
            db_conn.close()


def execute_notification_cron_job():
    """Hàm thực thi một chu kỳ quét hàng chờ thông báo dành cho APScheduler trong run_worker.py"""
    NotificationWorker.process_pending_notifications()


if __name__ == "__main__":
    NotificationWorker.start_job_loop(interval_seconds=5)
"""
Wallet Event Consumer (RabbitMQ / Kafka / Celery)
Chịu trách nhiệm tiêu thụ các sự kiện liên quan đến trạng thái ví (khóa/mở khóa/cảnh báo hạn mức)
"""
import json
import logging
from datetime import datetime
from typing import Dict, Any
from sqlalchemy import text

from app.db.session import SessionLocal
from app.jobs.notification_worker import NotificationWorker

logger = logging.getLogger("WalletConsumer")


def process_wallet_event(event_payload: Dict[str, Any]):
    """
    Xử lý message sự kiện thay đổi trạng thái ví:
    - Cảnh báo ví bị đóng băng do an ninh
    - Cảnh báo ví sắp vượt hạn mức chi tiêu
    """
    db = SessionLocal()
    try:
        user_id = event_payload.get("user_id")
        wallet_id = event_payload.get("wallet_id")
        event_type = event_payload.get("event_type", "WALLET_STATUS_CHANGED")
        status = event_payload.get("status")
        reason = event_payload.get("reason", "Thông báo quản trị ví")

        logger.info("[WALLET_CONSUMER] Event %s cho wallet=%s, status=%s", event_type, wallet_id, status)

        if user_id:
            user_row = db.execute(
                text("SELECT email, full_name FROM liochio_app_db.users WHERE id = :uid LIMIT 1"),
                {"uid": user_id}
            ).fetchone()

            if user_row and user_row[0]:
                email = user_row[0]
                name = user_row[1] or "Quý khách"
                html_body = f"""
                <div style="font-family: Arial, sans-serif; padding: 20px; border: 1px solid #f59e0b; border-radius: 8px;">
                    <h3 style="color: #d97706;">Cập Nhật Trạng Thái Ví Liochio</h3>
                    <p>Kính chào <b>{name}</b>,</p>
                    <p>Hệ thống vừa cập nhật trạng thái ví điện tử của bạn:</p>
                    <ul>
                        <li><b>Mã ví:</b> {wallet_id}</li>
                        <li><b>Trạng thái mới:</b> {status}</li>
                        <li><b>Lý do:</b> {reason}</li>
                        <li><b>Thời gian:</b> {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}</li>
                    </ul>
                    <p style="color: #64748b; font-size: 13px;">Nếu bạn không thực hiện yêu cầu này, vui lòng liên hệ bộ phận hỗ trợ khách hàng ngay lập tức.</p>
                </div>
                """
                NotificationWorker.send_email_via_smtp(
                    to_email=email,
                    subject=f"[Liochio] Cập nhật trạng thái ví: {status}",
                    html_content=html_body,
                    db_conn=db
                )
    except Exception as e:
        logger.error("[WALLET_CONSUMER] Loi xu ly wallet event: %s", e, exc_info=True)
    finally:
        db.close()


if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)
    logger.info("Khoi chay Wallet Consumer...")

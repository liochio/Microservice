"""
Notification Event Consumer (RabbitMQ / Kafka / Celery / Threaded Queue)
Chịu trách nhiệm tiêu thụ các sự kiện thông báo và gửi Email qua SMTP hoặc Push Notification
"""
import json
import logging
from typing import Dict, Any

from app.db.session import SessionLocal
from app.jobs.notification_worker import NotificationWorker

logger = logging.getLogger("NotificationConsumer")


def process_notification_event(event_payload: Dict[str, Any]):
    """
    Xử lý message sự kiện gửi thông báo / email.
    Hỗ trợ định tuyến email ảo (abc@gmail.com) sang default_recipient theo cấu hình database.
    """
    db = SessionLocal()
    try:
        recipient = event_payload.get("recipient") or event_payload.get("to_email")
        subject = event_payload.get("subject", "Thông báo từ Hệ thống Liochio FinTech")
        body = event_payload.get("body") or event_payload.get("html_content", "")
        
        if not recipient:
            logger.warning("[CONSUMER] Event thieu dia chi nguoi nhan, bo qua: %s", event_payload)
            return

        logger.info("[CONSUMER] Nhan yeu cau gui mail toi: %s - Tieu de: %s", recipient, subject)
        result = NotificationWorker.send_email_via_smtp(
            to_email=recipient,
            subject=subject,
            html_content=body,
            db_conn=db
        )
        logger.info("[CONSUMER] Ket qua gui mail: %s", result)
    except Exception as e:
        logger.error("[CONSUMER] Loi xu ly notification event: %s", e, exc_info=True)
    finally:
        db.close()


if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)
    logger.info("Khoi chay Notification Consumer...")

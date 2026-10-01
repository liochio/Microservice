"""
Transaction Event Consumer (RabbitMQ / Kafka / Celery)
Chịu trách nhiệm tiêu thụ các sự kiện biến động số dư và giao dịch tài chính
"""
import json
import logging
from datetime import datetime
from typing import Dict, Any
from sqlalchemy import text

from app.db.session import SessionLocal
from app.jobs.notification_worker import NotificationWorker

logger = logging.getLogger("TransactionConsumer")


def process_transaction_event(event_payload: Dict[str, Any]):
    """
    Xử lý message sự kiện biến động giao dịch:
    - Ghi nhận trạng thái hoàn tất giao dịch
    - Gửi email biên lai giao dịch cho khách hàng
    """
    db = SessionLocal()
    try:
        user_id = event_payload.get("user_id")
        amount = float(event_payload.get("amount", 0.0))
        tx_type = event_payload.get("tx_type", "EXPENSE")
        description = event_payload.get("description", "Giao dịch tài chính")
        wallet_id = event_payload.get("wallet_id")

        logger.info("[TX_CONSUMER] Xu ly giao dich cho user=%s, amount=%.2f, type=%s", user_id, amount, tx_type)

        # Lấy thông tin email người dùng để gửi biên lai
        if user_id:
            user_row = db.execute(
                text("SELECT email, full_name FROM liochio_app_db.users WHERE id = :uid LIMIT 1"),
                {"uid": user_id}
            ).fetchone()

            if user_row and user_row[0]:
                email = user_row[0]
                name = user_row[1] or "Quý khách"
                html_body = f"""
                <div style="font-family: Arial, sans-serif; padding: 20px; border: 1px solid #e2e8f0; border-radius: 8px;">
                    <h3 style="color: #2563eb;">Biên Lai Biến Động Số Dư Liochio FinTech</h3>
                    <p>Kính chào <b>{name}</b>,</p>
                    <p>Giao dịch của bạn đã được ghi nhận thành công trên hệ thống Sổ Cái Liochio:</p>
                    <ul>
                        <li><b>Loại giao dịch:</b> {tx_type}</li>
                        <li><b>Số tiền:</b> {amount:,.0f} VND</li>
                        <li><b>Nội dung:</b> {description}</li>
                        <li><b>Thời gian:</b> {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}</li>
                    </ul>
                    <p style="color: #64748b; font-size: 13px;">Cảm ơn bạn đã sử dụng dịch vụ của Liochio FinTech.</p>
                </div>
                """
                NotificationWorker.send_email_via_smtp(
                    to_email=email,
                    subject=f"[Liochio] Biên lai biến động số dư: {tx_type} {amount:,.0f} VND",
                    html_content=html_body,
                    db_conn=db
                )
    except Exception as e:
        logger.error("[TX_CONSUMER] Loi xu ly transaction event: %s", e, exc_info=True)
    finally:
        db.close()


if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)
    logger.info("Khoi chay Transaction Consumer...")

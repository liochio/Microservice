import json
from sqlalchemy import select
from app.models.notification.notification_log import NotificationLog


class NotificationLogQueryRepository:

    @staticmethod
    def find_pending_token(db_conn, token: str):
        stmt = select(NotificationLog).where(NotificationLog.status == "PENDING")
        rows = db_conn.execute(stmt).scalars().all()

        for row in rows:
            try:
                payload = json.loads(row.gateway_response or "{}")
                if payload.get("token") == token:
                    return row, payload
            except json.JSONDecodeError:
                continue

        return None, None
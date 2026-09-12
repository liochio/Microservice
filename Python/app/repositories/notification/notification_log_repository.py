from sqlalchemy import select
from app.models.user.user import User
from app.repositories.notification.notification_mapping import NotificationLogFactory


class NotificationLogRepository:

    @staticmethod
    def insert_notification_log(
        db_conn,
        notification_id: str,
        user_id: str,
        channel: str,
        status: str,
        provider_response: str,
        recipient_target: str = None
    ):
        if not recipient_target:
            stmt = select(User.email).where(User.id == user_id)
            recipient_target = db_conn.execute(stmt).scalar_one_or_none() or "unknown@fintech.com"

        params = NotificationLogFactory.build_params(
            notification_id,
            user_id,
            channel,
            status,
            provider_response
        )

        params["recipient_target"] = recipient_target

        db_conn.execute(NotificationLogFactory.get_insert_sql(), params)
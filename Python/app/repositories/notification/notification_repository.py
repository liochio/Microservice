import uuid
from datetime import datetime
from sqlalchemy import text, insert, update
from app.models.audit.security_log import SecurityLog
from app.models.audit.system_log import SystemLog
from app.models.notification.notification import Notification
from app.models.notification.notification_log import NotificationLog
from app.repositories.notification.notification_mapping import (
    NotificationTemplateFactory,
    NotificationFactory,
    ApiRequestLogFactory,
    SecurityLogFactory,
    NotificationLogFactory
)


class NotificationRepository:

    @staticmethod
    def get_template_by_code(db_conn, template_code: str):
        query = text(NotificationTemplateFactory.get_select_sql())
        result = db_conn.execute(query, {
            "code": template_code,
            "status": "ACTIVE"
        }).fetchone()

        return NotificationTemplateFactory.map_row_to_tuple(result)

    @staticmethod
    def insert_notification(db_conn, user_id: str, template_code: str, title: str, body: str):
        query = text(NotificationFactory.get_insert_sql())
        params = NotificationFactory.build_params(user_id, template_code, title, body)
        db_conn.execute(query, params)

    @staticmethod
    def insert_api_request_log(db_conn, user_id: str, endpoint: str, method: str, request_payload: str,
                               response_payload: str, status_code: int, latency_ms: int, status: str):
        query = text(ApiRequestLogFactory.get_insert_sql())
        params = ApiRequestLogFactory.build_params(
            user_id, endpoint, method, request_payload, response_payload, status_code, latency_ms, status
        )
        db_conn.execute(query, params)

    @staticmethod
    def insert_security_log(db_conn, action: str, details: str, ip: str, ua: str):
        query = text(SecurityLogFactory.get_insert_sql())
        params = SecurityLogFactory.build_params(action, details, ip, ua)
        db_conn.execute(query, params)

    @staticmethod
    def insert_notification_log(db_conn, notification_id: str, user_id: str, channel: str, status: str,
                                provider_response: str):
        q_mail = text("SELECT email FROM users WHERE id = :u_id LIMIT 1")
        user_row = db_conn.execute(q_mail, {"u_id": user_id}).fetchone()
        recipient_target = user_row[0] if user_row else "unknown_verify_target@fintech.com"

        query = text(NotificationLogFactory.get_insert_sql())
        params = NotificationLogFactory.build_params(notification_id, user_id, channel, status, provider_response)
        params["recipient_target"] = recipient_target

        db_conn.execute(query, params)

    @staticmethod
    def insert_pure_security_log(db_conn, user_id: str | None, event_type: str, description: str, severity: str):
        """👑 Ghi log bảo mật/audit thông qua đối tượng ORM mapping chính quy (Hỗ trợ user_id là None)"""
        stmt = insert(SecurityLog).values(
            id=str(uuid.uuid4()),
            user_id=user_id,
            event_type=event_type,
            description=description,
            severity=severity,
            ip_address="127.0.0.1",
            created_at=datetime.now()
        )
        db_conn.execute(stmt)

    @staticmethod
    def insert_pure_system_log(db_conn, error_type: str, stack_trace: str, component: str):
        """👑 Ghi log lỗi hệ thống thông qua đối tượng ORM mapping chính quy"""
        stmt = insert(SystemLog).values(
            id=str(uuid.uuid4()),
            error_type=error_type,
            stack_trace=stack_trace,
            component=component[:255],
            status="UNRESOLVED",
            created_at=datetime.now(),
            updated_at=datetime.now()
        )
        db_conn.execute(stmt)

    # ==============================================================================
    # 👑 ĐÁNH DẤU BỔ SUNG: TOÀN BỘ CỔNG MAPPING ORM REPOSITORY RIÊNG CHO WORKER
    # ==============================================================================
    @staticmethod
    def update_notification_status(db_conn, notification_id: str, status: str):
        """👑 Mapping ORM: Cập nhật trạng thái bảng chính notifications"""
        stmt = (
            update(Notification)
            .where(Notification.id == notification_id)
            .values(status=status, updated_at=datetime.now())
        )
        db_conn.execute(stmt)

    @staticmethod
    def insert_worker_notification_log(db_conn, notification_id: str, recipient: str, gateway_response: str, status: str):
        """👑 Mapping ORM: Chèn bản ghi lịch sử gửi tin của Worker vào notification_logs"""
        stmt = insert(NotificationLog).values(
            id=str(uuid.uuid4()),
            notification_id=notification_id,
            channel="EMAIL",
            recipient_target=recipient,
            gateway_response=gateway_response,
            status=status,
            created_at=datetime.now(),
            updated_at=datetime.now()
        )
        db_conn.execute(stmt)
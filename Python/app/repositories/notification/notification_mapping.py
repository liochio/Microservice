import uuid
from datetime import datetime


class NotificationTemplateFactory:

    @staticmethod
    def get_select_sql():
        return """
               SELECT title_template_vi, content_template_vi, title_template_en, content_template_en
               FROM notification_templates
               WHERE template_code = :code \
                 AND status = :status
               LIMIT 1 \
               """

    @staticmethod
    def map_row_to_tuple(row):
        if not row:
            return None
        title_vi, content_vi, title_en, content_en = row[0], row[1], row[2], row[3]

        title = title_vi if title_vi else title_en
        body_template = content_vi if content_vi else content_en
        channels = "[]"

        return title, body_template, channels


class NotificationFactory:

    @staticmethod
    def get_insert_sql():
        return """
               INSERT INTO notifications (id, user_id, title, content, notification_type, is_read, status, created_at, updated_at)
               VALUES (:id, :user_id, :title, :content, :notification_type, 'UNREAD', :status, :created_at, :updated_at) \
               """

    @staticmethod
    def build_params(user_id: str, template_code: str, title: str, body: str):
        now = datetime.now()
        return {
            "id": str(uuid.uuid4()),
            "user_id": user_id,
            "title": title,
            "content": body,
            "notification_type": template_code,
            "status": "PENDING",
            "created_at": now,
            "updated_at": now
        }


class ApiRequestLogFactory:

    @staticmethod
    def get_insert_sql():
        return """
               INSERT INTO api_request_logs (id, user_id, endpoint, method, request_payload, response_payload, \
                                             status_code, latency_ms, status, created_at, updated_at)
               VALUES (:id, :user_id, :endpoint, :method, :request_payload, :response_payload, :status_code, \
                       :latency_ms, :status, :created_at, :updated_at) \
               """

    @staticmethod
    def build_params(user_id: str, endpoint: str, method: str, request_payload: str,
                     response_payload: str, status_code: int, latency_ms: int, status: str):
        now = datetime.now()
        safe_user_id = None if (not user_id or str(user_id).strip() in ["UNKNOWN", "None"]) else user_id
        return {
            "id": str(uuid.uuid4()),
            "user_id": safe_user_id,
            "endpoint": endpoint,
            "method": method,
            "request_payload": request_payload,
            "response_payload": response_payload,
            "status_code": status_code,
            "latency_ms": latency_ms,
            "status": status,
            "created_at": now,
            "updated_at": now
        }


class SecurityLogFactory:

    @staticmethod
    def get_insert_sql():
        return """
               INSERT INTO security_logs (id, user_id, event_type, description, severity, ip_address, created_at)
               VALUES (:id, :user_id, :action, :details, :severity, :ip_address, :created_at) \
               """

    @staticmethod
    def build_params(action: str, details: str, ip: str, severity: str = "INFO"):
        return {
            "id": str(uuid.uuid4()),
            "user_id": None,
            "action": action,
            "details": details,
            "severity": severity,
            "ip_address": ip,
            "created_at": datetime.now()
        }


class NotificationLogFactory:

    @staticmethod
    def get_insert_sql():
        return """
               INSERT INTO notification_logs (id, notification_id, channel, recipient_target, gateway_response, status, created_at, updated_at)
               VALUES (:id, :notification_id, :channel, :recipient_target, :provider_response, :status, :created_at, NOW()) \
               """

    @staticmethod
    def build_params(notification_id: str, user_id: str, channel: str, status: str, provider_response: str):
        return {
            "id": str(uuid.uuid4()),
            "notification_id": notification_id,
            "channel": channel,
            "recipient_target": f"PENDING_TARGET_{user_id}",
            "status": status,
            "provider_response": provider_response,
            "created_at": datetime.now()
        }
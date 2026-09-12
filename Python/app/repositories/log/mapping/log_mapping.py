import uuid
import json
from typing import Optional, Any
from sqlalchemy import insert
from app.models.audit.request_flow_log import RequestFlowLog
from app.models.audit.audit_log import AuditLog
from app.models.audit.security_log import SecurityLog


class LogMappingFactory:
    """
    👑 FACTORY ÁNH XẠ ORM MAPPING CHÍNH QUY
    """

    @staticmethod
    def get_insert_request_flow_log_stmt(trace_id: str, node: Optional[str], details: Optional[str]):
        return insert(RequestFlowLog).values(
            id=str(uuid.uuid4()),
            trace_id=trace_id,
            node=(node or "").upper(),
            details=(details or "")[:1000]
        )

    @staticmethod
    def get_insert_audit_log_stmt(db_conn: Any, user_id: Optional[str], action: str, details: str):
        # 👑 ĐÁNH DẤU CHỈNH SỬA: Gom sạch logic trích xuất Context từ Connection qua bên này
        ip_address = getattr(db_conn, "_ctx_ip_address", "UNKNOWN")
        user_agent = getattr(db_conn, "_ctx_user_agent", "UNKNOWN")

        return insert(AuditLog).values(
            id=str(uuid.uuid4()),
            user_id=user_id or "ANONYMOUS",
            action=action,
            table_name="users",
            old_data=None,
            new_data=json.dumps({"context": details})[:2000],
            ip_address=ip_address,
            user_agent=user_agent,
            status="SUCCESS"
        )

    @staticmethod
    def get_insert_security_log_stmt(db_conn: Any, user_id: Optional[str], event_type: str, severity: str, details: Optional[str], ip_address: Optional[str]):
        # 👑 ĐÁNH DẤU CHỈNH SỬA: Gom trọn vẹn fallback lấy IP ẩn của hệ thống
        if not ip_address:
            ip_address = getattr(db_conn, "_ctx_ip_address", "UNKNOWN")

        return insert(SecurityLog).values(
            id=str(uuid.uuid4()),
            user_id=user_id or "ANONYMOUS",
            event_type=event_type,
            severity=severity,
            description=(details or "")[:1000],
            ip_address=ip_address,
            status="TRIGGERED"
        )
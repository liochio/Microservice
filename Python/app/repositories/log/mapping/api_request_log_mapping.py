import uuid
from datetime import datetime
from typing import Any
from sqlalchemy import insert
from app.models.audit.api_request_log import ApiRequestLog

class ApiRequestLogMappingFactory:
    """
    👑 FACTORY ÁNH XẠ ORM MAPPING CHÍNH QUY
    """
    @staticmethod
    def get_insert_log_stmt(
        user_id: Any,
        endpoint: str,
        method: str,
        request_payload: Any,
        response_payload: Any,
        status_code: int,
        latency_ms: int,
        status: str
    ):
        current_now_vn = datetime.now()

        return insert(ApiRequestLog).values(
            id=str(uuid.uuid4()),
            user_id=user_id or "ANONYMOUS",
            endpoint=endpoint,
            method=method.upper(),
            request_payload=request_payload,
            response_payload=response_payload,
            status_code=status_code,
            latency_ms=latency_ms,
            status=status.upper() if status else "SUCCESS",
            created_at=current_now_vn,
            updated_at=current_now_vn
        )
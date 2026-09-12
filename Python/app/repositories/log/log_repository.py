from sqlalchemy.exc import SQLAlchemyError
from app.db.session import SessionLocal
from app.repositories.log.mapping.log_mapping import LogMappingFactory


class LogRepository:
    """
    👑 CENTRALIZED INDUSTRIAL LOG REPOSITORY
    🎯 ORM VERSION - KEEP ORIGINAL LOGIC
    """

    # ======================================================
    # 📊 REQUEST FLOW LOG
    # ======================================================
    @staticmethod
    def insert_request_flow_log(
        trace_id: str,
        node: str,
        details: str
    ) -> None:

        db = SessionLocal()
        try:
            stmt = LogMappingFactory.get_insert_request_flow_log_stmt(
                trace_id=trace_id,
                node=node,
                details=details
            )
            db.execute(stmt)
            db.commit()
        except SQLAlchemyError:
            db.rollback()
        finally:
            db.close()

    # ======================================================
    # 👑 API REQUEST LOG (DELEGATE)
    # ======================================================
    @staticmethod
    def insert_api_request_log(
        db_conn,
        user_id: str,
        endpoint: str,
        method: str,
        request_payload: str,
        response_payload: str,
        status_code: int,
        latency_ms: int,
        status: str
    ) -> None:

        try:
            from app.repositories.log.api_request_log_repository import (
                ApiRequestLogRepository
            )
            ApiRequestLogRepository.insert_api_request_log(
                db_conn=db_conn,
                user_id=user_id,
                endpoint=endpoint,
                method=method,
                request_payload=request_payload,
                response_payload=response_payload,
                status_code=status_code,
                latency_ms=latency_ms,
                status=status
            )
        except (ImportError, AttributeError):
            pass

    # ======================================================
    # 📝 AUDIT LOG
    # ======================================================
    @staticmethod
    def insert_audit_log(
        db_conn,
        user_id: str,
        action: str,
        details: str
    ) -> None:

        try:
            # 👑 ĐÁNH DẤU CHỈNH SỬA: Sạch bành bạnh logic thô, chỉ gọi Factory bốc lệnh và thực thi
            stmt = LogMappingFactory.get_insert_audit_log_stmt(
                db_conn=db_conn,
                user_id=user_id,
                action=action,
                details=details
            )
            db_conn.execute(stmt)
            db_conn.commit()
        except SQLAlchemyError:
            db_conn.rollback()

    # ======================================================
    # 💥 SYSTEM LOG
    # ======================================================
    @staticmethod
    def insert_system_log(
        db_conn,
        error_type: str,
        stack_trace: str,
        component: str
    ) -> None:

        try:
            from app.repositories.log.system_log_repository import (
                SystemLogRepository
            )
            SystemLogRepository.insert_system_log(
                db_conn=db_conn,
                error_type=error_type,
                stack_trace=stack_trace,
                component=component
            )
        except (ImportError, AttributeError):
            pass

    # ======================================================
    # 🛡️ SECURITY LOG
    # ======================================================
    @staticmethod
    def insert_security_log(
        db_conn,
        user_id: str,
        event_type: str,
        severity: str,
        details: str,
        ip_address: str = None
    ) -> None:

        try:
            # 👑 ĐÁNH DẤU CHỈNH SỬA: Đẩy trọn gói tham số qua Factory xử lý, Repo siêu tinh khiết
            stmt = LogMappingFactory.get_insert_security_log_stmt(
                db_conn=db_conn,
                user_id=user_id,
                event_type=event_type,
                severity=severity,
                details=details,
                ip_address=ip_address
            )
            db_conn.execute(stmt)
            db_conn.commit()
        except SQLAlchemyError:
            db_conn.rollback()
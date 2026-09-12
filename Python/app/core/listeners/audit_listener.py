import time
import uuid
from sqlalchemy import event
from sqlalchemy.engine import Engine
from app.core.logging.logger import DBLogger

@event.listens_for(Engine, "before_cursor_execute")
def before_cursor_execute(conn, cursor, statement, parameters, context, execmany):
    """🧭 Điểm đầu châm ngòi đồng hồ bấm giờ SQL"""
    context._query_start_time = time.perf_counter()
    trace_id = getattr(conn, "_ctx_trace_id", "UNKNOWN")

    # 👑 CHỈNH SỬA: Đẩy SQL thô trực tiếp qua Driver, không kích hoạt lặp chéo Event Listener
    if statement.strip().upper() in ["BEGIN", "START TRANSACTION"]:
        try:
            conn.exec_driver_sql(
                "INSERT INTO request_flow_logs (id, trace_id, node, details, created_at) VALUES (%s, %s, %s, %s, NOW())",
                (str(uuid.uuid4()), trace_id, "DB_TRANSACTION_BEGIN", statement)
            )
        except:
            pass


@event.listens_for(Engine, "after_cursor_execute")
def after_cursor_execute(conn, cursor, statement, parameters, context, execmany):
    """🧭 Điểm cuối chốt hạ, phân tách Slow Query > 300ms (Mục 11, 19)"""
    total_time_ms = (time.perf_counter() - context._query_start_time) * 1000
    trace_id = getattr(conn, "_ctx_trace_id", "UNKNOWN")

    # 👑 CHỈNH SỬA: Đẩy SQL thô trực tiếp qua Driver khi Commit thành công, bảo vệ mạch sống của audit_logs chính
    if statement.strip().upper() == "COMMIT":
        try:
            conn.exec_driver_sql(
                "INSERT INTO request_flow_logs (id, trace_id, node, details, created_at) VALUES (%s, %s, %s, %s, NOW())",
                (str(uuid.uuid4()), trace_id, "DB_TRANSACTION_COMMIT", "Transaction committed successfully.")
            )
        except:
            pass

    # Tự động cộng dồn thời gian xử lý DB vào luồng Request Context phục vụ bóc tách KPI Latency
    try:
        from fastapi import Request
        import inspect
        for frame_info in inspect.stack():
            locals_dict = frame_info.frame.f_locals
            if "request" in locals_dict and isinstance(locals_dict["request"], Request):
                req = locals_dict["request"]
                current_db_time = getattr(req.state, "db_time_ms", 0.0)
                req.state.db_time_ms = current_db_time + total_time_ms
                break
    except:
        pass

    # 🚨 CHẶN BẮT SLOW QUERY CHÍ MẠNG (Mục 11)
    if total_time_ms > 300.0:
        details_slow = {
            "sql": statement[:1000],
            "parameters": str(parameters)[:500],
            "latency_ms": int(total_time_ms)
        }
        DBLogger.emit_json_log(
            trace_id=trace_id, event="SLOW_QUERY_ALERT", user_id="DATABASE_ENGINE",
            status="WARNING", latency_ms=int(total_time_ms), module="DATABASE",
            severity="WARNING", details=details_slow
        )


def register_audit_listeners(session_factory):
    """🎯 Kích hoạt cổng bẫy sự kiện SQLAlchemy mức độ toàn cục"""
    # Hàm này giữ nguyên signature để gọi từ file main.py cũ của sếp
    pass
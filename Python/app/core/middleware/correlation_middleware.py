# D:\UIT - HK2\FinanceProject\app\core\middleware\correlation_middleware.py

import time
import uuid
import traceback
from fastapi import Request, Response
from starlette.middleware.base import BaseHTTPMiddleware
from starlette.concurrency import run_in_threadpool
from sqlalchemy import text
from app.db.session import SessionLocal


class CorrelationMiddleware(BaseHTTPMiddleware):
    """
    👑 CENTRALIZED CORRELATION MIDDLEWARE (OBSERVABILITY LAYER)
    🎯 Bốc Trace-ID từ Nginx Gateway, tiêm vào luồng chạy và quản lý vòng đời Request.
    """

    async def dispatch(self, request: Request, call_next) -> Response:
        start_time = time.time()

        trace_id = request.headers.get("X-Trace-ID")

        if not trace_id or str(trace_id).strip() == "":
            trace_id = str(uuid.uuid4())

        request.state.trace_id = trace_id

        try:
            response = await call_next(request)

            latency_ms = int((time.time() - start_time) * 1000)

            response.headers["X-Trace-ID"] = str(trace_id)
            response.headers["X-Response-Time-Ms"] = str(latency_ms)

            def _insert_api_log():
                db_conn = SessionLocal()
                try:
                    query_insert_log = text("""
                                            INSERT INTO api_request_logs (id, user_id, endpoint, method,
                                                                          request_payload,
                                                                          response_payload,
                                                                          status_code, latency_ms, status, created_at)
                                            VALUES (:id, :user_id, :endpoint, :method, :req_payload, :res_payload,
                                                    :status_code, :latency_ms, 'SUCCESS', NOW())
                                            """)
                    db_conn.execute(query_insert_log, {
                        "id": str(uuid.uuid4()),
                        "user_id": getattr(request.state, "user_id", "ANONYMOUS"),
                        "endpoint": str(request.url.path),
                        "method": str(request.method),
                        "req_payload": f'{{"trace_id": "{trace_id}"}}',
                        "res_payload": f'{{"status_code": {response.status_code}, "latency_ms": {latency_ms}}}',
                        "status_code": int(response.status_code),
                        "latency_ms": int(latency_ms)
                    })
                    query_flow = text("""
                        INSERT INTO request_flow_logs (id, trace_id, node, details, created_at, updated_at)
                        VALUES (:id, :trace_id, :node, :details, NOW(), NOW())
                    """)
                    db_conn.execute(query_flow, {
                        "id": str(uuid.uuid4()),
                        "trace_id": str(trace_id),
                        "node": f"PYTHON_{str(request.method).upper()}_{str(request.url.path).replace('/', '_').strip('_')}",
                        "details": f"Status: {response.status_code} | Latency: {latency_ms}ms"
                    })

                    db_conn.commit()
                except Exception as sql_log_err:
                    db_conn.rollback()
                    print(f"[SQL_LOG_ERROR] Khong the insert truc tiep api_request_logs / request_flow_logs: {str(sql_log_err)}")
                finally:
                    db_conn.close()

            await run_in_threadpool(_insert_api_log)

            return response

        except Exception as e:
            def _insert_sys_log():
                db_conn = SessionLocal()
                try:
                    # Đã cập nhật lại các cột theo đúng schema thực tế của bảng system_logs
                    query_insert_sys_log = text("""
                                                INSERT INTO system_logs (id, component, error_type, stack_trace, created_at)
                                                VALUES (:id, 'CORRELATION_MIDDLEWARE', :error_type, :stack_trace, NOW())
                                                """)
                    db_conn.execute(query_insert_sys_log, {
                        "id": str(uuid.uuid4()),
                        "error_type": type(e).__name__,
                        "stack_trace": f"Endpoint: {request.url.path} | Error: {str(e)} | Trace: {traceback.format_exc()[:2000]}"
                    })
                    db_conn.commit()
                except Exception as sys_log_err:
                    print(f"[SYS_LOG_ERROR] Fallback ghi log system_logs that bai: {str(sys_log_err)}")
                finally:
                    db_conn.close()

            await run_in_threadpool(_insert_sys_log)
            raise e
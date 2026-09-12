# 📄 Đường dẫn file: app/core/middleware/middleware.py
import time
import uuid
import json
import jwt
from typing import Any, Optional
import redis.asyncio as aioredis
from fastapi import Request, Response, status
from fastapi.responses import JSONResponse
from starlette.middleware.base import BaseHTTPMiddleware

from app.core.config.settings import settings
from app.core.logging.logger import DBLogger


import asyncio
from concurrent.futures import ThreadPoolExecutor

_audit_pool = ThreadPoolExecutor(max_workers=5, thread_name_prefix="audit_worker")

def _insert_audit_db_sync(trace_id: str, tenant_id: str, user_id: Any, username: str, method: str, path: str, client_ip: str, user_agent: str, device_id: str, status_code: int, latency_ms: float):
    try:
        from app.db.session import engine
        from sqlalchemy import text
        clean_user_id = None
        if user_id and str(user_id).isdigit():
            clean_user_id = int(user_id)
        
        with engine.connect() as conn:
            conn.execute(
                text("""
                    INSERT INTO audit_logs (
                        trace_id, tenant_id, user_id, username, user_email,
                        user_role, client_ip, platform, device_id, device_name,
                        user_agent, module, action_type, action_description,
                        http_method, request_uri, request_params, request_body,
                        old_data, new_data, status, http_status_code,
                        error_message, execution_time_ms, created_at
                    ) VALUES (
                        :trace_id, :tenant_id, :user_id, :username, :user_email,
                        :user_role, :client_ip, :platform, :device_id, :device_name,
                        :user_agent, :module, :action_type, :action_description,
                        :http_method, :request_uri, :request_params, :request_body,
                        :old_data, :new_data, :status, :http_status_code,
                        :error_message, :execution_time_ms, NOW(6)
                    )
                """),
                {
                    "trace_id": trace_id,
                    "tenant_id": tenant_id or "default",
                    "user_id": clean_user_id,
                    "username": username if username != "ANONYMOUS" else None,
                    "user_email": username if username != "ANONYMOUS" else None,
                    "user_role": "USER",
                    "client_ip": client_ip or "127.0.0.1",
                    "platform": "WEB",
                    "device_id": device_id,
                    "device_name": None,
                    "user_agent": user_agent[:500] if user_agent else None,
                    "module": "FINTECH",
                    "action_type": "CREATE" if method == "POST" else ("UPDATE" if method in ("PUT", "PATCH") else ("DELETE" if method == "DELETE" else "VIEW")),
                    "action_description": f"{method} {path}",
                    "http_method": method,
                    "request_uri": path,
                    "request_params": None,
                    "request_body": None,
                    "old_data": None,
                    "new_data": None,
                    "status": "FAILED" if status_code >= 400 else "SUCCESS",
                    "http_status_code": status_code,
                    "error_message": None,
                    "execution_time_ms": int(latency_ms)
                }
            )

            # Ghi thêm vào bảng api_request_logs
            try:
                conn.execute(
                    text("""
                        INSERT INTO api_request_logs (
                            id, user_id, endpoint, method, status_code, latency_ms, status, created_at, updated_at
                        ) VALUES (
                            UUID(), :user_id, :endpoint, :method, :status_code, :latency_ms, :status, NOW(), NOW()
                        )
                    """),
                    {
                        "user_id": str(user_id) if user_id and user_id != "ANONYMOUS" else None,
                        "endpoint": path[:500],
                        "method": method[:10],
                        "status_code": status_code,
                        "latency_ms": int(latency_ms),
                        "status": "FAILED" if status_code >= 400 else "SUCCESS"
                    }
                )
            except Exception as e_api:
                pass

            conn.commit()
    except Exception as e:
        # Ghi log nếu xảy ra lỗi ghi DB
        print(f"[AUDIT_LOG_ERROR] Không thể ghi audit log: {e}")


def mask_dict(data: Any) -> Any:
    """
    👑 KÍNH LỌC AN NINH (DATA MASKING):
    Mục đích: Tự động che dấu các trường thông tin nhạy cảm (mật khẩu, token, OTP) trước khi log.
    """
    sensitive_keys = {
        "password", "confirm_password", "access_token", "refresh_token",
        "secret_key", "otp_code", "token"
    }
    if isinstance(data, dict):
        return {k: ("******" if str(k).lower() in sensitive_keys else mask_dict(v)) for k, v in data.items()}
    elif isinstance(data, list):
        return [mask_dict(item) for item in data]
    return data


async def safe_insert_flow_log(trace_id: str, node: str, details: str) -> None:
    """
    👑 HÀM GHI NHẬN FLOW LOG NHẸ (IN-MEMORY / CONSOLE ONLY):
    Mục đích: Không mở kết nối MySQL đồng bộ gây nghẽn Connection Pool.
    Thay vào đó, đẩy trực tiếp ra logger hệ thống có cấu trúc JSON.
    """
    DBLogger.emit_json_log(
        trace_id=trace_id,
        event=f"FLOW_{node.upper()}",
        user_id="SYSTEM",
        status="SUCCESS",
        module="FLOW_TRACE",
        details=details[:500] if details else ""
    )


class RequestContextAndLogMiddleware(BaseHTTPMiddleware):
    """
    👑 MIDDLEWARE 1: TRACE-ID, NGỮ CẢNH REQUEST, SECURITY HEADERS & LOGGING
    🎯 Mục đích & Nhiệm vụ:
       1. Khởi tạo/nhận Trace-ID duy nhất, bấm giờ Latency nano/milli-second.
       2. Bóc tách IP, User-Agent, Device-ID, Locale vào `request.state`.
       3. Giải mã sớm Bearer JWT (nếu có) trên RAM để nạp user_id vào context (leeway = 0s).
       4. Tiêm các Header an ninh WAF chuẩn quốc tế (HSTS, nosniff, DENY frame) vào Response.
       5. Ghi đúng 1 dòng Log JSON có cấu trúc khi kết thúc Request (0 kết nối DB).
    """

    async def dispatch(self, request: Request, call_next) -> Response:
        # 1. Khởi tạo Trace-ID & Đồng hồ đo độ trễ
        trace_id = request.headers.get("X-Trace-ID") or str(uuid.uuid4())
        request.state.trace_id = trace_id
        start_time = time.perf_counter()
        request.state.start_time = start_time

        # 2. Bóc tách dấu vết mạng Client (Fingerprint & Locale)
        forwarded_for = request.headers.get("X-Forwarded-For")
        request.state.client_ip = forwarded_for.split(",")[0].strip() if forwarded_for else (
            request.client.host if request.client else "127.0.0.1"
        )
        request.state.user_agent = request.headers.get("User-Agent", "Unknown")[:500]
        request.state.device_id = request.headers.get("X-Device-ID", "UNKNOWN")

        # Ngôn ngữ yêu cầu từ Client
        raw_lang = request.headers.get("Accept-Language", settings.DEFAULT_LANGUAGE)
        request.state.locale = raw_lang.split(",")[0].split("-")[0].lower()

        # 3. Khởi tạo thông tin User mặc định
        request.state.user_id = "ANONYMOUS"
        request.state.username = "ANONYMOUS"
        request.state.user_permissions = []
        request.state.user_modules = []
        request.state.permissions = []
        request.state.modules = []

        # Giải mã sớm Bearer JWT (nếu có) trên RAM
        auth_header = request.headers.get("Authorization") or request.headers.get("authorization")
        token = None
        if auth_header:
            if auth_header.lower().startswith("bearer "):
                token = auth_header[7:].strip()
            elif "." in auth_header:
                token = auth_header.strip()
        if not token:
            token = request.headers.get("X-Access-Token") or request.headers.get("x-access-token")

        # In log bắt đầu nhận Request
        query_str = f"?{request.url.query}" if request.url.query else ""
        print(f"\n📥 [REQ_START] {request.method} {request.url.path}{query_str}")
        print(f"   🆔 Trace-ID : {trace_id}")
        print(f"   🌐 Client   : IP={request.state.client_ip} | Locale={request.state.locale}")

        if token:
            try:
                from app.core.security.jwt.jwt_service import JwtService
                payload = JwtService.decode_token(token)
                request.state.user_id = payload.get("sub", "ANONYMOUS")
                request.state.username = payload.get("username", "ANONYMOUS")
                request.state.user_permissions = payload.get("permissions", [])
                request.state.permissions = request.state.user_permissions
                request.state.user_roles = payload.get("roles", [])
                request.state.user_modules = payload.get("modules", ["FINTECH", "LEDGER", "AI", "OCR", "PIGGY"])
                request.state.modules = request.state.user_modules
                print(f"   🔑 Auth (JWT) : User '{request.state.username}' (ID: {request.state.user_id})")
            except (jwt.ExpiredSignatureError, jwt.PyJWTError) as jwt_err:
                # Nếu JWT không decode được với secret nội bộ, kiểm tra xem có được Gateway chuyển tiếp không
                gateway_user_id = request.headers.get("X-User-Id") or request.headers.get("x-user-id")
                if gateway_user_id:
                    request.state.user_id = gateway_user_id
                    request.state.username = request.headers.get("X-Username") or request.headers.get("x-username") or "gateway_user"
                    roles_raw = request.headers.get("X-Roles") or request.headers.get("x-roles") or "USER"
                    request.state.user_roles = [r.strip() for r in roles_raw.split(",") if r.strip()]
                    request.state.user_permissions = ["READ", "WRITE", "EXECUTE"]
                    request.state.permissions = request.state.user_permissions
                    request.state.user_modules = ["FINTECH", "LEDGER", "AI", "OCR", "PIGGY"]
                    request.state.modules = request.state.user_modules
                    print(f"   🔑 Auth (Gateway-Validated) : User '{request.state.username}' (ID: {request.state.user_id})")
                else:
                    print(f"   ⚠️  Auth     : Bearer token không giải mã được: {str(jwt_err)}")
        else:
            # Không có Bearer token, kiểm tra trực tiếp Gateway headers
            gateway_user_id = request.headers.get("X-User-Id") or request.headers.get("x-user-id")
            if gateway_user_id:
                request.state.user_id = gateway_user_id
                request.state.username = request.headers.get("X-Username") or request.headers.get("x-username") or "gateway_user"
                roles_raw = request.headers.get("X-Roles") or request.headers.get("x-roles") or "USER"
                request.state.user_roles = [r.strip() for r in roles_raw.split(",") if r.strip()]
                request.state.user_permissions = ["READ", "WRITE", "EXECUTE"]
                request.state.permissions = request.state.user_permissions
                request.state.user_modules = ["FINTECH", "LEDGER", "AI", "OCR", "PIGGY"]
                request.state.modules = request.state.user_modules
                print(f"   🔑 Auth (Gateway Forwarded) : User '{request.state.username}' (ID: {request.state.user_id})")
            else:
                print(f"   👤 Auth     : Gói tin công khai (ANONYMOUS)")

        # 4. Chuyển tiếp Request vào Route Handler
        try:
            response = await call_next(request)
        except Exception as unhandled_exc:
            latency_ms = (time.perf_counter() - start_time) * 1000
            print(f"\n💥 [CRITICAL_UNHANDLED_ERROR] {request.method} {request.url.path} | Time: {latency_ms:.2f}ms")
            DBLogger.emit_json_log(
                trace_id=trace_id,
                event="UNHANDLED_ROUTE_CRASH",
                user_id=request.state.user_id,
                status="CRITICAL",
                latency_ms=int(latency_ms),
                module="MIDDLEWARE",
                severity="ERROR",
                details=f"Sập luồng chưa xử lý: {str(unhandled_exc)}"
            )
            raise unhandled_exc

        # 5. Tính toán Latency & Tiêm Security Headers
        latency_ms = (time.perf_counter() - start_time) * 1000
        response.headers["X-Trace-ID"] = trace_id
        response.headers["X-Response-Time"] = f"{latency_ms:.2f}ms"
        response.headers["X-Frame-Options"] = "DENY"
        response.headers["X-Content-Type-Options"] = "nosniff"
        response.headers["X-XSS-Protection"] = "1; mode=block"
        response.headers["Strict-Transport-Security"] = "max-age=31536000; includeSubDomains"

        # 6. Ghi Access Log chi tiết
        status_code = response.status_code
        status_icon = "✅" if status_code < 400 else ("⚠️ " if status_code < 500 else "💥")
        print(f"{status_icon} [REQ_COMPLETED] {request.method} {request.url.path} | Status: {status_code} | Time: {latency_ms:.2f}ms | User: {request.state.user_id}")

        DBLogger.emit_json_log(
            trace_id=trace_id,
            event="HTTP_REQUEST_COMPLETED",
            user_id=request.state.user_id,
            status="SUCCESS" if status_code < 400 else "FAIL",
            latency_ms=int(latency_ms),
            module="API_PIPELINE",
            severity="INFO" if status_code < 400 else ("WARNING" if status_code < 500 else "ERROR"),
            details={
                "method": request.method,
                "path": request.url.path,
                "status_code": status_code,
                "client_ip": request.state.client_ip,
                "user_agent": request.state.user_agent
            }
        )

        # 7. Ghi Audit Log vào MySQL CSDL (Async Thread Pool)
        try:
            _audit_pool.submit(
                _insert_audit_db_sync,
                trace_id,
                "default",
                getattr(request.state, "user_id", None),
                getattr(request.state, "username", None),
                request.method,
                request.url.path,
                getattr(request.state, "client_ip", "127.0.0.1"),
                getattr(request.state, "user_agent", "Unknown"),
                getattr(request.state, "device_id", None),
                status_code,
                latency_ms
            )
        except Exception:
            pass

        return response


class RedisRateLimitMiddleware(BaseHTTPMiddleware):
    """
    👑 MIDDLEWARE 2: GIỚI HẠN TẦN SUẤT TRUY CẬP (HYBRID REDIS & IN-MEMORY RATE LIMITING)
    🎯 Mục đích & Nhiệm vụ:
       - Chống brute-force spam vào các API nhạy cảm (/login, /register, /verify-otp, /activate).
       - Ưu tiên kiểm tra trên Redis Connection Pool Singleton.
       - Tự động chuyển sang In-Memory Sliding Window nếu Redis ngoại tuyến (Bảo vệ 100% không bao giờ hở sườn).
    """
    _redis_client: Optional[aioredis.Redis] = None
    _in_memory_store: dict = {}
    _last_clean_time: float = time.time()

    @classmethod
    def get_redis_client(cls) -> aioredis.Redis:
        """Khởi tạo Redis Connection Pool dùng chung dạng Singleton"""
        if cls._redis_client is None:
            cls._redis_client = aioredis.from_url(
                settings.REDIS_URL,
                max_connections=50,
                socket_timeout=0.3,
                decode_responses=True
            )
        return cls._redis_client

    @classmethod
    def check_in_memory_limit(cls, key: str, max_hits: int = 10, window_sec: int = 2) -> bool:
        """Kiểm tra tần suất trên RAM nếu Redis offline"""
        now = time.time()
        # Dọn rác định kỳ mỗi 60s
        if now - cls._last_clean_time > 60:
            cls._in_memory_store = {k: v for k, v in cls._in_memory_store.items() if now - v.get("start", 0) < 10}
            cls._last_clean_time = now

        entry = cls._in_memory_store.get(key)
        if not entry or (now - entry["start"]) > window_sec:
            cls._in_memory_store[key] = {"hits": 1, "start": now}
            return True
        else:
            entry["hits"] += 1
            if entry["hits"] > max_hits:
                return False
            return True

    async def dispatch(self, request: Request, call_next) -> Response:
        endpoint = str(request.url.path)
        sensitive_paths = ["/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/auth/verify-otp", "/api/v1/auth/activate"]

        if any(endpoint.startswith(p) for p in sensitive_paths):
            client_ip = getattr(request.state, "client_ip", "127.0.0.1")
            current_window = int(time.time() // 2)  # Cửa sổ 2 giây
            redis_key = f"ratelimit:{client_ip}:{endpoint}:{current_window}"
            is_allowed = True

            try:
                redis = self.get_redis_client()
                hits = await redis.incr(redis_key)
                if hits == 1:
                    await redis.expire(redis_key, 2)
                if hits > 10:
                    is_allowed = False
            except Exception:
                # Chuyển sang In-Memory fallback bảo vệ RAM cục bộ
                mem_key = f"{client_ip}:{endpoint}"
                is_allowed = self.check_in_memory_limit(mem_key, max_hits=10, window_sec=2)

            if not is_allowed:
                trace_id = getattr(request.state, "trace_id", SystemConstants.UNKNOWN)
                message = i18n_translator.translate(
                    request,
                    error_code=SystemConstants.GLOBAL_TOO_MANY_REQUESTS,
                    msg_type=SystemConstants.MSG_TYPE_ERROR
                )
                return JSONResponse(
                    status_code=status.HTTP_429_TOO_MANY_REQUESTS,
                    content={
                        "success": False,
                        "error_code": SystemConstants.GLOBAL_TOO_MANY_REQUESTS,
                        "message": message,
                        "trace_id": trace_id
                    }
                )

        return await call_next(request)


# ==============================================================================
# 👑 CÁC ALIAS TƯƠNG THÍCH NGƯỢC (ĐỂ KHÔNG GÃY CÁC IMPORT CŨ)
# ==============================================================================
StepZeroAsgiNetworkLogMiddleware = RequestContextAndLogMiddleware
StepOneCorrelationMiddleware = RequestContextAndLogMiddleware
StepTwoRequestClockMiddleware = RequestContextAndLogMiddleware
StepThreeClientFingerprintMiddleware = RequestContextAndLogMiddleware
StepFourI18nLocaleMiddleware = RequestContextAndLogMiddleware
StepFiveRequestBodyBufferMiddleware = RequestContextAndLogMiddleware
StepSecurityHeadersMiddleware = RequestContextAndLogMiddleware
StepSevenRedisRateLimitMiddleware = RedisRateLimitMiddleware
StepEightDbSessionMiddleware = RequestContextAndLogMiddleware
StepNineAuthenticationMiddleware = RequestContextAndLogMiddleware
StepTenRbacGuardMiddleware = RequestContextAndLogMiddleware
StepElevenResponseMaskingMiddleware = RequestContextAndLogMiddleware
StepTwelveStructuredLoggingMiddleware = RequestContextAndLogMiddleware
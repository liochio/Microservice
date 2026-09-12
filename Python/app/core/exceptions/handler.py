import json
import traceback
from fastapi import Request, FastAPI, status
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError

from app.constants import SystemConstants
from app.core.exceptions.base_exception import FintechBaseException
from app.core.translator.translator_engine import i18n_translator
from app.core.logging.logger import DBLogger
from app.repositories.log.log_repository import LogRepository
from app.db.session import SessionLocal


def register_exception_handlers(app: FastAPI):
    """👑 BỘ GÀI BẪY SỰ CỐ TRUNG TÂM - ĐA NGÔN NGỮ ĐỘNG ĂN THEO DICTIONARY"""

    @app.exception_handler(RequestValidationError)
    async def custom_validation_exception_handler(request: Request, exc: RequestValidationError):
        trace_id = getattr(request.state, "trace_id", SystemConstants.UNKNOWN)
        errors = exc.errors()
        clean_errors = []
        for err in errors:
            field_path = ".".join(str(loc) for loc in err["loc"] if loc != "body")
            clean_errors.append({
                "field": field_path,
                "error_code": f"MISSING_{field_path.upper()}" if err["type"] == "missing" else f"INVALID_{field_path.upper()}",
                "message": err["msg"]
            })

        error_message = i18n_translator.translate(
            request,
            error_code=SystemConstants.VALIDATION_ERROR_400,
            msg_type=SystemConstants.MSG_TYPE_ERROR
        )

        # 👑 IN LOG CHI TIẾT TỪNG TRƯỜNG DỮ LIỆU BỊ LỖI
        print(f"\n⚠️  [VALIDATION_ERROR_400] {request.method} {request.url.path} | TraceID: {trace_id}")
        print(f"   👤 User / Client IP : {getattr(request.state, 'user_id', 'ANONYMOUS')} / {getattr(request.state, 'client_ip', '127.0.0.1')}")
        print(f"   📋 Chi tiết các trường bị lỗi ({len(clean_errors)} lỗi):")
        for idx, err_item in enumerate(clean_errors, 1):
            print(f"      {idx}. Trường '{err_item['field']}': Mã lỗi={err_item['error_code']} | Chi tiết: {err_item['message']}")
        print()

        return JSONResponse(
            status_code=status.HTTP_400_BAD_REQUEST,
            content={
                "success": False,
                "error_code": SystemConstants.VALIDATION_ERROR_400,
                "message": error_message,
                "trace_id": trace_id,
                "data": {"details": clean_errors}
            }
        )

    @app.exception_handler(FintechBaseException)
    async def fintech_exception_handler(request: Request, exc: FintechBaseException):
        trace_id = getattr(request.state, "trace_id", SystemConstants.UNKNOWN)
        error_message = i18n_translator.translate(
            request,
            error_code=exc.error_code,
            msg_type=SystemConstants.MSG_TYPE_ERROR
        )
        location = getattr(exc, "location", "Unknown origin")

        # 👑 IN LOG FORENSIC ĐẬP THẲNG CONSOLE VỚI ĐẦY ĐỦ VỊ TRÍ FILE & DÒNG CODE
        status_icon = "⚠️ " if exc.status_code < 500 else "🚨"
        print(f"\n{status_icon} [BUSINESS_EXCEPTION_{exc.status_code}] {request.method} {request.url.path} | TraceID: {trace_id}")
        print(f"   📌 Mã lỗi (Code)      : {exc.error_code}")
        print(f"   💬 Thông điệp i18n    : {error_message}")
        print(f"   📍 Vị trí phát sinh   : {location}")
        print(f"   👤 User-ID / Client IP: {getattr(request.state, 'user_id', 'ANONYMOUS')} / {getattr(request.state, 'client_ip', '127.0.0.1')}")
        if exc.context:
            print(f"   📦 Context dữ liệu    : {json.dumps(exc.context, ensure_ascii=False)}")
        print()

        DBLogger.emit_json_log(
            trace_id=trace_id,
            event=f"BUSINESS_ERROR_{exc.status_code}",
            user_id=getattr(request.state, "user_id", "ANONYMOUS"),
            status="FAIL",
            module="EXCEPTION_HANDLER",
            severity="WARNING" if exc.status_code < 500 else "ERROR",
            details={
                "error_code": exc.error_code,
                "message": error_message,
                "location": location,
                "context": exc.context
            }
        )

        return JSONResponse(
            status_code=exc.status_code,
            content={
                "success": False,
                "error_code": exc.error_code,
                "message": error_message,
                "trace_id": trace_id,
                "context": exc.context
            }
        )

    @app.exception_handler(Exception)
    async def universal_exception_handler(request: Request, exc: Exception):
        """💥 CHỐT CHẶN PHÒNG VỆ CUỐI CÙNG: Hốt trọn lỗi Crash 500 và xả Stacktrace xuống Hệ thống"""
        trace_id = getattr(request.state, "trace_id", SystemConstants.UNKNOWN)
        full_stack = traceback.format_exc()

        # 👑 IN TOÀN BỘ STACKTRACE VÀ FILE GÂY SẬP CONSOLE
        print(f"\n💥 [FATAL_SERVER_CRASH_500] {request.method} {request.url.path} | TraceID: {trace_id}")
        print(f"   💥 Loại sự cố : {type(exc).__name__}: {str(exc)}")
        print(f"   👤 User / IP  : {getattr(request.state, 'user_id', 'ANONYMOUS')} / {getattr(request.state, 'client_ip', '127.0.0.1')}")
        print(f"   🔥 Chi tiết Stacktrace:")
        for line in full_stack.strip().splitlines():
            print(f"      {line}")
        print()

        # Ghi nhận vết xích sập nguồn ra file cứng app.log
        DBLogger.emit_json_log(
            trace_id=trace_id, event="FATAL_SERVER_CRASH", user_id="SYSTEM",
            status="CRITICAL", module="APPLICATION", severity="ERROR",
            details="Hệ thống sụp luồng xử lý mã nguồn vật lý", exception=full_stack
        )

        # Cứu hộ ghi nhận biến cố xuống bảng system_logs vật lý
        try:
            log_db = SessionLocal()
            setattr(log_db, "_ctx_trace_id", trace_id)
            LogRepository.insert_system_log(log_db, exc.__class__.__name__, full_stack, "CORE_GATEWAY_CRASH")
            LogRepository.insert_request_flow_log(trace_id=trace_id, node="REQUEST_CRASH_500", details=str(exc))
            log_db.commit()
            log_db.close()
        except Exception:
            pass

        error_message = i18n_translator.translate(
            request,
            error_code=SystemConstants.INTERNAL_SERVER_ERROR,
            msg_type=SystemConstants.MSG_TYPE_ERROR
        )

        return JSONResponse(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            content={
                "success": False,
                "error_code": SystemConstants.INTERNAL_SERVER_ERROR,
                "message": error_message,
                "trace_id": trace_id,
                "details": str(exc)
            }
        )
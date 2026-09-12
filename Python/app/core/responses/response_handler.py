from fastapi import Request
from fastapi.responses import JSONResponse
from app.core.translator.translator_engine import i18n_translator
from datetime import datetime, timezone  # 👑 KÍCH NẠP: Thêm timezone xử lý đồng bộ thời gian hệ thống
from typing import Any, Optional


class ResponseHandler:
    @staticmethod
    def _parse_http_status(error_code: str) -> int:
        try:
            parts = error_code.split("_")
            if len(parts) >= 2:
                return int(parts[1])
        except (ValueError, AttributeError, IndexError):
            pass
        return 400

    @classmethod
    def success(cls, _request: Request, data: Optional[Any] = None, message: str = "Success") -> JSONResponse:
        return JSONResponse(
            status_code=200,
            content={
                "success": True,
                "message": message,
                "error_code": None,
                "data": data if data is not None else {},
                # 👑 ĐÁNH DẤU CHỈNH SỬA: Ép chuỗi .isoformat() triệt tiêu vĩnh viễn Exception Not JSON Serializable
                "timestamp": datetime.now(timezone.utc).isoformat()
            }
        )

    @classmethod
    def error(cls, request: Request, error_code: str, _db: Optional[Any] = None,
              data: Optional[Any] = None) -> JSONResponse:
        translated_message = i18n_translator.translate(request, error_code=error_code)

        status_code = cls._parse_http_status(error_code)

        return JSONResponse(
            status_code=status_code,
            content={
                "success": False,
                "message": translated_message,
                "error_code": error_code,
                "data": data,
                # 👑 ĐÁNH DẤU CHỈNH SỬA: Ép chuỗi .isoformat() triệt tiêu vĩnh viễn Exception Not JSON Serializable
                "timestamp": datetime.now(timezone.utc).isoformat()
            }
        )
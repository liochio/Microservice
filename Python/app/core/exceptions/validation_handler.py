from fastapi import Request, FastAPI
from fastapi.exceptions import RequestValidationError
# 👑 Bảo lưu nguyên vẹn con hàng ResponseHandler tuyến đầu của sếp
from app.core.responses.response_handler import ResponseHandler
# 👑 ĐÁNH DẤU CHỈNH SỬA: Import bộ dịch đa ngôn ngữ động chính quy của hệ thống
from app.core.translator.translator_engine import i18n_translator

async def custom_validation_exception_handler(request: Request, exc: RequestValidationError):
    """
    🛡️ INTERCEPTOR: Chuyển đổi các lỗi dạng RequestValidationError (nếu có sót)
    về đúng chuẩn ma trận lỗi đầu vào của sếp.
    🎯 ĐÁNH DẤU SỬA ĐỔI: Ép ăn theo i18n_translator, tự động bốc ngôn ngữ từ Request thông qua Middleware
    """
    errors = exc.errors()
    clean_errors = []

    for err in errors:
        field_path = ".".join(str(loc) for loc in err["loc"] if loc != "body")
        clean_errors.append({
            "field": field_path,
            "error_code": f"MISSING_{field_path.upper()}" if err["type"] == "missing" else f"INVALID_{field_path.upper()}",
            "message": err["msg"]
        })

    # 👑 ĐÁNH DẤU CHỈNH SỬA: Ép tra từ điển i18n động dưới DB cho mã lỗi validation toàn cục
    # Thay thế mã găm cứng cũ bằng chuỗi định danh chuẩn quy hoạch
    from app.constants import SystemConstants
    error_code_key = SystemConstants.VALIDATION_ERROR_400
    translated_message = i18n_translator.translate(request, error_code=error_code_key, msg_type=SystemConstants.MSG_TYPE_ERROR)

    # Gọi ResponseHandler bọc thép, truyền đầy đủ message đã dịch và db=None để khớp signature
    return ResponseHandler.error(
        request=request,
        error_code=error_code_key,
        db=None,  # Gài None an toàn để không làm gãy đối số mặc định của ResponseHandler
        data={"details": clean_errors}
    )


def register_exception_handlers(app: FastAPI):
    """👑 BỘ ĐĂNG KÝ BẪY LỖI TOÀN CỤC CHO CẢ HỆ THỐNG APPS"""
    app.add_exception_handler(RequestValidationError, custom_validation_exception_handler)
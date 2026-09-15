
import inspect
import os
from fastapi import HTTPException, status

class FintechBaseException(HTTPException):
    """👑 Thực thể lỗi gốc bảo mật cao cho toàn bộ hệ thống Monolith"""
    def __init__(
        self,
        error_code: str,
        status_code: int = status.HTTP_400_BAD_REQUEST,
        context: dict = None
    ):
        self.error_code = error_code
        self.context = context or {}
        
        # 👑 Tự động bắt vị trí phát sinh lỗi (File, Dòng code, Tên hàm) để phục vụ Forensic Debug
        try:
            caller = inspect.currentframe().f_back
            if caller:
                f_path = caller.f_code.co_filename
                f_name = os.path.basename(f_path)
                try:
                    rel_p = os.path.relpath(f_path, os.getcwd())
                except Exception:
                    rel_p = f_name
                self.location = f"{rel_p}:{caller.f_lineno} in {caller.f_code.co_name}()"
            else:
                self.location = "Unknown location"
        except Exception:
            self.location = "Unknown location"

        # Tự động nạp vị trí vào context để frontend / developer dễ trace
        if "location" not in self.context:
            self.context["location"] = self.location

        # Super tạm thời để tương thích FastAPI, tầng Handler sẽ ghi đè payload trả về
        super().__init__(status_code=status_code, detail=error_code)

from typing import Generic, TypeVar, Optional
from pydantic import BaseModel

T = TypeVar('T')

class ApiResponse(BaseModel, Generic[T]):
    """👑 Khuôn mẫu phản hồi đầu ra bảo mật cao tiêu chuẩn Enterprise"""
    success: bool = True
    error_code: Optional[str] = None
    message: Optional[str] = "Success"
    data: Optional[T] = None

    class Config:
        from_attributes = True
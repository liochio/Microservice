
from pydantic import BaseModel
from typing import Optional, Any


class BaseResponse(BaseModel):
    success: bool = True
    error_code: str = 'SUCCESS'
    message: str = ''
    data: Optional[Any] = None
    trace_id: Optional[str] = None

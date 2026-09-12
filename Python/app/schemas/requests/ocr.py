# 📄 Đường dẫn file: app/schemas/requests/ocr.py
from pydantic import BaseModel, model_validator
from typing import Optional, Any
from app.constants import SystemConstants
from app.core.exceptions.base_exception import FintechBaseException


class OcrScanRequest(BaseModel):
    """👑 DTO ĐẦU VÀO QUÉT HÓA ĐƠN OCR (BASE64 HOẶC IMAGE URL)"""
    image_base64: Optional[Any] = None
    image_url: Optional[Any] = None

    @model_validator(mode="after")
    def validate_image(self):
        if not self.image_base64 and not self.image_url:
            raise FintechBaseException(error_code="MISSING_IMAGE_DATA", status_code=400)
        return self

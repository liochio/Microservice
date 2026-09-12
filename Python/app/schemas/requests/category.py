# 📄 Đường dẫn file: app/schemas/requests/category.py
from pydantic import BaseModel, model_validator
from typing import Optional, Any
from app.constants import SystemConstants
from app.core.exceptions.base_exception import FintechBaseException


class CreateCategoryRequest(BaseModel):
    """
    👑 DTO ĐẦU VÀO TẠO DANH MỤC THU / CHI
    🎯 Mục đích:
       - Nhận tên danh mục, loại thu/chi (EXPENSE / INCOME), icon và mã màu.
       - Validate tuần tự chống rỗng và đúng enum.
    """
    name: Optional[Any] = None
    type: Optional[Any] = None
    icon: Optional[str] = "tag"
    color: Optional[str] = "#1976D2"
    parent_id: Optional[str] = None

    @model_validator(mode="after")
    def validate_category(self):
        if not self.name or str(self.name).strip() == "":
            raise FintechBaseException(error_code=SystemConstants.MISSING_CATEGORY_NAME, status_code=400)

        if not self.type or str(self.type).strip().upper() not in ["EXPENSE", "INCOME"]:
            raise FintechBaseException(error_code=SystemConstants.INVALID_CATEGORY_TYPE, status_code=400)

        self.name = str(self.name).strip()
        self.type = str(self.type).strip().upper()
        return self

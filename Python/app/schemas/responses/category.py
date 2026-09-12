# 📄 Đường dẫn file: app/schemas/responses/category.py
from pydantic import BaseModel
from typing import List, Optional, Any


class CategoryItem(BaseModel):
    id: str
    user_id: Optional[str] = None
    name: str
    type: str
    icon: Optional[str] = "tag"
    color: Optional[str] = "#1976D2"
    parent_id: Optional[str] = None
    status: str = "ACTIVE"


class CategoryListResponse(BaseModel):
    success: bool = True
    error_code: str = "CATEGORY_FETCH_SUCCESS"
    message: str
    data: List[CategoryItem]
    trace_id: Optional[str] = None


class CategoryDetailResponse(BaseModel):
    success: bool = True
    error_code: str = "CATEGORY_CREATE_SUCCESS"
    message: str
    data: CategoryItem
    trace_id: Optional[str] = None

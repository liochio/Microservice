
from pydantic import BaseModel
from typing import Optional, List
from datetime import datetime
from app.schemas.responses.base_response import BaseResponse


class NotificationItem(BaseModel):
    id: str
    user_id: str
    title: str
    content: str
    notification_type: str
    is_read: str
    created_at: Optional[datetime] = None


class NotificationListResponse(BaseResponse):
    data: List[NotificationItem] = []
    unread_count: int = 0

# 📄 Đường dẫn file: app/api/v1/notifications/notifications.py
from fastapi import APIRouter, Depends, Request, status, Query
from sqlalchemy.orm import Session
from typing import List
from datetime import datetime

from app.dependency import get_db
from app.core.security.guard.guards import get_current_user
from app.constants import SystemConstants
from app.core.translator.translator_engine import i18n_translator
from app.models.notification.notification import Notification
from app.schemas.responses.notification import NotificationListResponse, NotificationItem
from app.schemas.responses.base_response import BaseResponse

router = APIRouter(prefix="/notifications", tags=["Notifications"])


@router.get("", response_model=NotificationListResponse, status_code=status.HTTP_200_OK)
async def get_user_notifications(
    request: Request,
    limit: int = Query(50, ge=1, le=100),
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Lấy danh sách thông báo và số lượng chưa đọc của người dùng"""
    user_id = current_user.get("user_id")
    notifs = db.query(Notification).filter(
        Notification.user_id == user_id,
        Notification.status == "ACTIVE"
    ).order_by(Notification.created_at.desc()).limit(limit).all()

    unread_count = db.query(Notification).filter(
        Notification.user_id == user_id,
        Notification.is_read == "UNREAD",
        Notification.status == "ACTIVE"
    ).count()

    items = [
        NotificationItem(
            id=n.id,
            user_id=n.user_id,
            title=n.title,
            content=n.content,
            notification_type=n.notification_type or "SYSTEM",
            is_read=n.is_read,
            created_at=n.created_at
        )
        for n in notifs
    ]

    msg = i18n_translator.translate(request, SystemConstants.TRANSACTION_FETCH_SUCCESS, msg_type=SystemConstants.MSG_TYPE_MESSAGE)
    return NotificationListResponse(
        success=True,
        error_code=SystemConstants.TRANSACTION_FETCH_SUCCESS,
        message=msg,
        data=items,
        unread_count=unread_count,
        trace_id=getattr(request.state, "trace_id", None)
    )


@router.put("/{notification_id}/read", response_model=BaseResponse, status_code=status.HTTP_200_OK)
async def mark_notification_as_read(
    notification_id: str,
    request: Request,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Đánh dấu 1 thông báo là đã đọc"""
    user_id = current_user.get("user_id")
    notif = db.query(Notification).filter(
        Notification.id == notification_id,
        Notification.user_id == user_id
    ).first()

    if notif:
        notif.is_read = "READ"
        notif.updated_at = datetime.now()
        db.commit()

    return BaseResponse(
        success=True,
        error_code=SystemConstants.NOTIFICATION_MARK_READ_SUCCESS,
        message="Đã đánh dấu thông báo là đã đọc.",
        trace_id=getattr(request.state, "trace_id", None)
    )


@router.put("/read-all", response_model=BaseResponse, status_code=status.HTTP_200_OK)
async def mark_all_notifications_as_read(
    request: Request,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Đánh dấu tất cả thông báo của người dùng là đã đọc"""
    user_id = current_user.get("user_id")
    db.query(Notification).filter(
        Notification.user_id == user_id,
        Notification.is_read == "UNREAD"
    ).update({"is_read": "READ", "updated_at": datetime.now()})
    db.commit()

    return BaseResponse(
        success=True,
        error_code=SystemConstants.NOTIFICATION_CLEAR_ALL_SUCCESS,
        message="Đã đánh dấu tất cả thông báo là đã đọc.",
        trace_id=getattr(request.state, "trace_id", None)
    )

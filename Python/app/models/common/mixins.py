

from sqlalchemy import Column, DateTime
from sqlalchemy.sql import func
from sqlalchemy.orm import declarative_mixin

@declarative_mixin
class TimestampMixin:
    # Bỏ sort_order để Alembic và SQLAlchemy bản cũ không báo lỗi khi sinh schema
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
from sqlalchemy import Column, String, Text, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class SystemSetting(Base):
    __tablename__ = "system_settings"
    __table_args__ = {"comment": "Bảng cấu hình cài đặt dùng chung toàn bộ hệ thống phân chia phân hệ type"}

    key = Column(String(100), primary_key=True, index=True, comment="Từ khóa cấu hình")
    value = Column(String(255), nullable=False, comment="Giá trị cấu hình")
    type = Column(String(50), nullable=False, index=True, comment="Phân loại cấu hình hệ thống (AUTH, NOTI, FINANCE...)")
    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False, comment="Trạng thái cấu hình")
    description = Column(Text, nullable=True, comment="Mô tả chi tiết tác dụng cài đặt")
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
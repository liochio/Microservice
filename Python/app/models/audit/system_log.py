from sqlalchemy import Column, String, Text, DateTime, text, Enum
from sqlalchemy.sql import func
from app.db.base import Base

class SystemLog(Base):
    __tablename__ = "system_logs"
    __table_args__ = {"comment": "Bảng lưu vết log lỗi Exception crash thô sâu của lõi hệ thống"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    error_type = Column(String(255), nullable=False, index=True)
    stack_trace = Column(Text, nullable=False)
    component = Column(String(100), nullable=False)
    status = Column(Enum("UNRESOLVED", "RESOLVED", "IGNORED", name="sys_log_status_enum"), server_default=text("'UNRESOLVED'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
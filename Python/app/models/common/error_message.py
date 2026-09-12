from sqlalchemy import Column, String, Text, DateTime, text, Enum
from sqlalchemy.sql import func
from app.db.base import Base

class ErrorMessage(Base):
    __tablename__ = "error_messages"
    __table_args__ = {"comment": "Bảng từ điển định nghĩa toàn bộ mã lỗi, nhãn UI, thông báo tập trung đa ngôn ngữ"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    error_code = Column(String(100), unique=True, nullable=False, index=True, comment="Mã code định danh hệ thống (Key)")
    msg_type = Column(Enum("ERROR", "MESSAGE", "LABEL", name="i18n_msg_type_enum"), nullable=False, index=True, comment="Phân loại: ERROR, MESSAGE, LABEL")
    lang_vi = Column(Text, nullable=False, comment="Nội dung hiển thị tiếng Việt")
    lang_en = Column(Text, nullable=False, comment="Nội dung hiển thị tiếng Anh")
    lang_zh = Column(Text, nullable=False, comment="Nội dung hiển thị tiếng Trung")
    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False, comment="Trạng thái cấu hình")
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
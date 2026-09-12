from sqlalchemy import Column, String, BigInteger, DateTime, text, Enum
from sqlalchemy.dialects.mysql import LONGTEXT
from sqlalchemy.sql import func
from app.db.base import Base

class StoredFile(Base):
    __tablename__ = "stored_files"
    __table_args__ = {"comment": "Bảng lưu trữ siêu dữ liệu file và nội dung text bốc tách base64/CLOB"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    file_name = Column(String(255), nullable=False)
    unique_name = Column(String(255), unique=True, nullable=False, index=True)
    file_path = Column(String(500), nullable=True)
    file_size = Column(BigInteger, nullable=False)
    mime_type = Column(String(100), nullable=False)
    file_type = Column(Enum("AVATAR", "TRANSACTION_PROOF", "OCR_BILL", "FIRMWARE_BINARY", "OTHER", name="file_type_enum"), nullable=False)
    content = Column(LONGTEXT, nullable=True)
    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
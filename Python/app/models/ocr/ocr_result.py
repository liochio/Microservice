from sqlalchemy import Column, String, ForeignKey, Numeric, JSON, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class OcrResult(Base):
    __tablename__ = "ocr_results"
    __table_args__ = {"comment": "Bảng tổng hợp bốc tách dữ liệu hóa đơn thô"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    stored_file_id = Column(String(36), ForeignKey("stored_files.id", ondelete="CASCADE"), nullable=False, index=True)
    merchant_name = Column(String(255), nullable=True, index=True)
    total_amount = Column(Numeric(18, 4), nullable=True)
    raw_ocr_json = Column(JSON, nullable=False)
    status = Column(String(50), server_default=text("'PROCESSED'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
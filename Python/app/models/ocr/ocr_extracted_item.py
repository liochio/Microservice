from sqlalchemy import Column, String, ForeignKey, Numeric, Integer, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class OcrExtractedItem(Base):
    __tablename__ = "ocr_extracted_items"
    __table_args__ = {"comment": "Chi tiết từng mặt hàng bốc tách được trong hóa đơn"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    ocr_result_id = Column(String(36), ForeignKey("ocr_results.id", ondelete="CASCADE"), nullable=False, index=True)
    item_name = Column(String(255), nullable=False)
    quantity = Column(Integer, nullable=False)
    unit_price = Column(Numeric(18, 4), nullable=False)
    total_price = Column(Numeric(18, 4), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
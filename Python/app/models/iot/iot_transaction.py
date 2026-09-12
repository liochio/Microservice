from sqlalchemy import Column, String, ForeignKey, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class IotTransaction(Base):
    __tablename__ = "iot_transactions"
    __table_args__ = {"comment": "Giao dịch tài chính tự động kích hoạt bởi IoT"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    iot_device_id = Column(String(36), ForeignKey("iot_devices.id", ondelete="RESTRICT"), nullable=False, index=True)
    transaction_id = Column(String(36), ForeignKey("transactions.id", ondelete="CASCADE"), nullable=False, index=True)
    trigger_event = Column(String(255), nullable=False)
    status = Column(String(50), server_default=text("'COMPLETED'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
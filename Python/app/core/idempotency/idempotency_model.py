from sqlalchemy import Column, String, DateTime, text
from app.db.base import Base

class IdempotencyKey(Base):
    """
    👑 IDEMPOTENCY KEYS ORM MODEL (DOMAIN LAYER)
    🎯 Bảng găm khóa vật lý gác cổng chống Replay Attack và trùng lặp lệnh chuyển tiền.
    """
    __tablename__ = "idempotency_keys"

    id = Column(String(255), primary_key=True, nullable=False, comment="Chứa chuỗi mã Idempotency-Key từ Client gửi lên")
    user_id = Column(String(255), nullable=False, index=True, comment="Mã định danh User thực hiện giao dịch")
    status = Column(String(50), nullable=False, server_default=text("'PROCESSING'"), comment="Trạng thái xử lý: PROCESSING, SUCCESS, FAILED")
    created_at = Column(DateTime, nullable=False, server_default=text("NOW()"))
    updated_at = Column(DateTime, nullable=False, server_default=text("NOW() ON UPDATE NOW()"))
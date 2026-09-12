import uuid
from datetime import datetime
from sqlalchemy import String, Integer, DateTime, Text
from sqlalchemy.orm import Mapped, mapped_column
from app.db.base import Base


class UserOtp(Base):
    """
    👑 USER OTP ENTITY MODEL
    🎯 ĐÁNH DẤU CHỈNH SỬA: Bổ sung trường retry_count chặn Brute-force.
    """
    __tablename__ = "user_otps"

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id: Mapped[str] = mapped_column(String(36), nullable=False, index=True)
    otp_code: Mapped[str] = mapped_column(String(255), nullable=False)  # Chứa chuỗi Hash Bcrypt
    type: Mapped[str] = mapped_column(String(50), nullable=False)
    content: Mapped[str] = mapped_column(Text, nullable=True)
    is_used: Mapped[int] = mapped_column(Integer, default=0, server_default="0")
    status: Mapped[str] = mapped_column(String(50), default="ACTIVE", server_default="'ACTIVE'")

    # 👑 ĐÁNH DẤU CHỈNH SỬA: Bộ đếm chống Spam/Brute-force
    retry_count: Mapped[int] = mapped_column(Integer, default=0, server_default="0")
    max_retries: Mapped[int] = mapped_column(Integer, default=3, server_default="3")

    expired_at: Mapped[datetime] = mapped_column(DateTime, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.now)
    updated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.now, onupdate=datetime.now)
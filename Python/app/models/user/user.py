from sqlalchemy import Column, String, Boolean, DateTime, Index, text, ForeignKey, Date, Enum
from sqlalchemy.sql import func
from app.db.base import Base

class User(Base):
    __tablename__ = "users"
    __table_args__ = (Index("ix_users_email_phone", "email", "phone_number"), {"comment": "Bảng người dùng cốt lõi hệ thống"})

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    username = Column(String(100), unique=True, nullable=False, index=True)
    email = Column(String(255), unique=True, nullable=False, index=True)
    phone_number = Column(String(20), unique=True, nullable=True, index=True)
    password_hash = Column(String(255), nullable=False)
    full_name = Column(String(255), nullable=True)
    date_of_birth = Column(Date, nullable=True)
    gender = Column(Enum("MALE", "FEMALE", "OTHER", name="gender_enum"), nullable=True)
    avatar_file_id = Column(String(36), ForeignKey("stored_files.id", ondelete="SET NULL"), nullable=True)
    is_active = Column(Boolean, server_default=text("1"), nullable=False)
    is_verified = Column(Boolean, server_default=text("0"), nullable=False)
    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
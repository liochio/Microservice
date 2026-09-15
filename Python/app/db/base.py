from sqlalchemy import create_engine
from sqlalchemy.orm import DeclarativeBase, sessionmaker
# 👑 CHỈNH SỬA ĐÓNG ĐINH: Import đúng đối tượng instance 'settings' viết thường từ module config
from app.core.config.settings import settings

# 1. KHỞI TẠO KHUNG SƯỜN MODEL ORM CHÍNH QUY (CHUẨN HÓA KIẾN TRÚC HỆ THỐNG)
class Base(DeclarativeBase):
    """Lớp nền tảng để các Model thực thể (User, UserOtp, Notification...) kế thừa"""
    pass

# 2. NẠP BIẾN DATABASE_URL CHUẨN HÓA TỪ .ENV QUA INSTANCE SETTINGS
DATABASE_URL = settings.DATABASE_URL

# Khởi tạo engine bảo mật cao pool kết nối chống lụt connection
engine = create_engine(
    DATABASE_URL,
    pool_pre_ping=True,  # Tự động kiểm tra trạng thái sống của connection trước khi gửi lệnh
    pool_recycle=3600,   # Tự động làm mới kết nối tránh lỗi tự ngắt của MySQL
    echo=False
)

# 3. Nhà máy sinh Session chính quy cho Worker triệu hồi
SessionLocal = sessionmaker(
    autocommit=False,
    autoflush=False,
    bind=engine
)
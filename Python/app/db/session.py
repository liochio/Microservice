from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, scoped_session
from app.core.config.settings import settings

# 👑 BẢO MẬT CAO CONNECTION POOL: Tối ưu hiệu năng kết nối sâu, dùng vĩnh viễn (Bảo lưu logic nghiệp vụ lõi)
engine = create_engine(
    settings.DATABASE_URL,
    pool_pre_ping=True,      # Tự động ping DB kiểm tra xem sống hay chết trước khi bốc session
    pool_size=20,            # Số lượng kết nối mặc định luôn duy trì trong bộ nhớ
    max_overflow=10,         # Số lượng kết nối tối đa được phép vượt ngưỡng khi hệ thống quá tải
    pool_recycle=3600,       # Tự động làm mới kết nối sau 1 tiếng để tránh lỗi Timeout từ MySQL
    echo=False               # Đặt thành True nếu muốn xuất chi tiết câu lệnh SQL ra log khi kiểm thử
)

# Khởi tạo nhà máy sản xuất Session chính quy
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# Scoped Session bảo mật cao bảo vệ luồng truy vấn cho các tác vụ Background Task hoặc Schedulers
db_session = scoped_session(SessionLocal)
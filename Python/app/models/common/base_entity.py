from app.db.base import Base

class BaseEntity(Base):
    """👑 Thực thể gốc trừu tượng cho metadata engine """
    __abstract__ = True

# 👑 SIÊU BỌC THÉP METADATA: Ép hệ thống nạp toàn bộ các phân hệ để tạo các bảng vật lý tự động
